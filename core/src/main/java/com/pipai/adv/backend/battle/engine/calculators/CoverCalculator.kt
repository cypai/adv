package com.pipai.adv.backend.battle.engine.calculators

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.domain.CoverDirections
import com.pipai.adv.backend.battle.engine.domain.CoverType
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.DirectionUtils
import com.pipai.adv.utils.fetch

class CoverCalculator(private val envObjList: AutoIncrementIdMap<EnvObject>, private val map: BattleMap) {

    fun directionalCover(position: GridPosition, direction: Direction): CoverType {
        val neighborPosition = DirectionUtils.neighborCellInDirection(position, direction)
        val neighborCell = map.getCellSafe(neighborPosition) ?: return CoverType.OPEN
        val envObj = neighborCell.fullEnvObjId.fetch(envObjList)
        return when (envObj) {
            is NpcEnvObject -> CoverType.OPEN
            is FullWall -> CoverType.FULL
            is ChestEnvObject -> CoverType.HALF
            is DestructibleEnvObject -> envObj.coverType
            else -> CoverType.OPEN
        }
    }

    fun coverDirections(position: GridPosition): CoverDirections {
        return CoverDirections(
                directionalCover(position, Direction.N),
                directionalCover(position, Direction.S),
                directionalCover(position, Direction.E),
                directionalCover(position, Direction.W))
    }

    fun bestCoverAgainstAttack(defenderPosition: GridPosition, attackerPosition: GridPosition): CoverType {
        val attackDirection = DirectionUtils.directionFor(defenderPosition, attackerPosition)
        val requiredCoverDirections = DirectionUtils.cardinalDefendingDirections(attackDirection)
        val cover = coverDirections(defenderPosition)
        return requiredCoverDirections.map { cover.coverInDirection(it) }
                .maxBy { it.value } ?: CoverType.OPEN
    }

}
