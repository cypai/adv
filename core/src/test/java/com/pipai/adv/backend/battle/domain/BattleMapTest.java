package com.pipai.adv.backend.battle.domain;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.FullEnvObject.FullWallType;

public class BattleMapTest {

    @Test
    public void testImmutability() {
        BattleMap battleMap = BattleMap.Factory.createBattleMap(2, 2);
        BattleMap mapCopy = battleMap.deepCopy();
        mapCopy.getCell(0, 0).setFullEnvObject(new FullEnvObject.FullWall(FullWallType.SOLID));
        Assert.assertNull(battleMap.getCell(0, 0).getFullEnvObject());
    }

    @Test
    public void testDimensions() {
        BattleMap battleMap = BattleMap.Factory.createBattleMap(2, 3);
        Assert.assertNotNull(battleMap.getCell(0, 0));
        Assert.assertNotNull(battleMap.getCell(1, 0));
        Assert.assertNotNull(battleMap.getCell(1, 2));
        Assert.assertNotNull(battleMap.getCell(0, 2));
    }

}
