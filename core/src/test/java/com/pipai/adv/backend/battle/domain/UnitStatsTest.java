package com.pipai.adv.backend.battle.domain;

import org.junit.Assert;
import org.junit.Test;

public class UnitStatsTest {

    @Test
    public void testFactory() {
        UnitStatsFactory factory = new UnitStatsFactory()
                .hpMax(1)
                .tpMax(2)
                .strength(3)
                .dexterity(4)
                .constitution(5)
                .intelligence(6)
                .wisdom(7)
                .avoid(8)
                .mobility(9);

        UnitStats stats = factory.buildUnitStats();
        Assert.assertEquals(stats.getHpMax(), 1);
        Assert.assertEquals(stats.getTpMax(), 2);
        Assert.assertEquals(stats.getStrength(), 3);
        Assert.assertEquals(stats.getDexterity(), 4);
        Assert.assertEquals(stats.getConstitution(), 5);
        Assert.assertEquals(stats.getIntelligence(), 6);
        Assert.assertEquals(stats.getWisdom(), 7);
        Assert.assertEquals(stats.getAvoid(), 8);
        Assert.assertEquals(stats.getMobility(), 9);

        MutableUnitStats mutableStats = factory.buildMutableUnitStats();
        Assert.assertEquals(mutableStats.getHpMax(), 1);
        Assert.assertEquals(mutableStats.getTpMax(), 2);
        Assert.assertEquals(mutableStats.getStrength(), 3);
        Assert.assertEquals(mutableStats.getDexterity(), 4);
        Assert.assertEquals(mutableStats.getConstitution(), 5);
        Assert.assertEquals(mutableStats.getIntelligence(), 6);
        Assert.assertEquals(mutableStats.getWisdom(), 7);
        Assert.assertEquals(mutableStats.getAvoid(), 8);
        Assert.assertEquals(mutableStats.getMobility(), 9);
    }

}
