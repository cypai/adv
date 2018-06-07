package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.calculators.CoverCalculator
import com.pipai.adv.backend.battle.engine.commands.BattleCommand
import com.pipai.adv.backend.battle.engine.commands.TargetStageExecuteCommand
import com.pipai.adv.backend.battle.engine.domain.ExecutableStatus
import com.pipai.adv.backend.battle.engine.domain.NpcStatus
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.log.BattleEndEvent
import com.pipai.adv.backend.battle.engine.log.BattleLog
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent
import com.pipai.adv.backend.battle.engine.rules.command.*
import com.pipai.adv.backend.battle.engine.rules.ending.EndingRule
import com.pipai.adv.backend.battle.engine.rules.ending.MapClearEndingRule
import com.pipai.adv.backend.battle.engine.rules.ending.TotalPartyKillEndingRule
import com.pipai.adv.backend.battle.engine.rules.execution.*
import com.pipai.adv.backend.battle.engine.rules.verification.ApVerificationRule
import com.pipai.adv.backend.battle.engine.rules.verification.VerificationRule
import com.pipai.adv.domain.NpcList
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

    private var state: BattleState = BattleState(Team.PLAYER, npcList, battleMap, BattleLog(),
            ActionPointState(npcList), NpcStatusState(npcList), BattleStats(npcList))
    private lateinit var cache: BattleBackendCache

    /**
     * Rules that verify that the commands are OK
     */
    private val commandRules: List<CommandRule> = listOf(
            NpcMustExistRule(),
            KoCannotTakeActionRule(),
            KoCannotBeAttackedRule(),
            MoveCommandSanityRule(),
            NormalAttackCommandSanityRule(),
            DoubleSlashSanityRule())

    /**
     * Rules that verify that the battle state shown by the preview is OK
     */
    private val verificationRules: List<VerificationRule> = listOf(
            ApVerificationRule())

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
            StageInitializationExecutionRule(),
            DevHpChangeExecutionRule(),
            SkillTpUseExecutionRule(),
            MoveExecutionRule(),
            RushExecutionRule(),
            DefendExecutionRule(),
            WaitExecutionRule(),
            NormalAttackExecutionRule(),
            DoubleSlashExecutionRule(),
            ElementalSkillExecutionRule(),
            HealingSkillExecutionRule(),
            MeleeHitCritExecutionRule(),
            RangedHitCritExecutionRule(),
            AvoidHitCritExecutionRule(),
            DefendHitCritExecutionRule(),
            CoverHitCritExecutionRule(CoverCalculator(battleMap)),
            ElementalResistanceExecutionRule(),
            ApChangeExecutionRule(),
            TpChangeExecutionRule(),
            AttackCalculationExecutionRule(),
            HealExecutionRule(),
            AmmoChangeExecutionRule(),
            StagePreviewExecutionRule(),
            KoExecutionRule())

    private val endingRules: List<EndingRule> = listOf(
            TotalPartyKillEndingRule(),
            MapClearEndingRule())

    companion object {
        const val MELEE_WEAPON_DISTANCE = 1.8
        const val MELEE_WEAPON_DISTANCE2 = (MELEE_WEAPON_DISTANCE * MELEE_WEAPON_DISTANCE).toInt()
        const val RANGED_WEAPON_DISTANCE = 10.0
        const val RANGED_WEAPON_DISTANCE2 = (RANGED_WEAPON_DISTANCE * RANGED_WEAPON_DISTANCE).toInt()
        const val VISIBLE_DISTANCE = 10
        const val VISIBLE_DISTANCE2 = VISIBLE_DISTANCE * VISIBLE_DISTANCE
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
    fun getBattleMapUnsafe(): BattleMap = battleMap
    fun getNpc(npcId: Int) = npcList.getNpc(npcId)
    fun getNpcAp(npcId: Int) = state.apState.getNpcAp(npcId)
    fun getNpcTp(npcId: Int) = npcList.getNpc(npcId)!!.unitInstance.tp
    fun getNpcPositions(): Map<Int, GridPosition> = cache.npcPositions
    fun getNpcPosition(npcId: Int) = cache.npcPositions[npcId]
    fun getNpcTeams(): Map<Int, Team> = cache.npcTeams
    fun getNpcTeam(npcId: Int) = cache.npcTeams[npcId]
    fun getTeamNpcs(): Map<Team, List<Int>> = cache.teamNpcs
    fun getTeam(team: Team) = cache.teamNpcs[team]!!
    fun getNpcAilment(npcId: Int) = state.getNpcAilment(npcId)
    fun getNpcStatus(npcId: Int) = state.getNpcStatus(npcId)
    fun checkNpcStatus(npcId: Int, status: NpcStatus) = state.checkNpcStatus(npcId, status)
    fun getCurrentTurn() = state.turn
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
            if (it.matches(command, previews)) {
                logger.debug("Previewing rule: ${it::class}")
                previews.addAll(it.preview(command, previews, this, state, cache))
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
            if (it.matches(command, previewComponents)) {
                logger.debug("Executing rule: ${it::class}")
                it.execute(command, previewComponents, this, state, cache)
            }
        }
        refreshCache()
        endingRules.forEach {
            val ended = it.evaluate(this, state, cache)
            if (ended) {
                state.battleLog.addEvent(BattleEndEvent(it.endingType, state.battleStats))
                return@forEach
            }
        }
        val events = state.battleLog.getEventsDuringExecution()
        events.forEach { logger.debug("BATTLE EVENT: ${it::class.simpleName} ${it.description()}") }
        return events
    }

    fun executeStagePreview(command: TargetStageExecuteCommand) {
        val previewComponents = command.preview.previews
        previewComponents.forEach { logger.debug("$it") }
        commandExecutionRules.forEach {
            if (it.matches(command, previewComponents)) {
                logger.debug("Executing rule: ${it::class}")
                it.execute(command, previewComponents, this, state, cache)
            }
        }
    }

    fun endTurn() {
        logger.debug("Turn has ended for ${state.turn}")
        when (state.turn) {
            Team.PLAYER -> {
                state.turn = Team.AI
                state.npcList
                        .filter { cache.getNpcTeam(it.key) == Team.AI }
                        .forEach { state.apState.setNpcAp(it.key, ActionPointState.startingNumAPs) }
            }
            Team.AI -> {
                state.turn = Team.PLAYER
                state.npcList
                        .filter { cache.getNpcTeam(it.key) == Team.PLAYER }
                        .forEach { state.apState.setNpcAp(it.key, ActionPointState.startingNumAPs) }
            }
        }
        logger.debug("Turn starting for ${state.turn}")
        state.npcStatusState.decreaseTurnCount()
    }
}

