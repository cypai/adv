package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.domain.BattleTurn
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.BattleLog
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent
import com.pipai.adv.backend.battle.engine.rules.command.*
import com.pipai.adv.backend.battle.engine.rules.execution.*
import com.pipai.adv.backend.battle.engine.rules.verification.ApRequirementRule
import com.pipai.adv.backend.battle.engine.rules.verification.VerificationRule
import com.pipai.adv.npc.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.adv.utils.getLogger

/**
 * The engine that keeps track of battle game state and executes commands.
 *
 * Notes:
 * npcList contains a list of ALL characters, including player guild members, randomly generated enemies,
 * defeated characters, etc. This means that not all characters in the list are active participants in battle.
 *
 * The BattleBackendCache is a snapshot of the game state before commands are executed. They might not reflect
 * changes by ExecutionRules.
 *
 * teamNpcs and npcTeams should only contain active characters in battle (unless they were KOed as a result of the commands)
 *
 * currentTurnKos is a list of player characters that were already KOed at the start of the commands.
 */
class BattleBackend(private val save: AdvSave, private val npcList: NpcList, private val battleMap: BattleMap) {

    private val logger = getLogger()

    private var state: BattleState = BattleState(BattleTurn.PLAYER, npcList, battleMap, BattleLog(), ActionPointState(npcList))
    private lateinit var cache: BattleBackendCache

    /**
     * Rules that verify that the commands are OK
     */
    private val commandRules: List<CommandRule> = listOf(
            NpcMustExistRule(),
            KoCannotTakeActionRule(),
            KoCannotBeAttackedRule(),
            MoveCommandSanityRule(),
            NormalAttackCommandSanityRule())

    /**
     * Rules that verify that the battle state shown by the preview is OK
     */
    private val verificationRules: List<VerificationRule> = listOf(
            ApRequirementRule())

    /**
     * Order matters for CommandExecutionRules. The order in which commands are evaluated will also be returned to the UI
     * as BattleLogEvents, so it is important to order each execution rule in a way that makes sense.
     *
     * Order should look like:
     * Rules that add PreviewComponents (e.g. Move, NormalAttack, Reload)
     * Evaluation rules (those that calculate based on PreviewComponents)
     * Secondary Effect rules
     * End-of-commands rules (e.g. ReduceAP, KO)
     */
    private val commandExecutionRules: List<CommandExecutionRule> = listOf(
            DevHpChangeExecutionRule(),
            MoveExecutionRule(),
            NormalAttackExecutionRule(),
            BaseHitCritExecutionRule(),
            MeleeHitCritExecutionRule(),
            RangedHitCritExecutionRule(),
            AttackCalculationExecutionRule(),
            AmmoChangeExecutionRule(),
            ChangeApExecutionRule(),
            KoExecutionRule())

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
        val currentTurnKos: MutableList<Int> = mutableListOf()

        teamNpcs.put(Team.PLAYER, mutableListOf())
        teamNpcs.put(Team.AI, mutableListOf())
        for (x in 0 until battleMap.width) {
            for (y in 0 until battleMap.height) {
                val envObject = battleMap.getCell(x, y).fullEnvObject
                if (envObject != null && envObject is NpcEnvObject) {
                    val npcId = envObject.npcId
                    npcPositions.put(npcId, GridPosition(x, y))
                    val team = envObject.team
                    teamNpcs[team]!!.add(npcId)
                    npcTeams[npcId] = team
                    val npc = npcList.getNpc(envObject.npcId)!!
                    if (npc.unitInstance.hp <= 0) {
                        currentTurnKos.add(npcId)
                    }
                }
            }
        }
        cache = BattleBackendCache(npcPositions, teamNpcs.mapValues { it.value.toList() }, npcTeams, currentTurnKos)
    }

    fun getBattleMapState(): BattleMap = battleMap.deepCopy()
    fun getNpc(npcId: Int) = npcList.getNpc(npcId)
    fun getNpcAp(npcId: Int) = state.apState.getNpcAp(npcId)
    fun getNpcPositions(): Map<Int, GridPosition> = cache.npcPositions
    fun getNpcPosition(npcId: Int) = cache.npcPositions[npcId]
    fun getNpcTeams(): Map<Int, Team> = cache.npcTeams
    fun getNpcTeam(npcId: Int) = cache.npcTeams[npcId]
    fun getTeamNpcs(): Map<Team, List<Int>> = cache.teamNpcs
    fun getTeam(team: Team) = cache.teamNpcs[team]!!
    fun getBattleState() = state.copy()

    fun canBeExecuted(command: BattleCommand): ExecutableStatus {
        logger.debug("Checking executable status of $command")
        commandRules.forEach { rule ->
            val status = rule.canBeExecuted(command, state, cache)
            if (!status.executable) {
                logger.debug("Failed ${rule::class}")
                return status
            }
        }
        val previewComponents = preview(command)
        verificationRules.forEach { rule ->
            val status = rule.verify(previewComponents, state)
            if (!status.executable) {
                logger.debug("Failed ${rule::class}")
                return status
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

    fun preview(command: BattleCommand): List<PreviewComponent> {
        val previews: MutableList<PreviewComponent> = mutableListOf()
        commandExecutionRules.forEach {
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
        commandExecutionRules.forEach {
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
                              val npcTeams: Map<Int, Team>,
                              val currentTurnKos: List<Int>)

data class BattleState(var turn: BattleTurn,
                       val npcList: NpcList,
                       val battleMap: BattleMap,
                       val battleLog: BattleLog,
                       val apState: ActionPointState)

