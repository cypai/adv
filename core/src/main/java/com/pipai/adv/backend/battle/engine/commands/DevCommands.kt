package com.pipai.adv.backend.battle.engine.commands

data class DevHpChangeCommand(val unitId: Int, val hp: Int) : BattleCommand
