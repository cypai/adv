package com.pipai.adv.utils;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.Direction;

public class DirectionUtilsTest {

    @Test
    public void testDirectionFor() {
        Assert.assertEquals(Direction.N, DirectionUtils.INSTANCE.directionFor(0, 0, 0, 1));
        Assert.assertEquals(Direction.S, DirectionUtils.INSTANCE.directionFor(0, 0, 0, -1));
        Assert.assertEquals(Direction.E, DirectionUtils.INSTANCE.directionFor(0, 0, 1, 0));
        Assert.assertEquals(Direction.W, DirectionUtils.INSTANCE.directionFor(0, 0, -1, 0));
        Assert.assertEquals(Direction.NE, DirectionUtils.INSTANCE.directionFor(0, 0, 1, 1));
        Assert.assertEquals(Direction.NW, DirectionUtils.INSTANCE.directionFor(0, 0, -1, 1));
        Assert.assertEquals(Direction.SE, DirectionUtils.INSTANCE.directionFor(0, 0, 1, -1));
        Assert.assertEquals(Direction.SW, DirectionUtils.INSTANCE.directionFor(0, 0, -1, -1));
    }

}
