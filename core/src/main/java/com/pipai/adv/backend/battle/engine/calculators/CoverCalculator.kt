package com.pipai.adv.backend.battle.engine.calculators

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.domain.CoverDirections
import com.pipai.adv.backend.battle.engine.domain.CoverType
import com.pipai.adv.utils.DirectionUtils

class CoverCalculator(private val map: BattleMap) {

    fun directionalCover(position: GridPosition, direction: Direction): CoverType {
        val neighborPosition = DirectionUtils.neighborCellInDirection(position, direction)
        val neighborCell = map.getCellSafe(neighborPosition) ?: return CoverType.OPEN
        val envObj = neighborCell.fullEnvObject
        return when (envObj) {
            is FullEnvObject.NpcEnvObject -> CoverType.OPEN
            is FullEnvObject.FullWall -> CoverType.FULL
            is FullEnvObject.ChestEnvObject -> CoverType.HALF
            is FullEnvObject.DestructibleEnvObject -> envObj.type.coverType
            null -> CoverType.OPEN
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
