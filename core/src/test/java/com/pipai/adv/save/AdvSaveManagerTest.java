package com.pipai.adv.save;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata;
import com.pipai.adv.backend.battle.domain.UnitInstance;
import com.pipai.adv.backend.battle.domain.UnitStats;
import com.pipai.adv.npc.Npc;
import com.pipai.adv.tiles.PccMetadata;
import com.pipai.test.fixtures.TestFixturesKt;
import com.pipai.test.libgdx.GdxMockedTest;

public class AdvSaveManagerTest extends GdxMockedTest {

    private static final int SAVE_SLOT = 0;
    private static final SaveManager MANAGER = new SaveManager();

    @After
    public void teardown() {
        MANAGER.delete(SAVE_SLOT);
    }

    @Test
    public void testSaveLoad() {
        AdvSave save = new AdvSave();
        save.changePlayerGuildName("Test Guild");

        List<PccMetadata> playerPcc = new ArrayList<>();
        playerPcc.add(new PccMetadata("body", "body_2.png"));
        Npc playerNpc = new Npc(
                new UnitInstance(TestFixturesKt.getSchemaList().getSchema("Human").getSchema(), "Amber"),
                new PccTilesetMetadata(playerPcc));
        save.getGlobalNpcList().addNpc(playerNpc);

        Npc npc = TestFixturesKt.npcFromStats(new UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3));
        save.getGlobalNpcList().addNpc(npc);

        save.addToGuild("Test Guild", 0);

        MANAGER.save(SAVE_SLOT, save);

        AdvSave loadedSave = MANAGER.load(SAVE_SLOT);
        Assert.assertEquals("Test Guild", loadedSave.getPlayerGuild());
        Assert.assertEquals(2, loadedSave.getGlobalNpcList().getNpcs().size());

        Npc npc0 = loadedSave.getGlobalNpcList().getNpc(0);
        Assert.assertEquals(1, ((PccTilesetMetadata) npc0.getTilesetMetadata()).getPccMetadata().size());
        PccMetadata pcc = ((PccTilesetMetadata) npc0.getTilesetMetadata()).getPccMetadata().get(0);
        Assert.assertEquals("body", pcc.getType());
        Assert.assertEquals("body_2.png", pcc.getFilename());

        Assert.assertEquals("Amber", npc0.getUnitInstance().getNickname());
        Assert.assertEquals("Human", npc0.getUnitInstance().getSchema().getName());

        Assert.assertEquals(1, loadedSave.getGuilds().get("Test Guild").size());
        Assert.assertEquals(0, (int) loadedSave.getGuilds().get("Test Guild").get(0));
    }
}
