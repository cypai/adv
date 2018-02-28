package com.pipai.adv.backend.battle.domain;

import com.pipai.adv.backend.battle.engine.ActionPointState;
import com.pipai.adv.npc.Npc;
import com.pipai.adv.npc.NpcList;
import com.pipai.test.fixtures.TestFixturesKt;
import org.junit.Assert;
import org.junit.Test;

public class ActionPointStateTest {

    @Test
    public void testNpcIdExists() {
        NpcList npcList = new NpcList();
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        ActionPointState aps = new ActionPointState(npcList);
        Assert.assertTrue(aps.npcIdExists(id));
    }

    @Test
    public void testGetNpcAp() {
        NpcList npcList = new NpcList();
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        ActionPointState aps = new ActionPointState(npcList);
        Assert.assertTrue(aps.getNpcAp(id) == 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCantReduceApToNegative() {
        NpcList npcList = new NpcList();
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        ActionPointState aps = new ActionPointState(npcList);
        aps.setNpcAp(id, -3);
    }
}
