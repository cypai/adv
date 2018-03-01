package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.npc.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.adv.utils.getLogger

class BattleBackend(private val save: AdvSave, private val npcList: NpcList, private val battleMap: BattleMap) {

    private val logger = getLogger()

    private var state: BattleState = BattleState(BattleTurn.PLAYER, npcList, battleMap, BattleLog(), ActionPointState(npcList))
    private lateinit var cache: BattleBackendCache

    private val commandRules: List<CommandRule> = listOf(
            NoActionIfNpcNotExistsRule(),
            NoActionIfNotEnoughApRule(),
            MoveCommandSanityRule(),
            NormalAttackCommandSanityRule())

    /**
     * Order matters for CommandExecutionRules. The order in which commands are evaluated will also be returned to the UI
     * as BattleLogEvents, so it is important to order each execution rule in a way that makes sense.
     *
     * Order should look like:
     * Rules that add PreviewComponents (e.g. Move, NormalAttack, Reload)
     * Evaluation rules (those that calculate based on PreviewComponents)
     * Secondary Effect rules
     * End-of-command rules (e.g. ReduceAP, KO)
     */
    private val commandExecutionRules: List<CommandExecutionRule> = listOf(
            MovementExecutionRule(),
            NormalAttackExecutionRule(),
            BaseHitCritExecutionRule(),
            MeleeHitCritExecutionRule(),
            RangedHitCritExecutionRule(),
            AttackCalculationExecutionRule(),
            AmmoChangeExecutionRule(),
            ReduceApExecutionRule())

    companion object {
        const val MELEE_WEAPON_DISTANCE = 1.8
        const val MELEE_WEAPON_DISTANCE2 = (MELEE_WEAPON_DISTANCE * MELEE_WEAPON_DISTANCE).toInt()
        const val RANGED_WEAPON_DISTANCE = 10.0
        const val RANGED_WEAPON_DISTANCE2 = (RANGED_WEAPON_DISTANCE * RANGED_WEAPON_DISTANCE).toInt()
    }

    init {
        refreshCache()
    }

    private fun refreshCache() {
        val npcPositions: MutableMap<Int, GridPosition> = mutableMapOf()
        val teamNpcs: MutableMap<Team, MutableList<Int>> = mutableMapOf()
        val npcTeams: MutableMap<Int, Team> = mutableMapOf()

        teamNpcs.put(Team.PLAYER, mutableListOf())
        teamNpcs.put(Team.AI, mutableListOf())
        for (x in 0 until battleMap.width) {
            for (y in 0 until battleMap.height) {
                val maybeNpc = battleMap.getCell(x, y).fullEnvObject
                if (maybeNpc != null && maybeNpc is NpcEnvObject) {
                    val npcId = maybeNpc.npcId
                    npcPositions.put(npcId, GridPosition(x, y))
                    if (save.npcInPlayerGuild(npcId)) {
                        teamNpcs[Team.PLAYER]!!.add(npcId)
                        npcTeams[npcId] = Team.PLAYER
                    } else {
                        teamNpcs[Team.AI]!!.add(npcId)
                        npcTeams[npcId] = Team.AI
                    }
                }
            }
        }
        cache = BattleBackendCache(npcPositions, teamNpcs.mapValues { it.value.toList() }, npcTeams)
    }

    fun getBattleMapState(): BattleMap = battleMap.deepCopy()
    fun getNpcPositions(): Map<Int, GridPosition> = cache.npcPositions
    fun getNpcTeams(): Map<Int, Team> = cache.npcTeams
    fun getTeamNpcs(): Map<Team, List<Int>> = cache.teamNpcs
    fun getBattleState() = state.copy()

