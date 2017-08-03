package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.npc.NpcList

class BattleBackend(private val npcList: NpcList, private val battleMap: BattleMap) {

    private var state: BattleState = BattleState(BattleTurn.PLAYER, npcList, battleMap, BattleLog(), ActionPointState(npcList))
    private var npcPositions: MutableMap<Int, GridPosition> = mutableMapOf()

    val commandRules: List<CommandRule> = listOf(NoActionIfNpcNotExistsRule(), NoActionIfNotEnoughApRule(), NoMovingToFullCellRule())
    val commandExecutionRules: List<CommandExecutionRule> = listOf(MovementExecutionRule(), ReduceApExecutionRule())

    init {
        for (x in 0 until battleMap.width) {
            for (y in 0 until battleMap.height) {
                val maybeNpc = battleMap.getCell(x, y).fullEnvObject
                if (maybeNpc != null && maybeNpc is NpcEnvObject) {
                    npcPositions.put(maybeNpc.npcId, GridPosition(x, y))
                }
            }
        }
    }

    fun getBattleMapState(): BattleMap = battleMap.deepCopy()
    fun getNpcPositions(): Map<Int, GridPosition> = npcPositions

    fun canBeExecuted(command: BattleCommand): ExecutableStatus {
        for (rule in commandRules) {
            val status = rule.canBeExecuted(command, state, npcPositions)
            if (!status.executable) {
                return status
            }
        }
        return ExecutableStatus.COMMAND_OK
    }

    fun preview(command: BattleCommand): List<PreviewComponent> {
        val previews: MutableList<PreviewComponent> = mutableListOf()
        commandExecutionRules.forEach({ previews.addAll(it.preview(command, state, npcPositions)) })
        return previews
    }

    fun execute(command: BattleCommand) {
        val executionStatus = canBeExecuted(command)
        if (!executionStatus.executable) {
            throw IllegalArgumentException("$command cannot be executed because: ${executionStatus.reason}")
        }
        commandExecutionRules.forEach({ it.execute(command, state, npcPositions) })
    }
}

class NoActionIfNpcNotExistsRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, unitPositions: Map<Int, GridPosition>): ExecutableStatus {
        if (command is ActionCommand) {
            if (!state.npcList.npcExists(command.unitId)) {
                return ExecutableStatus(false, "Npc does not exist")
            }
        }
        return ExecutableStatus.COMMAND_OK
    }
}

class NoActionIfNotEnoughApRule : CommandRule {
    override fun canBeExecuted(command: BattleCommand, state: BattleState, unitPositions: Map<Int, GridPosition>): ExecutableStatus {
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
                         unitPositions: Map<Int, GridPosition>): List<PreviewComponent> {

        return listOf()
    }

    override fun execute(command: BattleCommand,
                         state: BattleState,
                         unitPositions: MutableMap<Int, GridPosition>) {
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
        npcList.forEach{ apMap.put(it.key, startingNumAPs) }
    }

    fun npcIdExists(npcId: Int): Boolean {
        return apMap.containsKey(npcId)
    }

    fun getNpcAp(npcId: Int): Int {
        val npcAp = apMap.get(npcId)
        if (npcAp != null){
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

class BattleUnitEval

enum class BattleTurn {
    PLAYER, ENEMY
}

interface CommandRule {
    fun canBeExecuted(command: BattleCommand, state: BattleState, unitPositions: Map<Int, GridPosition>): ExecutableStatus
}

interface CommandExecutionRule {
    fun matches(command: BattleCommand): Boolean
    fun preview(command: BattleCommand, state: BattleState, unitPositions: Map<Int, GridPosition>): List<PreviewComponent>
    fun execute(command: BattleCommand, state: BattleState, unitPositions: MutableMap<Int, GridPosition>)
}

interface BattleCommand {
}

interface ActionCommand: BattleCommand {
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
