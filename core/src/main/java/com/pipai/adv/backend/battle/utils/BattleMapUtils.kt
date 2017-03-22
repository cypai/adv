package com.pipai.adv.backend.battle.utils

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.BattleMapCellSpecialFlag.Exit
import com.pipai.adv.backend.battle.domain.GridPosition

fun isExit(map: BattleMap, position: GridPosition) = isExit(map, position.x, position.y)

fun isExit(map: BattleMap, x: Int, y: Int) = map.getCell(x, y).specialFlags.any { it is Exit }
