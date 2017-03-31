package com.pipai.test.fixtures

import com.pipai.adv.backend.battle.domain.BattleUnit
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.backend.battle.domain.UnitSchema
import com.pipai.adv.backend.battle.domain.UnitStats

fun battleUnitFromStats(stats: UnitStats, id: Int): BattleUnit {
    val schema = UnitSchema("Test Unit", stats)
    val unitInstance = UnitInstance(schema, "Test Unit Nickname", schema.baseStats.hpMax, schema.baseStats.mpMax, null)
    return BattleUnit(unitInstance, id)
}
