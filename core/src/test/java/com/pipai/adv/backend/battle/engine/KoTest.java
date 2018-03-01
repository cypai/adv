package com.pipai.adv.backend.battle.engine;

import com.pipai.adv.backend.battle.domain.*;
import com.pipai.adv.backend.battle.engine.commands.DevHpChangeCommand;
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent;
import com.pipai.adv.backend.battle.engine.log.DamageEvent;
import com.pipai.adv.backend.battle.engine.log.NpcKoEvent;
import com.pipai.adv.backend.battle.engine.log.PlayerKoEvent;
import com.pipai.adv.npc.Npc;
import com.pipai.adv.npc.NpcList;
import com.pipai.adv.save.AdvSave;
import com.pipai.test.fixtures.TestFixturesKt;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class KoTest {

    @Test
    public void testPlayerKo() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc player = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int playerId = npcList.addNpc(player);
        Npc enemy = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int enemyId = npcList.addNpc(enemy);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell(1, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        DevHpChangeCommand cmd = new DevHpChangeCommand(playerId, 0);

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        List<BattleLogEvent> events = backend.execute(cmd);
        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof DamageEvent));
        DamageEvent damageEvent = (DamageEvent) events.stream().filter(it -> it instanceof DamageEvent).findFirst().get();
        Assert.assertEquals(playerId, damageEvent.getNpcId());
        Assert.assertEquals(0, player.getUnitInstance().getHp());

        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof PlayerKoEvent));
        PlayerKoEvent koEvent = (PlayerKoEvent) events.stream().filter(it -> it instanceof PlayerKoEvent).findFirst().get();
        Assert.assertEquals(playerId, koEvent.getNpcId());

        FullEnvObject envObj = map.getCell(0, 0).getFullEnvObject();
        Assert.assertTrue(envObj instanceof FullEnvObject.NpcEnvObject);
        Assert.assertEquals(playerId, ((FullEnvObject.NpcEnvObject) envObj).getNpcId());
    }

    @Test
    public void testEnemyKo() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        Npc player = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int playerId = npcList.addNpc(player);
        Npc enemy = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int enemyId = npcList.addNpc(enemy);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(playerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell(1, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(enemyId, Team.AI, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        DevHpChangeCommand cmd = new DevHpChangeCommand(enemyId, 0);

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        List<BattleLogEvent> events = backend.execute(cmd);
        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof DamageEvent));
        DamageEvent damageEvent = (DamageEvent) events.stream().filter(it -> it instanceof DamageEvent).findFirst().get();
        Assert.assertEquals(enemyId, damageEvent.getNpcId());
        Assert.assertEquals(0, enemy.getUnitInstance().getHp());

        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof NpcKoEvent));
        NpcKoEvent koEvent = (NpcKoEvent) events.stream().filter(it -> it instanceof NpcKoEvent).findFirst().get();
        Assert.assertEquals(enemyId, koEvent.getNpcId());
        Assert.assertEquals(null, map.getCell(1, 1).getFullEnvObject());
    }
}
