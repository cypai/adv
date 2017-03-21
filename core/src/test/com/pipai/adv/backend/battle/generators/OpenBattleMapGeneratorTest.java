package com.pipai.adv.backend.battle.generators;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.BattleMap;
import com.pipai.adv.backend.battle.utils.BattleMapUtilsKt;

public class OpenBattleMapGeneratorTest {

    @Test
    public void testOpenBattleMapGeneration() {
        OpenBattleMapGenerator generator = new OpenBattleMapGenerator();
        generator.setRocks(1);
        generator.setTrees(2);

        BattleMap map = generator.generate(3, 3);
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 0, 0));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 1, 0));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 2, 0));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 0, 1));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 2, 1));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 0, 2));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 1, 2));
        Assert.assertTrue(BattleMapUtilsKt.isExit(map, 2, 2));
    }

}
