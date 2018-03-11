package com.pipai.adv.save

import java.util.ArrayList

import org.junit.After
import org.junit.Assert
import org.junit.Test

import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.backend.battle.domain.UnitStats
import com.pipai.adv.npc.Npc
import com.pipai.adv.tiles.PccMetadata
import com.pipai.test.fixtures.*
import com.pipai.test.libgdx.GdxMockedTest

class AdvSaveManagerTest : GdxMockedTest() {

    @After
    fun teardown() {
        MANAGER.delete(SAVE_SLOT)
    }

    @Test
    fun testSaveLoad() {
        val save = AdvSave()
        save.changePlayerGuildName("Test Guild")

        val playerPcc = ArrayList<PccMetadata>()
        playerPcc.add(PccMetadata("body", "body_2.png", null, null))
        val playerNpc = Npc(
                UnitInstance(getSchemaList().getSchema("Human").schema, "Amber"),
                PccTilesetMetadata(playerPcc))
        save.globalNpcList.addNpc(playerNpc)

        val npc = npcFromStats(UnitStats(1, 1, 1, 1, 1, 1, 1, 1, 3), null)
        save.globalNpcList.addNpc(npc)

        save.addToGuild("Test Guild", 0)

        MANAGER.save(SAVE_SLOT, save)

        val loadedSave = MANAGER.load(SAVE_SLOT)
        Assert.assertEquals("Test Guild", loadedSave.playerGuild)
        Assert.assertEquals(2, loadedSave.globalNpcList.getNpcs().size.toLong())

        val npc0 = loadedSave.globalNpcList.getNpc(0)
        Assert.assertEquals(1, (npc0!!.tilesetMetadata as PccTilesetMetadata).pccMetadata.size.toLong())
        val (type, filename) = (npc0.tilesetMetadata as PccTilesetMetadata).pccMetadata[0]
        Assert.assertEquals("body", type)
        Assert.assertEquals("body_2.png", filename)

        Assert.assertEquals("Amber", npc0.unitInstance.nickname)
        Assert.assertEquals("Human", npc0.unitInstance.schema.name)

        Assert.assertEquals(1, loadedSave.guilds["Test Guild"]!!.size.toLong())
        Assert.assertEquals(0, loadedSave.guilds["Test Guild"]!![0].toLong())
    }

    companion object {

        private val SAVE_SLOT = 0
        private val MANAGER = SaveManager()
    }
}
