package com.pipai.adv.backend.battle.domain;

import com.pipai.adv.utils.AutoIncrementIdMap;
import org.junit.Assert;
import org.junit.Test;

public class BattleMapTest {

    @Test
    public void testImmutability() {
        AutoIncrementIdMap<EnvObject> envObjList = new AutoIncrementIdMap<>();
        BattleMap battleMap = BattleMap.Factory.createBattleMap(2, 2);
        BattleMap mapCopy = battleMap.deepCopy();
        mapCopy.getCell(0, 0).setFullEnvObjId(envObjList.add(new FullWall(FullWallType.SOLID)));
        Assert.assertNull(battleMap.getCell(0, 0).getFullEnvObjId());
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
