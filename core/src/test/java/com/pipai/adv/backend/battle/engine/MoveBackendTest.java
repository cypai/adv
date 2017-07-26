package com.pipai.adv.backend.battle.engine;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.BattleMap;
import com.pipai.adv.backend.battle.domain.BattleMapDomainKt;
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject;
import com.pipai.adv.backend.battle.domain.GridPosition;
import com.pipai.adv.backend.battle.domain.UnitStats;
import com.pipai.adv.save.Npc;
import com.pipai.adv.save.NpcList;
import com.pipai.test.fixtures.TestFixturesKt;

public class MoveBackendTest {

    @Test
    public void testMove() {
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvironmentObject(new FullEnvironmentObject.NpcEnvironmentObject(id));

        BattleBackend backend = new BattleBackend(npcList, map);

        MoveCommand cmd = new MoveCommand(1, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        backend.execute(cmd);

        GridPosition expectedDestination = new GridPosition(3, 1);
        Assert.assertEquals(expectedDestination, backend.getNpcPositions().get(id));

        FullEnvironmentObject destinationObj = backend.getBattleMapState().getCell(expectedDestination)
                .getFullEnvironmentObject();

        Assert.assertTrue(destinationObj instanceof FullEnvironmentObject.NpcEnvironmentObject);

        FullEnvironmentObject.NpcEnvironmentObject unit = (FullEnvironmentObject.NpcEnvironmentObject) destinationObj;

        Assert.assertEquals(id, unit.getNpcId());
    }

    @Test
    public void testBadMove() {
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        map.getCell(3, 1).setFullEnvironmentObject(BattleMapDomainKt.getSolidFullWall());
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvironmentObject(new FullEnvironmentObject.NpcEnvironmentObject(id));

        BattleBackend backend = new BattleBackend(npcList, map);

        MoveCommand cmd = new MoveCommand(1, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));

        ExecutableStatus executable = backend.canBeExecuted(cmd);
        Assert.assertFalse(executable.getExecutable());

        try {
            backend.execute(cmd);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().endsWith(executable.getReason()));
        }

        GridPosition expectedLocation = new GridPosition(2, 1);
        Assert.assertEquals(expectedLocation, backend.getNpcPositions().get(id));

        FullEnvironmentObject destinationObj = backend.getBattleMapState().getCell(expectedLocation)
                .getFullEnvironmentObject();

        Assert.assertTrue(destinationObj instanceof FullEnvironmentObject.NpcEnvironmentObject);

        FullEnvironmentObject.NpcEnvironmentObject unit = (FullEnvironmentObject.NpcEnvironmentObject) destinationObj;

        Assert.assertEquals(id, unit.getNpcId());
    }

}
