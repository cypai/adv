package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.NpcIdComponent
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.domain.Npc
import com.pipai.adv.gui.PccCustomizer
import com.pipai.adv.gui.PccPreview
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.mapper

class CharacterCustomizationUiSystem(private val game: AdvGame, private val stage: Stage) : BaseSystem(), InputProcessor {

    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mNpcId by mapper<NpcIdComponent>()

    private lateinit var table: Table
    private lateinit var nameText: TextField

    private var npcId = 0
    private val pccPreviews: MutableList<PccPreview> = mutableListOf()
    private var pccPreviewFrameTimer = 0
    private val pccPreviewFrameTimerMax = 30

    private val pccCustomizer = PccCustomizer(listOf(), game.globals.pccManager, game.skin)

    init {
        pccCustomizer.addChangeListener { pccPreviews.forEach { it.setPcc(pccCustomizer.getPcc()) } }
        createMainForm()
    }

    fun activate(npcId: Int) {
        this.npcId = npcId
        isEnabled = true
        val pcc = (getNpc().tilesetMetadata as PccTilesetMetadata).pccMetadata
        nameText.text = getNpc().unitInstance.nickname
        pccCustomizer.setPcc(pcc)
        pccPreviews.forEach { it.setPcc(pcc) }
        stage.addActor(table)
    }

    fun disable() {
        table.remove()
        isEnabled = false
    }

    private fun createMainForm() {
        val skin = game.skin

        val formWidth = game.advConfig.resolution.width.toFloat()
        val formHeight = game.advConfig.resolution.height.toFloat()

        table = Table()
        val tablePadding = formHeight / 12

        table.x = tablePadding
        table.y = tablePadding
        table.width = formWidth - 2 * tablePadding
        table.height = formHeight - 2 * tablePadding
        table.background = skin.getDrawable("frameDrawable")
        table.left().top().pad(10f)

        val topTable = Table()

        topTable.add(Label("Name: ", skin)).padLeft(10f).padTop(10f)
        nameText = TextField("Len", skin)
        topTable.add(nameText)
        topTable.row()

        table.add(topTable).left()
        table.row()

        val imageTable = Table()
        pccPreviews.add(PccPreview(pccCustomizer.getPcc(), Direction.S, game.globals.pccManager, skin))
        pccPreviews.add(PccPreview(pccCustomizer.getPcc(), Direction.E, game.globals.pccManager, skin))
        pccPreviews.add(PccPreview(pccCustomizer.getPcc(), Direction.W, game.globals.pccManager, skin))
        pccPreviews.add(PccPreview(pccCustomizer.getPcc(), Direction.N, game.globals.pccManager, skin))
        pccPreviews.forEach {
            imageTable.add(it)
                    .width(PccManager.PCC_WIDTH.toFloat() + 2f)
                    .height(PccManager.PCC_HEIGHT.toFloat() + 2f)
                    .pad(8f)
        }
        table.add(imageTable).left()
        table.row()
        table.add(pccCustomizer).pad(10f)
        table.validate()
    }

    override fun processSystem() {
        if (isEnabled) {
            pccPreviewFrameTimer++
            if (pccPreviewFrameTimer >= pccPreviewFrameTimerMax) {
                pccPreviewFrameTimer = 0
                pccPreviews.forEach {
                    it.incrementFrame()
                }
            }
            stage.act()
            stage.draw()
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (isEnabled) {
            when (keycode) {
                Keys.ENTER -> confirmChange()
                Keys.ESCAPE -> {
                    disable()
                    return true
                }
                else -> stage.keyDown(keycode)
            }
        }
        return false
    }

    private fun confirmChange() {
        val npc = getNpc()
        val pcc: List<PccMetadata> = pccCustomizer.getPcc()
        val newNpc = npc.copy(
                unitInstance = npc.unitInstance.copy(nickname = nameText.text),
                tilesetMetadata = PccTilesetMetadata(pcc))
        game.globals.save!!.globalNpcList.setNpc(newNpc, npcId)
        val targetEntityId = world.fetch(allOf(NpcIdComponent::class, EnvObjTileComponent::class))
                .firstOrNull { mNpcId.get(it).npcId == npcId }
        targetEntityId?.let {
            val cEnvObjTile = mEnvObjTile.get(it)
            cEnvObjTile.tilesetMetadata = PccTilesetMetadata(pcc)
        }
        game.globals.autoSave()

        disable()
    }

    private fun getNpc(): Npc = game.globals.save!!.globalNpcList.getNpc(npcId)!!

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

}
