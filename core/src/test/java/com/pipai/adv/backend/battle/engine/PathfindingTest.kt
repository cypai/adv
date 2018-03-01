package com.pipai.adv.backend.battle.engine

import java.util.ArrayList
import java.util.LinkedList

import com.pipai.adv.backend.battle.domain.*
import org.junit.Assert
import org.junit.Test

class PathfindingTest {

    @Test
    fun testOneMobilityMovableList() {
        val map = BattleMap.createBattleMap(3, 3)
        val graph = MapGraph(map, GridPosition(1, 1), 1, 1, 1, false)
        val movableList = graph.getMovableCellPositions(1)
        val req = ArrayList<GridPosition>()
        req.add(GridPosition(0, 1))
        req.add(GridPosition(1, 0))
        req.add(GridPosition(1, 2))
        req.add(GridPosition(2, 1))
        for (r in req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r))
        }
        Assert.assertEquals(req.size.toLong(), movableList.size.toLong())
    }

    @Test
    fun testTwoMobilityMovableList() {
        val map = BattleMap.createBattleMap(4, 4)
        val graph = MapGraph(map, GridPosition(0, 1), 2, 1, 1, false)
        val movableList = graph.getMovableCellPositions(1)
        val req = ArrayList<GridPosition>()
        req.add(GridPosition(0, 0))
        req.add(GridPosition(0, 2))
        req.add(GridPosition(0, 3))
        req.add(GridPosition(1, 1))
        req.add(GridPosition(2, 1))
        req.add(GridPosition(1, 2))
        req.add(GridPosition(1, 0))
        for (r in req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r))
        }
        Assert.assertEquals(req.size.toLong(), movableList.size.toLong())
    }

    @Test
    fun testObstacleMovableList() {
        /*
         * Map looks like: 0 0 0 0 0 1 1 0 0 A 0 0 0 0 0 1
         */
        val map = BattleMap.createBattleMap(4, 4)
        map.getCell(1, 2).fullEnvObject = FullEnvObject.SOLID_FULL_WALL
        map.getCell(2, 2).fullEnvObject = FullEnvObject.SOLID_FULL_WALL
        map.getCell(3, 0).fullEnvObject = FullEnvObject.SOLID_FULL_WALL
        val graph = MapGraph(map, GridPosition(1, 1), 3, 1, 1, false)
        val movableList = graph.getMovableCellPositions(1)
        val req = ArrayList<GridPosition>()
        req.add(GridPosition(0, 0))
        req.add(GridPosition(0, 1))
        req.add(GridPosition(0, 2))
        req.add(GridPosition(0, 3))
        req.add(GridPosition(1, 0))
        req.add(GridPosition(1, 3))
        req.add(GridPosition(2, 0))
        req.add(GridPosition(2, 1))
        req.add(GridPosition(3, 1))
        req.add(GridPosition(3, 2))
        for (r in req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r))
        }
        Assert.assertEquals(req.size.toLong(), movableList.size.toLong())
    }

    @Test
    fun testThreeMobilityMovableList() {
        val map = BattleMap.createBattleMap(8, 8)
        val graph = MapGraph(map, GridPosition(3, 3), 3, 1, 1, false)
        val movableList = graph.getMovableCellPositions(1)
        val req = ArrayList<GridPosition>()
        req.add(GridPosition(0, 3))
        req.add(GridPosition(1, 3))
        req.add(GridPosition(2, 3))
        req.add(GridPosition(4, 3))
        req.add(GridPosition(5, 3))
        req.add(GridPosition(6, 3))
        req.add(GridPosition(3, 0))
        req.add(GridPosition(3, 1))
        req.add(GridPosition(3, 2))
        req.add(GridPosition(3, 4))
        req.add(GridPosition(3, 5))
        req.add(GridPosition(3, 6))
        req.add(GridPosition(1, 1))
        req.add(GridPosition(1, 5))
        req.add(GridPosition(5, 1))
        req.add(GridPosition(5, 5))
        for (r in req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r))
        }
        Assert.assertEquals(28, movableList.size.toLong())
    }

    /*
     * Checks to see if the list contains a valid path from start to end
     */
    private fun checkPathingList(list: LinkedList<GridPosition>, start: GridPosition, end: GridPosition): Boolean {
        if (list.peekLast() != end) {
            return false
        }
        var prev: GridPosition? = start
        for (pos in list) {
            if (prev != null) {
                if (Math.abs(prev.x - pos.x) > 1 || Math.abs(prev.y - pos.y) > 1) {
                    return false
                }
            }
            prev = pos
        }
        return true
    }

    @Test
    fun testCorrectPathing() {
        val map = BattleMap.createBattleMap(4, 4)
        val start = GridPosition(0, 0)
        val end = GridPosition(3, 2)
        val graph = MapGraph(map, start, 10, 1, 1, false)
        val path = graph.getPath(end)
        Assert.assertTrue("Invalid path", checkPathingList(path, start, end))
        Assert.assertEquals(4, path.size.toLong())
    }

    @Test
    fun testTooFarPathing() {
        val map = BattleMap.createBattleMap(4, 4)
        val start = GridPosition(0, 0)
        val end = GridPosition(3, 2)
        val graph = MapGraph(map, start, 3, 1, 1, false)
        val path = graph.getPath(end)
        Assert.assertEquals(0, path.size.toLong())
    }

    @Test
    fun testCannotMoveToNonEmpty() {
        val map = BattleMap.createBattleMap(4, 4)
        map.getCell(3, 0).fullEnvObject = FullEnvObject.SOLID_FULL_WALL
        map.getCell(2, 1).fullEnvObject = FullEnvObject.NpcEnvObject(0, Team.PLAYER, EnvObjTilesetMetadata.NONE)

        val graph = MapGraph(map, GridPosition(1, 1), 10, 1, 1, false)
        Assert.assertFalse("Failed to return false on moving to solid tile", graph.canMoveTo(GridPosition(3, 0)))
        Assert.assertFalse("Failed to return false on moving to tile with other agent",
                graph.canMoveTo(GridPosition(2, 1)))
    }

    @Test
    fun testIllegalSquares() {
        val map = BattleMap.createBattleMap(4, 4)
        val graph = MapGraph(map, GridPosition(1, 1), 10, 1, 1, false)
        val illegal = GridPosition(4, 1)
        Assert.assertFalse("Failed to return false on moving to tile outside map", graph.canMoveTo(illegal))
        Assert.assertEquals(0, graph.getPath(illegal).size.toLong())
    }

    @Test
    fun testNoAP() {
        val map = BattleMap.createBattleMap(3, 3)
        val graph = MapGraph(map, GridPosition(1, 1), 10, 0, 1, false)
        val any = GridPosition(0, 0)
        Assert.assertFalse("Failed to return false on moving to a tile", graph.canMoveTo(any))
        Assert.assertEquals(0, graph.getPath(any).size.toLong())
    }

    @Test
    fun testTwoAP() {
        val map = BattleMap.createBattleMap(5, 5)
        val graph = MapGraph(map, GridPosition(2, 2), 2, 2, 2, false)
        val movableList1 = graph.getMovableCellPositions(1)
        val req1 = ArrayList<GridPosition>()
        req1.add(GridPosition(1, 2))
        req1.add(GridPosition(2, 3))
        req1.add(GridPosition(2, 1))
        req1.add(GridPosition(3, 2))
        for (r in req1) {
            Assert.assertTrue("Does not contain " + r, movableList1.contains(r))
        }
        Assert.assertEquals(req1.size.toLong(), movableList1.size.toLong())
        val movableList2 = graph.getMovableCellPositions(2)
        val req2 = ArrayList<GridPosition>()
        req2.add(GridPosition(0, 2))
        req2.add(GridPosition(1, 3))
        req2.add(GridPosition(1, 1))
        req2.add(GridPosition(2, 4))
        req2.add(GridPosition(2, 0))
        req2.add(GridPosition(3, 3))
        req2.add(GridPosition(3, 1))
        req2.add(GridPosition(4, 2))
        for (r in req2) {
            Assert.assertTrue("Does not contain " + r, movableList2.contains(r))
        }
        Assert.assertEquals(req2.size.toLong(), movableList2.size.toLong())
    }

}
