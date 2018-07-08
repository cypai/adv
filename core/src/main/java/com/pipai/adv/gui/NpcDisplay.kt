package com.pipai.adv.gui

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.progression.LevelBackend
import com.pipai.adv.domain.NpcList

class NpcDisplay(private val game: AdvGame,
                 private val npcList: NpcList,
                 private var npcId: Int?) : ScrollPane(Table(), game.skin) {

    private val table = this.actor as Table
    private val levelBackend = LevelBackend()

    val pccPreview = PccPreview(listOf(), Direction.S, game.globals.pccManager, game.skin)

    init {
        table.background = game.skin.getDrawable("frameDrawable")
        initializeTableForNpc()
    }

    fun setNpcId(npcId: Int?) {
        this.npcId = npcId
        initializeTableForNpc()
    }

    private fun initializeTableForNpc() {
        table.clear()
        val npcId = this.npcId ?: return
        val npc = npcList.getNpc(npcId)!!
        val tilesetMetadata = npc.tilesetMetadata
        val pcc = if (tilesetMetadata is EnvObjTilesetMetadata.PccTilesetMetadata) {
            tilesetMetadata.pccMetadata
        } else {
            listOf()
        }

        table.pad(16f)
        table.add(Label(npc.unitInstance.nickname, game.skin))
        table.add(pccPreview)
        pccPreview.setPcc(pcc)
        table.row()
        table.add(Label(npc.unitInstance.schema, game.skin, "small"))
        table.row()
        table.add(Label(game.globals.save!!.classes[npcId] ?: "", game.skin, "small"))
        table.row()
        table.add(Label("Level: ${npc.unitInstance.level}", game.skin, "small"))
        table.add(Label("Exp: ${npc.unitInstance.exp}/${levelBackend.expRequired(npc.unitInstance.level)}", game.skin, "small"))
        table.row()
        table.add(Label("HP: ${npc.unitInstance.hp}/${npc.unitInstance.stats.hpMax}", game.skin, "small"))
        table.add(Label("TP: ${npc.unitInstance.tp}/${npc.unitInstance.stats.tpMax}", game.skin, "small"))
        table.row()
        table.add(Label("STR: ${npc.unitInstance.stats.strength}", game.skin, "small"))
        table.add(Label("DEX: ${npc.unitInstance.stats.dexterity}", game.skin, "small"))
        table.row()
        table.add(Label("CON: ${npc.unitInstance.stats.constitution}", game.skin, "small"))
        table.add(Label("AVD: ${npc.unitInstance.stats.avoid}", game.skin, "small"))
        table.row()
        table.add(Label("INT: ${npc.unitInstance.stats.intelligence}", game.skin, "small"))
        table.add(Label("WIS: ${npc.unitInstance.stats.wisdom}", game.skin, "small"))
        table.row()
        table.add(Label("MOB: ${npc.unitInstance.stats.mobility}", game.skin, "small"))
        table.width = table.prefWidth
        table.height = table.prefHeight
    }

}
