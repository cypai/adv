package com.pipai.adv.backend.battle.engine.commands

import com.pipai.adv.backend.battle.engine.domain.BodyPart

data class DevHpChangeCommand(val unitId: Int, val hp: Int) : BattleCommand

data class DevBindChangeCommand(val unitId: Int, val bodyPart: BodyPart, val amount: Int) : BattleCommand
