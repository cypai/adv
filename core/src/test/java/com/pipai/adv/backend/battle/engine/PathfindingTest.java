package com.pipai.adv.backend.battle.engine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.BattleMap;
import com.pipai.adv.backend.battle.domain.BattleMapDomainKt;
import com.pipai.adv.backend.battle.domain.BattleUnit;
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject;
import com.pipai.adv.backend.battle.domain.GridPosition;
import com.pipai.adv.backend.battle.domain.UnitStats;
import com.pipai.test.fixtures.BattleUnitFixturesKt;

public class PathfindingTest {

    @Test
    public void testOneMobilityMovableList() {
        BattleMap map = BattleMap.Factory.createBattleMap(3, 3);
        MapGraph graph = new MapGraph(map, new GridPosition(1, 1), 1, 1, 1, false);
        List<GridPosition> movableList = graph.getMovableCellPositions(1);
        ArrayList<GridPosition> req = new ArrayList<>();
        req.add(new GridPosition(0, 1));
        req.add(new GridPosition(1, 0));
        req.add(new GridPosition(1, 2));
        req.add(new GridPosition(2, 1));
        for (GridPosition r : req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r));
        }
        Assert.assertEquals(req.size(), movableList.size());
    }

    @Test
    public void testTwoMobilityMovableList() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        MapGraph graph = new MapGraph(map, new GridPosition(0, 1), 2, 1, 1, false);
        List<GridPosition> movableList = graph.getMovableCellPositions(1);
        ArrayList<GridPosition> req = new ArrayList<>();
        req.add(new GridPosition(0, 0));
        req.add(new GridPosition(0, 2));
        req.add(new GridPosition(0, 3));
        req.add(new GridPosition(1, 1));
        req.add(new GridPosition(2, 1));
        req.add(new GridPosition(1, 2));
        req.add(new GridPosition(1, 0));
        for (GridPosition r : req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r));
        }
        Assert.assertEquals(req.size(), movableList.size());
    }

    @Test
    public void testObstacleMovableList() {
        /*
         * Map looks like:
         * 0 0 0 0
         * 0 1 1 0
         * 0 A 0 0
         * 0 0 0 1
         */
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        map.getCell(1, 2).setFullEnvironmentObject(BattleMapDomainKt.getSolidFullWall());
        map.getCell(2, 2).setFullEnvironmentObject(BattleMapDomainKt.getSolidFullWall());
        map.getCell(3, 0).setFullEnvironmentObject(BattleMapDomainKt.getSolidFullWall());
        MapGraph graph = new MapGraph(map, new GridPosition(1, 1), 3, 1, 1, false);
        List<GridPosition> movableList = graph.getMovableCellPositions(1);
        ArrayList<GridPosition> req = new ArrayList<>();
        req.add(new GridPosition(0, 0));
        req.add(new GridPosition(0, 1));
        req.add(new GridPosition(0, 2));
        req.add(new GridPosition(0, 3));
        req.add(new GridPosition(1, 0));
        req.add(new GridPosition(1, 3));
        req.add(new GridPosition(2, 0));
        req.add(new GridPosition(2, 1));
        req.add(new GridPosition(3, 1));
        req.add(new GridPosition(3, 2));
        for (GridPosition r : req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r));
        }
        Assert.assertEquals(req.size(), movableList.size());
    }

    @Test
    public void testThreeMobilityMovableList() {
        BattleMap map = BattleMap.Factory.createBattleMap(8, 8);
        MapGraph graph = new MapGraph(map, new GridPosition(3, 3), 3, 1, 1, false);
        List<GridPosition> movableList = graph.getMovableCellPositions(1);
        ArrayList<GridPosition> req = new ArrayList<>();
        req.add(new GridPosition(0, 3));
        req.add(new GridPosition(1, 3));
        req.add(new GridPosition(2, 3));
        req.add(new GridPosition(4, 3));
        req.add(new GridPosition(5, 3));
        req.add(new GridPosition(6, 3));
        req.add(new GridPosition(3, 0));
        req.add(new GridPosition(3, 1));
        req.add(new GridPosition(3, 2));
        req.add(new GridPosition(3, 4));
        req.add(new GridPosition(3, 5));
        req.add(new GridPosition(3, 6));
        req.add(new GridPosition(1, 1));
        req.add(new GridPosition(1, 5));
        req.add(new GridPosition(5, 1));
        req.add(new GridPosition(5, 5));
        for (GridPosition r : req) {
            Assert.assertTrue("Does not contain " + r, movableList.contains(r));
        }
        Assert.assertEquals(28, movableList.size());
    }

    /*
     * Checks to see if the list contains a valid path from start to end
     */
    private static boolean checkPathingList(LinkedList<GridPosition> list, GridPosition start, GridPosition end) {
        if (!list.peekLast().equals(end)) {
            return false;
        }
        GridPosition prev = start;
        for (GridPosition pos : list) {
            if (prev != null) {
                if (Math.abs(prev.getX() - pos.getX()) > 1 || Math.abs(prev.getY() - pos.getY()) > 1) {
                    return false;
                }
            }
            prev = pos;
        }
        return true;
    }

    @Test
    public void testCorrectPathing() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        GridPosition start = new GridPosition(0, 0);
        GridPosition end = new GridPosition(3, 2);
        MapGraph graph = new MapGraph(map, start, 10, 1, 1, false);
        LinkedList<GridPosition> path = graph.getPath(end);
        Assert.assertTrue("Invalid path", checkPathingList(path, start, end));
        Assert.assertEquals(4, path.size());
    }

    @Test
    public void testTooFarPathing() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        GridPosition start = new GridPosition(0, 0);
        GridPosition end = new GridPosition(3, 2);
        MapGraph graph = new MapGraph(map, start, 3, 1, 1, false);
        LinkedList<GridPosition> path = graph.getPath(end);
        Assert.assertEquals(0, path.size());
    }

    @Test
    public void testCannotMoveToNonEmpty() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        map.getCell(3, 0).setFullEnvironmentObject(BattleMapDomainKt.getSolidFullWall());

        BattleUnit battleUnit = BattleUnitFixturesKt.battleUnitFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), 1);
        map.getCell(2, 1).setFullEnvironmentObject(new FullEnvironmentObject.BattleUnitEnvironmentObject(battleUnit));

        MapGraph graph = new MapGraph(map, new GridPosition(1, 1), 10, 1, 1, false);
        Assert.assertFalse("Failed to return false on moving to solid tile", graph.canMoveTo(new GridPosition(3, 0)));
        Assert.assertFalse("Failed to return false on moving to tile with other agent",
                graph.canMoveTo(new GridPosition(2, 1)));
    }

    @Test
    public void testIllegalSquares() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        MapGraph graph = new MapGraph(map, new GridPosition(1, 1), 10, 1, 1, false);
        GridPosition illegal = new GridPosition(4, 1);
        Assert.assertFalse("Failed to return false on moving to tile outside map", graph.canMoveTo(illegal));
        Assert.assertEquals(0, graph.getPath(illegal).size());
    }

    @Test
    public void testNoAP() {
        BattleMap map = BattleMap.Factory.createBattleMap(3, 3);
        MapGraph graph = new MapGraph(map, new GridPosition(1, 1), 10, 0, 1, false);
        GridPosition any = new GridPosition(0, 0);
        Assert.assertFalse("Failed to return false on moving to a tile", graph.canMoveTo(any));
        Assert.assertEquals(0, graph.getPath(any).size());
    }

    @Test
    public void testTwoAP() {
        BattleMap map = BattleMap.Factory.createBattleMap(5, 5);
        MapGraph graph = new MapGraph(map, new GridPosition(2, 2), 2, 2, 2, false);
        List<GridPosition> movableList1 = graph.getMovableCellPositions(1);
        ArrayList<GridPosition> req1 = new ArrayList<>();
        req1.add(new GridPosition(1, 2));
        req1.add(new GridPosition(2, 3));
        req1.add(new GridPosition(2, 1));
        req1.add(new GridPosition(3, 2));
        for (GridPosition r : req1) {
            Assert.assertTrue("Does not contain " + r, movableList1.contains(r));
        }
        Assert.assertEquals(req1.size(), movableList1.size());
        List<GridPosition> movableList2 = graph.getMovableCellPositions(2);
        ArrayList<GridPosition> req2 = new ArrayList<>();
        req2.add(new GridPosition(0, 2));
        req2.add(new GridPosition(1, 3));
        req2.add(new GridPosition(1, 1));
        req2.add(new GridPosition(2, 4));
        req2.add(new GridPosition(2, 0));
        req2.add(new GridPosition(3, 3));
        req2.add(new GridPosition(3, 1));
        req2.add(new GridPosition(4, 2));
        for (GridPosition r : req2) {
            Assert.assertTrue("Does not contain " + r, movableList2.contains(r));
        }
        Assert.assertEquals(req2.size(), movableList2.size());
    }

}
