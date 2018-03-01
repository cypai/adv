package com.pipai.adv.utils

import org.junit.Assert
import org.junit.Test

import com.pipai.adv.backend.battle.domain.Direction

class DirectionUtilsTest {

    @Test
    fun testDirectionFor() {
        Assert.assertEquals(Direction.N, DirectionUtils.directionFor(0f, 0f, 0f, 1f))
        Assert.assertEquals(Direction.S, DirectionUtils.directionFor(0f, 0f, 0f, -1f))
        Assert.assertEquals(Direction.E, DirectionUtils.directionFor(0f, 0f, 1f, 0f))
        Assert.assertEquals(Direction.W, DirectionUtils.directionFor(0f, 0f, -1f, 0f))
        Assert.assertEquals(Direction.NE, DirectionUtils.directionFor(0f, 0f, 1f, 1f))
        Assert.assertEquals(Direction.NW, DirectionUtils.directionFor(0f, 0f, -1f, 1f))
        Assert.assertEquals(Direction.SE, DirectionUtils.directionFor(0f, 0f, 1f, -1f))
        Assert.assertEquals(Direction.SW, DirectionUtils.directionFor(0f, 0f, -1f, -1f))
    }

}
