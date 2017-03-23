package com.pipai.adv.backend.battle.utils

import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.GridPosition

fun distance(pos1: GridPosition, pos2: GridPosition): Double {
    val xdiff = pos1.x - pos2.x
    val ydiff = pos1.y - pos2.y
    return Math.sqrt((xdiff * xdiff + ydiff * ydiff).toDouble())
}

fun directionFor(from: GridPosition, to: GridPosition): Direction {
    if (from.x == to.x) {
        if (from.y > to.y) {
            return Direction.N
        } else {
            return Direction.S
        }
    } else if (from.y == to.y) {
        if (from.x > to.x) {
            return Direction.E
        } else {
            return Direction.W
        }
    } else {
        if (from.x > to.x && from.y > to.y) {
            return Direction.NE
        } else if (from.x > to.x && from.y < to.y) {
            return Direction.SE
        } else if (from.x < to.x && from.y > to.y) {
            return Direction.NW
        } else {
            return Direction.SW
        }
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

fun oppositeFor(direction:Direction): Direction {
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
