package com.pipai.adv.backend.battle.generators

import org.junit.Assert
import org.junit.Test

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.utils.*

class OpenBattleMapGeneratorTest {

    @Test
    fun testOpenBattleMapGeneration() {
        val generator = OpenBattleMapGenerator()
        generator.rocks = 1
        generator.trees = 2

        val map = generator.generate(3, 3)
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
