package com.pipai.adv.backend.battle.engine.domain

import com.pipai.adv.backend.battle.domain.Direction

data class CoverDirections(val north: CoverType,
                           val south: CoverType,
                           val east: CoverType,
                           val west: CoverType) {

    fun coverInDirection(direction: Direction): CoverType {
        return when (direction) {
            Direction.N -> north
            Direction.S -> south
            Direction.E -> east
            Direction.W -> west
            else -> throw IllegalArgumentException("Only cardinal directions allowed, but received $direction")
        }
    }
}
