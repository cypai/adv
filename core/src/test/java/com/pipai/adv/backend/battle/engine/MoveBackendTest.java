package com.pipai.adv.backend.battle.engine;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.BattleMap;
import com.pipai.adv.backend.battle.domain.BattleMapDomainKt;
import com.pipai.adv.backend.battle.domain.BattleUnit;
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject;
import com.pipai.adv.backend.battle.domain.GridPosition;
import com.pipai.adv.backend.battle.domain.UnitStats;
import com.pipai.test.fixtures.BattleUnitFixturesKt;

public class MoveBackendTest {

    @Test
    public void testMove() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        BattleUnit battleUnit = BattleUnitFixturesKt.battleUnitFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), 1);
        map.getCell(2, 1).setFullEnvironmentObject(new FullEnvironmentObject.BattleUnitEnvironmentObject(battleUnit));

        BattleBackend backend = new BattleBackend(map);

        MoveCommand cmd = new MoveCommand(1, Arrays.asList(
                new GridPosition(2, 1),
                new GridPosition(3, 1)));

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        backend.execute(cmd);

        GridPosition expectedDestination = new GridPosition(3, 1);
        Assert.assertEquals(expectedDestination, backend.getBattleUnitPositions().get(1));

        FullEnvironmentObject destinationObj = backend.getBattleMapState().getCell(expectedDestination).getFullEnvironmentObject();

        Assert.assertTrue(destinationObj instanceof FullEnvironmentObject.BattleUnitEnvironmentObject);

        FullEnvironmentObject.BattleUnitEnvironmentObject unit = (FullEnvironmentObject.BattleUnitEnvironmentObject) destinationObj;

        Assert.assertEquals(1, unit.getBattleUnit().getId());
    }

    @Test
    public void testBadMove() {
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        map.getCell(3, 1).setFullEnvironmentObject(BattleMapDomainKt.getSolidFullWall());
        BattleUnit battleUnit = BattleUnitFixturesKt.battleUnitFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), 1);
        map.getCell(2, 1).setFullEnvironmentObject(new FullEnvironmentObject.BattleUnitEnvironmentObject(battleUnit));

        BattleBackend backend = new BattleBackend(map);

        MoveCommand cmd = new MoveCommand(1, Arrays.asList(
                new GridPosition(2, 1),
                new GridPosition(3, 1)));

        ExecutableStatus executable = backend.canBeExecuted(cmd);
        Assert.assertFalse(executable.getExecutable());

        try {
            backend.execute(cmd);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().endsWith(executable.getReason()));
        }

        GridPosition expectedLocation = new GridPosition(2, 1);
        Assert.assertEquals(expectedLocation, backend.getBattleUnitPositions().get(1));

        FullEnvironmentObject destinationObj = backend.getBattleMapState().getCell(expectedLocation).getFullEnvironmentObject();

        Assert.assertTrue(destinationObj instanceof FullEnvironmentObject.BattleUnitEnvironmentObject);

        FullEnvironmentObject.BattleUnitEnvironmentObject unit = (FullEnvironmentObject.BattleUnitEnvironmentObject) destinationObj;

        Assert.assertEquals(1, unit.getBattleUnit().getId());
    }

}
