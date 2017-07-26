package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject.NpcEnvironmentObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.npc.NpcList

class BattleBackend(private val npcList: NpcList, private val battleMap: BattleMap) {

    private var state: BattleState = BattleState(BattleTurn.PLAYER, npcList, battleMap, BattleLog())
    private var npcPositions: MutableMap<Int, GridPosition> = mutableMapOf()

    val commandRules: List<CommandRule> = listOf(NoMovingToFullCellRule())
    val commandExecutionRules: List<CommandExecutionRule> = listOf(MovementExecutionRule())

    init {
        for (x in 0 until battleMap.width) {
            for (y in 0 until battleMap.height) {
                val maybeNpc = battleMap.getCell(x, y).fullEnvironmentObject
                if (maybeNpc != null && maybeNpc is NpcEnvironmentObject) {
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

data class ExecutableStatus(val executable: Boolean, val reason: String?) {
    companion object {
        @JvmStatic
        val COMMAND_OK = ExecutableStatus(true, null)
    }
}

data class BattleState(var turn: BattleTurn,
                       val npcList: NpcList,
                       val battleMap: BattleMap,
                       val battleLog: BattleLog)

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

interface BattleCommand

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
