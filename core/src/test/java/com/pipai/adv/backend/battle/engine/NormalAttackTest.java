package com.pipai.adv.backend.battle.engine;

import com.pipai.adv.backend.battle.domain.*;
import com.pipai.adv.npc.Npc;
import com.pipai.adv.npc.NpcList;
import com.pipai.adv.save.AdvSave;
import com.pipai.test.fixtures.TestFixturesKt;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NormalAttackTest {

    @Test
    public void testMeleeNormalAttack() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        InventoryItem.WeaponInstance sword = TestFixturesKt.swordFixture();
        Npc attacker = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword);
        int attackerId = npcList.addNpc(attacker);
        Npc target = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int targetId = npcList.addNpc(target);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell(1, 1).setFullEnvObject(new FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        NormalAttackCommand cmd = new NormalAttackCommand(attackerId, targetId, sword);

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        List<BattleLogEvent> events = backend.execute(cmd);
        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof DamageEvent));
        DamageEvent damageEvent = (DamageEvent) events.stream().filter(it -> it instanceof DamageEvent).findFirst().get();
        Assert.assertEquals(targetId, damageEvent.getNpcId());
        Assert.assertEquals(100 - damageEvent.getDamage(), target.getUnitInstance().getHp());
    }

    @Test
    public void testMeleeNormalAttackOutOfRange() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        InventoryItem.WeaponInstance sword = TestFixturesKt.swordFixture();
        Npc attacker = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword);
        int attackerId = npcList.addNpc(attacker);
        Npc target = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int targetId = npcList.addNpc(target);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell(2, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        NormalAttackCommand cmd = new NormalAttackCommand(attackerId, targetId, sword);

        ExecutableStatus status = backend.canBeExecuted(cmd);
        Assert.assertFalse(status.getExecutable());
        Assert.assertEquals("Attacking distance is too great", status.getReason());
    }

    @Test
    public void testBowRangedNormalAttack() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        InventoryItem.WeaponInstance bow = TestFixturesKt.bowFixture();
        Npc attacker = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                bow);
        int attackerId = npcList.addNpc(attacker);
        Npc target = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int targetId = npcList.addNpc(target);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell(2, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        NormalAttackCommand cmd = new NormalAttackCommand(attackerId, targetId, bow);

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        List<BattleLogEvent> events = backend.execute(cmd);
        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof NormalAttackEvent));
        NormalAttackEvent normalAttackEvent = (NormalAttackEvent) events.stream().filter(it -> it instanceof NormalAttackEvent).findFirst().get();
        Assert.assertEquals(attackerId, normalAttackEvent.getAttackerId());
        Assert.assertEquals(targetId, normalAttackEvent.getTargetId());
        Assert.assertEquals(bow, normalAttackEvent.getWeapon());

        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof DamageEvent));
        DamageEvent damageEvent = (DamageEvent) events.stream().filter(it -> it instanceof DamageEvent).findFirst().get();
        Assert.assertEquals(targetId, damageEvent.getNpcId());
        Assert.assertEquals(100 - damageEvent.getDamage(), target.getUnitInstance().getHp());

        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof AmmoChangeEvent));
        AmmoChangeEvent ammoChangeEvent = (AmmoChangeEvent) events.stream().filter(it -> it instanceof AmmoChangeEvent).findFirst().get();
        Assert.assertEquals(2, ammoChangeEvent.getNewAmount());
        Assert.assertEquals(attackerId, ammoChangeEvent.getNpcId());
        Assert.assertEquals(2, bow.getAmmo());
    }

    @Test
    public void testBowRangedNormalAttackOutOfRange() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap((int) BattleBackend.RANGED_WEAPON_DISTANCE + 1, 1);
        InventoryItem.WeaponInstance sword = TestFixturesKt.swordFixture();
        Npc attacker = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                sword);
        int attackerId = npcList.addNpc(attacker);
        Npc target = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int targetId = npcList.addNpc(target);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell((int) BattleBackend.RANGED_WEAPON_DISTANCE, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        NormalAttackCommand cmd = new NormalAttackCommand(attackerId, targetId, sword);

        ExecutableStatus status = backend.canBeExecuted(cmd);
        Assert.assertFalse(status.getExecutable());
        Assert.assertEquals("Attacking distance is too great", status.getReason());
    }

    @Test
    public void testBowOutOfAmmoNormalAttack() {
        AdvSave save = new AdvSave();
        NpcList npcList = new NpcList();
        BattleMap map = BattleMap.Factory.createBattleMap(4, 4);
        InventoryItem.WeaponInstance bow = TestFixturesKt.bowFixture();
        bow.setAmmo(0);
        Npc attacker = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                bow);
        int attackerId = npcList.addNpc(attacker);
        Npc target = TestFixturesKt.npcFromStats(new UnitStats(100, 1, 1, 1, 1, 1, 1, 1, 3),
                null);
        int targetId = npcList.addNpc(target);
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(attackerId, Team.PLAYER, EnvObjTilesetMetadata.NONE));
        map.getCell(2, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        NormalAttackCommand cmd = new NormalAttackCommand(attackerId, targetId, bow);

        ExecutableStatus status = backend.canBeExecuted(cmd);
        Assert.assertFalse(status.getExecutable());
        Assert.assertEquals("This weapon needs to be reloaded", status.getReason());
    }
}
