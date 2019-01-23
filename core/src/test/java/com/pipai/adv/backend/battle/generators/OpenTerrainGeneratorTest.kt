package com.pipai.adv.backend.battle.generators

import com.pipai.adv.backend.battle.utils.isExit
import com.pipai.adv.utils.AutoIncrementIdMap
import org.junit.Assert
import org.junit.Test

class OpenTerrainGeneratorTest {

    @Test
    fun testOpenBattleMapGeneration() {
        val generator = OpenTerrainGenerator()
        generator.rocks = 1
        generator.trees = 2

        val map = generator.generate(AutoIncrementIdMap(), 3, 3)
        Assert.assertTrue(isExit(map, 0, 0))
        Assert.assertTrue(isExit(map, 1, 0))
        Assert.assertTrue(isExit(map, 2, 0))
        Assert.assertTrue(isExit(map, 0, 1))
        Assert.assertTrue(isExit(map, 2, 1))
        Assert.assertTrue(isExit(map, 0, 2))
        Assert.assertTrue(isExit(map, 1, 2))
        Assert.assertTrue(isExit(map, 2, 2))
    }

}
