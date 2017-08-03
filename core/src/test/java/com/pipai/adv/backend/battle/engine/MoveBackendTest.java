package com.pipai.adv.backend.battle.engine;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.BattleMap;
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata;
import com.pipai.adv.backend.battle.domain.FullEnvObject;
import com.pipai.adv.backend.battle.domain.GridPosition;
import com.pipai.adv.backend.battle.domain.UnitStats;
import com.pipai.adv.npc.Npc;
import com.pipai.adv.npc.NpcList;
import com.pipai.test.fixtures.TestFixturesKt;

public class MoveBackendTest {

    @Test
    public void testMove() {
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(npcList, map);

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
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        map.getCell(3, 1).setFullEnvObject(FullEnvObject.FULL_WALL);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(npcList, map);

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
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(npcList, map);

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
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        int id = npcList.addNpc(npc);
        map.getCell(2, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(id, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(npcList, map);

        MoveCommand cmd = new MoveCommand(id + 1, Arrays.asList(new GridPosition(2, 1), new GridPosition(3, 1)));
        ExecutableStatus executable = backend.canBeExecuted(cmd);
        Assert.assertFalse(executable.getExecutable());
        Assert.assertEquals("Npc does not exist", executable.getReason());
    }
}
