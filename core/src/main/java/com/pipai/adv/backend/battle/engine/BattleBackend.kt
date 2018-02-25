package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.npc.NpcList
import com.pipai.adv.save.AdvSave

class BattleBackend(private val save: AdvSave, private val npcList: NpcList, private val battleMap: BattleMap) {

    private var state: BattleState = BattleState(BattleTurn.PLAYER, npcList, battleMap, BattleLog(), ActionPointState(npcList))
    private lateinit var cache: BattleBackendCache

    val commandRules: List<CommandRule> = listOf(NoActionIfNpcNotExistsRule(), NoActionIfNotEnoughApRule(), NoMovingToFullCellRule())
    val commandExecutionRules: List<CommandExecutionRule> = listOf(MovementExecutionRule(), ReduceApExecutionRule())

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

    fun canBeExecuted(command: BattleCommand): ExecutableStatus {
        for (rule in commandRules) {
            val status = rule.canBeExecuted(command, state, cache)
            if (!status.executable) {
                return status
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

    fun preview(command: BattleCommand): List<PreviewComponent> {
        val previews: MutableList<PreviewComponent> = mutableListOf()
        commandExecutionRules.forEach({ previews.addAll(it.preview(command, state, cache)) })
        return previews
    }

    fun execute(command: BattleCommand) {
        val executionStatus = canBeExecuted(command)
        if (!executionStatus.executable) {
            throw IllegalArgumentException("$command cannot be executed because: ${executionStatus.reason}")
        }
        commandExecutionRules.forEach({ it.execute(command, state, cache) })
        refreshCache()
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
        var npcAp: Int = apState.getNpcAp(npcId)
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

    val startingNumAPs = 2
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
    fun execute(command: BattleCommand, state: BattleState, cache: BattleBackendCache)
}

interface BattleCommand {
}

interface ActionCommand : BattleCommand {
    val unitId: Int
    val requiredAp: Int
}

class BattleLog {
    val log: MutableList<BattleLogEvent> = mutableListOf()
}


interface BattleLogEvent {
    fun description(): String
    fun userFriendlyDescription(): String = ""
}

data class CommandEvent(val command: BattleCommand) : BattleLogEvent {
    override fun description() = "Command was received: $command"
}

sealed class PreviewComponent {

    class MovePreviewComponent(val mapGraph: MapGraph)

}
