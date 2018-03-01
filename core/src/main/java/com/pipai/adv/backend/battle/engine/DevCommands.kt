package com.pipai.adv.backend.battle.engine

data class DevHpChangeCommand(val unitId: Int, val hp: Int) : BattleCommand

class DevHpChangeExecutionRule : CommandExecutionRule {
    override fun matches(command: BattleCommand): Boolean {
        return command is DevHpChangeCommand
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

        val cmd = command as DevHpChangeCommand
        val npc = state.npcList.getNpc(cmd.unitId)!!
        val damage = npc.unitInstance.hp - cmd.hp
        npc.unitInstance.hp = cmd.hp
        when {
            damage > 0 -> state.battleLog.addEvent(DamageEvent(cmd.unitId, npc, damage, DamageOutcome.HIT))
            damage < 0 -> state.battleLog.addEvent(HealEvent(cmd.unitId, npc, -damage))
        }
    }
}
