package com.pipai.adv.backend.battle.domain;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.FullEnvironmentObject.FullWallType;

public class BattleMapTest {

    @Test
    public void testImmutability() {
        BattleMap battleMap = BattleMap.Factory.createBattleMap(2, 2);
        BattleMap mapCopy = battleMap.deepCopy();
        mapCopy.getCell(0, 0).setFullEnvironmentObject(new FullEnvironmentObject.FullWall(FullWallType.SOLID));
        Assert.assertNull(battleMap.getCell(0, 0).getFullEnvironmentObject());
    }

}