    fun canBeExecuted(command: BattleCommand): ExecutableStatus {
        logger.debug("Checking executable status of $command")
        for (rule in commandRules) {
            val status = rule.canBeExecuted(command, state, cache)
            if (!status.executable) {
                return status
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

    fun preview(command: BattleCommand): List<PreviewComponent> {
        logger.debug("Previewing $command")
        val previews: MutableList<PreviewComponent> = mutableListOf()
        commandExecutionRules.forEach{
            if (it.matches(command)) {
                previews.addAll(it.preview(command, state, cache))
            }
        }
        return previews
    }

    fun execute(command: BattleCommand): List<BattleLogEvent> {
        val executionStatus = canBeExecuted(command)
        logger.debug("Executing $command")
        if (!executionStatus.executable) {
            throw IllegalArgumentException("$command cannot be executed because: ${executionStatus.reason}")
        }
        state.battleLog.beginExecution()
        val previewComponents = preview(command)
        previewComponents.forEach { logger.debug("$it") }
        commandExecutionRules.forEach{
            if (it.matches(command)) {
                logger.debug("Executing rule: ${it::class}")
                it.execute(command, previewComponents, state, cache)
            }
        }
        refreshCache()
        val events = state.battleLog.getEventsDuringExecution()
        events.forEach { logger.debug("BATTLE EVENT: ${it::class.simpleName} ${it.description()}") }
        return events
    }
}

data class BattleBackendCache(val npcPositions: Map<Int, GridPosition>,
                              val teamNpcs: Map<Team, List<Int>>,
                              val npcTeams: Map<Int, Team>)

class NoActionIfNpcNotExistsRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            if (!state.npcList.npcExists(command.unitId)) {
                return ExecutableStatus(false, "Npc does not exist")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

class NoActionIfNotEnoughApRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus {
        if (command is ActionCommand) {
            if (!hasEnoughAp(command.unitId, command.requiredAp, state.apState)) {
                return ExecutableStatus(false, "Not enough action points available")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

    private fun hasEnoughAp(npcId: Int, requiredAp: Int, apState: ActionPointState): Boolean {
        val npcAp: Int = apState.getNpcAp(npcId)
        return npcAp >= requiredAp
    }
}

class ReduceApExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand): Boolean {
        return command is ActionCommand
    }

    override fun preview(command: BattleCommand,
                         state: BattleState,
                         cache: BattleBackendCache): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         previews: List<PreviewComponent>,
                         state: BattleState,
                         cache: BattleBackendCache) {
        if (command is ActionCommand) {
            val newAp = state.apState.getNpcAp(command.unitId) - command.requiredAp
            state.apState.setNpcAp(command.unitId, newAp)
        }
    }
}

data class ExecutableStatus(val executable: Boolean, val reason: String?) {
    companion object {
        @JvmStatic
        val COMMAND_OK = ExecutableStatus(true, null)
    }
}

data class BattleState(var turn: BattleTurn,
                       val npcList: NpcList,
                       val battleMap: BattleMap,
                       val battleLog: BattleLog,
                       val apState: ActionPointState)

class ActionPointState(npcList: NpcList) {

    companion object {
        val startingNumAPs = 2
    }

    private var apMap: MutableMap<Int, Int> = mutableMapOf()

    init {
        npcList.forEach { apMap.put(it.key, startingNumAPs) }
    }

    fun npcIdExists(npcId: Int): Boolean {
        return apMap.containsKey(npcId)
    }

    fun getNpcAp(npcId: Int): Int {
        val npcAp = apMap.get(npcId)
        if (npcAp != null) {
            return npcAp
        }
        throw IllegalArgumentException("Cannot get AP of null NPC")
    }

    fun setNpcAp(npcId: Int, points: Int) {
        if (points < 0) {
            throw IllegalArgumentException("Cannot set AP to be negative")
        }
        apMap.put(npcId, points)
    }
}

enum class BattleTurn {
    PLAYER, ENEMY
}

interface CommandRule {
    fun canBeExecuted(command: BattleCommand, state: BattleState, cache: BattleBackendCache): ExecutableStatus
}

interface CommandExecutionRule {
    fun matches(command: BattleCommand): Boolean
    fun preview(command: BattleCommand, state: BattleState, cache: BattleBackendCache): List<PreviewComponent>
    fun execute(command: BattleCommand, previews: List<PreviewComponent>, state: BattleState, cache: BattleBackendCache)
}

interface BattleCommand {
}

interface ActionCommand : BattleCommand {
    val unitId: Int
    val requiredAp: Int
}

interface HitCritCommand : BattleCommand {
    val targetId: Int
    val baseHit: Int
    val baseCrit: Int
}

interface WeaponCommand : BattleCommand {
    val weapon: InventoryItem.WeaponInstance
}

class BattleLog {
    private val log: MutableList<BattleLogEvent> = mutableListOf()

    private var executionIndex = 0

    fun addEvent(event: BattleLogEvent) {
        log.add(event)
    }

    fun beginExecution() {
        executionIndex = log.size
    }

    fun getEventsDuringExecution(): List<BattleLogEvent> {
        return log.takeLast(log.size - executionIndex)
    }
}

interface BattleLogEvent {
    fun description(): String
    fun userFriendlyDescription(): String = ""
}

sealed class PreviewComponent {

    data class ToHitPreviewComponent(val toHit: Int) : PreviewComponent() {
        override val description: String = "Base to hit"
    }

    data class ToCritPreviewComponent(val toCrit: Int) : PreviewComponent() {
        override val description: String = "Base to crit"
    }

    data class ToHitFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent()

    data class ToCritFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent()

    data class DamagePreviewComponent(val minDamage: Int, val maxDamage: Int) : PreviewComponent() {
        override val description: String = "Base damage range"
    }

    data class DamageFlatAdjustmentPreviewComponent(val adjustment: Int, override val description: String) : PreviewComponent()

    data class AmmoChangePreviewComponent(val npcId: Int, val newAmount: Int) : PreviewComponent() {
        override val description: String = "Ammo change"
    }

    data class SecondaryEffectPreviewComponent(val chance: Int, override val description: String) : PreviewComponent()

    abstract val description: String
}
