package com.pipai.adv.map

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.ObjectSet
import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.utils.*

class FogOfWar {

    private val npcIdCachedPosition: IntMap<GridPosition> = IntMap()
    private val npcIdVisibility: IntMap<ObjectSet<GridPosition>> = IntMap()
    private val playerVisibility: ObjectSet<GridPosition> = ObjectSet()
    private val playerFog: ObjectSet<GridPosition> = ObjectSet()

    fun calculateVisibility(backend: BattleBackend, npcId: Int, position: GridPosition) {
        clearVisibility(backend, npcId)

        val map = backend.getBattleMapUnsafe()
        val envObjList = backend.getBattleState().envObjList
        val visibilityBounds = GridUtils.boundaries(
                GridPosition(
                        Math.max(0, position.x - BattleBackend.VISIBLE_DISTANCE),
                        Math.max(0, position.y - BattleBackend.VISIBLE_DISTANCE)),
                GridPosition(
                        Math.min(map.width - 1, position.x + BattleBackend.VISIBLE_DISTANCE),
                        Math.min(map.height - 1, position.y + BattleBackend.VISIBLE_DISTANCE)))

        val visibility = npcIdVisibility[npcId]
        visibilityBounds.forEach {
            val supercover = supercover(position, it)
            val rayPositions = supercover.getSupercover()
            var stopped = false
            for (rayPosition in rayPositions) {
                if (MathUtils.distance2(position.x, position.y, rayPosition.x, rayPosition.y) > BattleBackend.VISIBLE_DISTANCE2) {
                    stopped = true
                    break
                }
                visibility.add(rayPosition)
                if (blocksVision(envObjList, map, rayPosition)) {
                    stopped = true
                    break
                }
            }
            if (!stopped) {
                visibility.add(it)
            }
        }
        visibility.add(position)
        val tilesInRange = GridUtils.gridInRadius(position, BattleBackend.VISIBLE_DISTANCE,
                0, 0, map.width - 1, map.height - 1)
        tilesInRange.forEach {
            if (blocksVision(envObjList, map, it)) {
                val tilesInFront = GridUtils.tilesInFront(position, it)
                tilesInFront.forEach { tileInFront ->
                    if (visibility.contains(tileInFront)) {
                        visibility.add(it)
                    }
                }
            }
        }
        if (backend.getNpcTeam(npcId) == Team.PLAYER) {
            playerVisibility.addAll(visibility)
            playerFog.addAll(visibility)
        }
        npcIdCachedPosition.put(npcId, position)
    }

    private fun blocksVision(envObjList: AutoIncrementIdMap<EnvObject>, map: BattleMap, position: GridPosition): Boolean {
        val envObj = map.getCell(position).fullEnvObjId.fetch(envObjList)
        return envObj != null && envObj is FullWall
    }

    fun getPreviouslyCalculatedPosition(npcId: Int): GridPosition? {
        return npcIdCachedPosition[npcId]
    }

    fun clearVisibility(backend: BattleBackend, npcId: Int) {
        if (backend.getNpcTeam(npcId) == Team.PLAYER) {
            val visibleTiles = npcIdVisibility[npcId]
            visibleTiles?.forEach {
                playerVisibility.remove(it)
            }
        }
        if (npcIdVisibility.containsKey(npcId)) {
            npcIdVisibility[npcId]?.clear()
        } else {
            npcIdVisibility.put(npcId, ObjectSet())
        }
    }

    fun canSee(npcId: Int, position: GridPosition): Boolean {
        return npcIdVisibility[npcId]?.contains(position) ?: false
    }

    fun setPlayerTileVisibility(position: GridPosition, visibility: TileVisibility) {
        when (visibility) {
            TileVisibility.VISIBLE -> {
                playerVisibility.add(position)
                playerFog.add(position)
            }
            TileVisibility.SEEN -> playerFog.add(position)
            TileVisibility.NEVER_SEEN -> {
                playerVisibility.remove(position)
                playerFog.remove(position)
            }
        }
    }

