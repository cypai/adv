package com.pipai.adv.utils

import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.GridPosition

object DirectionUtils {

    fun directionFor(fromX: Float, fromY: Float, toX: Float, toY: Float): Direction {
        if (fromX == toX) {
            if (fromY > toY) {
                return Direction.S
            } else {
                return Direction.N
            }
        } else if (fromY == toY) {
            if (fromX > toX) {
                return Direction.W
            } else {
                return Direction.E
            }
        } else {
            if (fromX > toX && fromY > toY) {
                return Direction.SW
            } else if (fromX > toX && fromY < toY) {
                return Direction.NW
            } else if (fromX < toX && fromY > toY) {
                return Direction.SE
            } else {
                return Direction.NE
            }
        }
    }

    fun cardinalMoveDirection(previousDirection: Direction, nextDirection: Direction): Direction {
        return when (nextDirection) {
            Direction.NE -> when (previousDirection) {
                Direction.N -> Direction.E
                Direction.E -> Direction.N
                else -> Direction.E
            }
            Direction.NW -> when (previousDirection) {
                Direction.N -> Direction.W
                Direction.W -> Direction.N
                else -> Direction.W
            }
            Direction.SE -> when (previousDirection) {
                Direction.S -> Direction.E
                Direction.E -> Direction.S
                else -> Direction.E
            }
            Direction.SW -> when (previousDirection) {
                Direction.S -> Direction.W
                Direction.W -> Direction.S
                else -> Direction.W
            }
            else -> nextDirection
        }
    }

    fun isInGeneralDirection(direction: Direction, targetDirection: Direction): Boolean {
        return when (direction) {
            Direction.N -> listOf(Direction.N, Direction.NE, Direction.NW).contains(targetDirection)
            Direction.S -> listOf(Direction.S, Direction.SE, Direction.SW).contains(targetDirection)
            Direction.E -> listOf(Direction.E, Direction.NE, Direction.SE).contains(targetDirection)
            Direction.W -> listOf(Direction.W, Direction.NW, Direction.SW).contains(targetDirection)
            Direction.NE -> listOf(Direction.NE, Direction.N, Direction.E).contains(targetDirection)
            Direction.NW -> listOf(Direction.NW, Direction.N, Direction.W).contains(targetDirection)
            Direction.SE -> listOf(Direction.SE, Direction.S, Direction.E).contains(targetDirection)
            Direction.SW -> listOf(Direction.SW, Direction.S, Direction.W).contains(targetDirection)
        }
    }

    fun perpendicularsFor(direction: Direction): List<Direction> {
        return when (direction) {
            Direction.N -> listOf(Direction.W, Direction.E)
            Direction.S -> listOf(Direction.E, Direction.W)
            Direction.E -> listOf(Direction.N, Direction.S)
            Direction.W -> listOf(Direction.S, Direction.N)
            Direction.NE -> listOf(Direction.NW, Direction.SE)
            Direction.NW -> listOf(Direction.SW, Direction.NE)
            Direction.SE -> listOf(Direction.NE, Direction.SW)
            Direction.SW -> listOf(Direction.SE, Direction.NW)
        }
    }

    fun oppositeFor(direction: Direction): Direction {
        return when (direction) {
            Direction.N -> Direction.S
            Direction.S -> Direction.N
            Direction.E -> Direction.W
            Direction.W -> Direction.E
            Direction.NE -> Direction.SW
            Direction.NW -> Direction.SE
            Direction.SE -> Direction.NW
            Direction.SW -> Direction.NE
        }
    }
}
