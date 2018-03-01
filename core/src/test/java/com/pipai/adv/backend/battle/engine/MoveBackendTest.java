package com.pipai.adv.backend.battle.engine;

import com.pipai.adv.backend.battle.domain.*;
import com.pipai.adv.npc.Npc;
import com.pipai.adv.npc.NpcList;
import com.pipai.adv.save.AdvSave;
import com.pipai.test.fixtures.TestFixturesKt;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class MoveBackendTest {

    @Test
    public void testMove() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        MoveCommand cmd = new MoveCommand(id, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        backend.execute(cmd);

        GridPosition expectedDestination = new GridPosition(3, 1);
        Assert.assertEquals(expectedDestination, backend.getNpcPositions().get(id));

        FullEnvObject destinationObj = backend.getBattleMapState().getCell(expectedDestination).getFullEnvObject();

        Assert.assertTrue(destinationObj instanceof FullEnvObject.NpcEnvObject);

        FullEnvObject.NpcEnvObject unit = (FullEnvObject.NpcEnvObject) destinationObj;

        Assert.assertEquals(id, unit.getNpcId());
    }

    @Test
    public void testCantMoveToFullSpace() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        map.getCell(3, 1).setFullEnvObject(FullEnvObject.FULL_WALL);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        MoveCommand cmd = new MoveCommand(id, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));

        ExecutableStatus executable = backend.canBeExecuted(cmd);
        Assert.assertFalse(executable.getExecutable());
        Assert.assertEquals("Destination is not empty", executable.getReason());

        try {
            backend.execute(cmd);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().endsWith(executable.getReason()));
        }

        GridPosition expectedLocation = new GridPosition(2, 1);
        Assert.assertEquals(expectedLocation, backend.getNpcPositions().get(id));

        FullEnvObject destinationObj = backend.getBattleMapState().getCell(expectedLocation).getFullEnvObject();

        Assert.assertTrue(destinationObj instanceof FullEnvObject.NpcEnvObject);

        FullEnvObject.NpcEnvObject unit = (FullEnvObject.NpcEnvObject) destinationObj;

        Assert.assertEquals(id, unit.getNpcId());
    }

    @Test
    public void testCantMoveMoreThanApAllow() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        MoveCommand cmd = new MoveCommand(id, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));
        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());
        backend.execute(cmd);

        cmd = new MoveCommand(id, Arrays.asList(new GridPosition(3, 1), new GridPosition(3, 2)));
        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());
        backend.execute(cmd);

        cmd = new MoveCommand(id, Arrays.asList(new GridPosition(3, 2), new GridPosition(3, 3)));
        ExecutableStatus executable = backend.canBeExecuted(cmd);
        Assert.assertFalse(executable.getExecutable());
        Assert.assertEquals("Not enough action points available", executable.getReason());
    }

    @Test
    public void testCantMoveNonexistentNpc() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null);
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        int badId = id + 1;
        MoveCommand cmd = new MoveCommand(badId, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));
        ExecutableStatus executable = backend.canBeExecuted(cmd);
        Assert.assertFalse(executable.getExecutable());
        Assert.assertEquals("Npc " + badId + " does not exist", executable.getReason());
    }
}