    fun getPlayerTileVisibility(position: GridPosition): TileVisibility {
        return when {
            playerVisibility.contains(position) -> TileVisibility.VISIBLE
            playerFog.contains(position) -> TileVisibility.SEEN
            else -> TileVisibility.NEVER_SEEN
        }
    }

    fun getPlayerVisibleTilesUnsafe() = playerVisibility
    fun getPlayerFogTilesUnsafe() = playerFog

    private fun supercover(a: GridPosition, b: GridPosition): Supercover {
        return supercover(gridCenter(a), gridCenter(b))
    }

    private fun supercover(a: Vector2, b: Vector2): Supercover {
        val points = Supercover()
        var i: Int // loop counter
        val ystep: Int
        val xstep: Int // the step on y and x axis
        var error: Int // the error accumulated during the increment
        var errorprev: Int // *vision the previous value of the error variable
        var y = a.y.toInt()
        var x = a.x.toInt() // the line points
        val ddy: Int
        val ddx: Int // compulsory variables: the double values of dy and dx
        var dx = b.x.toInt() - a.x.toInt()
        var dy = b.y.toInt() - a.y.toInt()
        // NB the last point can't be here, because of its previous point (which has to be verified)
        if (dy < 0) {
            ystep = -1
            dy = -dy
        } else {
            ystep = 1
        }
        if (dx < 0) {
            xstep = -1
            dx = -dx
        } else {
            xstep = 1
        }
        // work with double values for full precision
        ddy = 2 * dy
        ddx = 2 * dx
        if (ddx >= ddy) { // first octant (0 <= slope <= 1)
            // compulsory initialization (even for errorprev, needed when dx==dy)
            error = dx // start in the middle of the square
            errorprev = error
            i = 0
            while (i < dx) { // do not use the first point (already done)
                x += xstep
                error += ddy
                if (error > ddx) { // increment y if AFTER the middle ( > )
                    y += ystep
                    error -= ddx
                    // three cases (octant == right->right-top for directions below):
                    if (error + errorprev < ddx) {
                        points.add(GridPosition(x, y - ystep))
                    } else if (error + errorprev > ddx) {
                        points.add(GridPosition(x - xstep, y))
                    } else { // corner: bottom and left squares also
                        points.addCornerPair(GridPosition(x, y - ystep), GridPosition(x - xstep, y))
                    }
                }
                points.add(GridPosition(x, y))
                errorprev = error
                i++
            }
        } else { // the same as above
            error = dy
            errorprev = error
            i = 0
            while (i < dy) {
                y += ystep
                error += ddx
                if (error > ddy) {
                    x += xstep
                    error -= ddy
                    if (error + errorprev < ddy) {
                        points.add(GridPosition(x - xstep, y))
                    } else if (error + errorprev > ddy) {
                        points.add(GridPosition(x, y - ystep))
                    } else {
                        points.addCornerPair(GridPosition(x - xstep, y), GridPosition(x, y - ystep))
                    }
                }
                points.add(GridPosition(x, y))
                errorprev = error
                i++
            }
        }
        points.remove(GridPosition(b.x.toInt(), b.y.toInt()))
        return points
    }

    private fun gridCenter(pos: GridPosition): Vector2 {
        return Vector2(pos.x + 0.5f, pos.y + 0.5f)
    }

    private class Supercover {

        private val supercover: Array<GridPosition> = ArrayUtils.libgdxArrayOf()
        private val corners: Array<CornerPair> = ArrayUtils.libgdxArrayOf()

        fun getSupercover(): Array<GridPosition> {
            return supercover
        }

        fun remove(pos: GridPosition) {
            supercover.removeValue(pos, false)
        }

        fun add(pos: GridPosition) {
            supercover.add(pos)
        }

        fun addCornerPair(a: GridPosition, b: GridPosition) {
            corners.add(CornerPair(a, b))
        }

        data class CornerPair internal constructor(private val a: GridPosition, private val b: GridPosition)
    }

}

enum class TileVisibility {
    NEVER_SEEN, SEEN, VISIBLE
}
