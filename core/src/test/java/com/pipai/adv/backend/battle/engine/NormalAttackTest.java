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
        map.getCell(0, 0).setFullEnvObject(new FullEnvObject.NpcEnvObject(targetId, Team.PLAYER, EnvObjTilesetMetadata.NONE));

        BattleBackend backend = new BattleBackend(save, npcList, map);

        NormalAttackCommand cmd = new NormalAttackCommand(attackerId, targetId, sword);

        Assert.assertTrue(backend.canBeExecuted(cmd).getExecutable());

        List<BattleLogEvent> events = backend.execute(cmd);
        Assert.assertTrue(events.stream().anyMatch(it -> it instanceof DamageEvent));
        DamageEvent damageEvent = (DamageEvent) events.stream().filter(it -> it instanceof DamageEvent).findFirst().get();
        Assert.assertEquals(targetId, damageEvent.getNpcId());
        Assert.assertEquals(100 - damageEvent.getDamage(), target.getUnitInstance().getHp());
    }
}
