package com.pipai.adv.backend.battle.domain;

import com.pipai.adv.backend.battle.engine.ActionPointState;
import com.pipai.adv.domain.Npc;
import com.pipai.adv.utils.AutoIncrementIdMap;
import com.pipai.test.fixtures.TestFixturesKt;
import org.junit.Assert;
import org.junit.Test;

public class ActionPointStateTest {

    @Test
    public void testNpcIdExists() {
        AutoIncrementIdMap<Npc> npcList = new AutoIncrementIdMap<>();
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.add(npc);
        ActionPointState aps = new ActionPointState(npcList);
        Assert.assertTrue(aps.npcIdExists(id));
    }

    @Test
    public void testGetNpcAp() {
        AutoIncrementIdMap<Npc> npcList = new AutoIncrementIdMap<>();
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.add(npc);
        ActionPointState aps = new ActionPointState(npcList);
        Assert.assertTrue(aps.getNpcAp(id) == 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantReduceApToNegative() {
        AutoIncrementIdMap<Npc> npcList = new AutoIncrementIdMap<>();
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.add(npc);
        ActionPointState aps = new ActionPointState(npcList);
        aps.setNpcAp(id, -3);
    }
}