data class BattleBackendCache(val npcPositions: Map<Int, GridPosition>,
                              val teamNpcs: Map<Team, List<Int>>,
                              val npcTeams: Map<Int, Team>,
                              val currentTurnKos: List<Int>) {

    fun getNpcPosition(npcId: Int) = npcPositions[npcId]
    fun getNpcTeam(npcId: Int) = npcTeams[npcId]
    fun getTeam(team: Team) = teamNpcs[team]!!
}

data class BattleState(var turn: Team,
                       val npcList: NpcList,
                       val battleMap: BattleMap,
                       val battleLog: BattleLog,
                       val apState: ActionPointState,
                       val npcStatusState: NpcStatusState,
                       val battleStats: BattleStats) {

    fun getNpc(npcId: Int) = npcList.getNpc(npcId)
    fun getNpcAp(npcId: Int) = apState.getNpcAp(npcId)
    fun getNpcWeapon(npcId: Int) = getNpc(npcId)?.unitInstance?.weapon
    fun getNpcAilment(npcId: Int) = npcStatusState.getNpcAilment(npcId)
    fun getNpcStatus(npcId: Int) = npcStatusState.getNpcStatus(npcId)
    fun checkNpcStatus(npcId: Int, status: NpcStatus) = npcStatusState.checkNpcStatus(npcId, status)
}

