package com.pipai.adv.artemis.system.misc

import com.artemis.BaseSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGame
import com.pipai.adv.AdvGameGlobals
import com.pipai.adv.SchemaList
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.gui.PccCustomizer
import com.pipai.adv.npc.Npc
import com.pipai.adv.save.AdvSave
import com.pipai.adv.tiles.PccMetadata

class NewGameUiSystem(private val game: AdvGame,
                      private val config: AdvConfig,
                      private val globals: AdvGameGlobals) : BaseSystem(), InputProcessor {

    private val batch = game.batchHelper

    val stage = Stage(ScreenViewport())
    private val table = Table()
    private lateinit var nameText: TextField
    private lateinit var guildText: TextField

    val pccCustomizer = PccCustomizer(globals.pccManager, game.skin,
            100f, 100f, config.resolution.width / 3f, config.resolution.height / 3f)

    init {
        createMainForm()
    }

    private fun createMainForm() {
        val skin = game.skin

        val x = 0
        val y = 0

        val formWidth = game.advConfig.resolution.width.toFloat()
        val formHeight = game.advConfig.resolution.height.toFloat()

        val bgRegion = skin.get("mainMenuBg", Texture::class.java)
        val bg = Image(bgRegion)
        bg.width = formWidth
        bg.height = formHeight
        bg.x = x.toFloat()
        bg.y = y.toFloat()
        stage.addActor(bg)

        val newGameLabel = Label("New Game", skin)
        newGameLabel.x = game.font.lineHeight / 2
        newGameLabel.y = formHeight - game.font.lineHeight * 1.25f
        stage.addActor(newGameLabel)

        val tablePadding = formHeight / 12

        table.x = x.toFloat() + tablePadding
        table.y = y.toFloat() + tablePadding
        table.width = formWidth - 2 * tablePadding
        table.height = formHeight - 2 * tablePadding
        table.left().top()

        val uiBgTexture = skin.getRegion("bg")
        uiBgTexture.setRegion(0, 0, table.width.toInt(), table.height.toInt())
        val uiBg = Image(uiBgTexture)
        uiBg.x = table.x
        uiBg.y = table.y
        uiBg.width = table.width
        uiBg.height = table.height
        stage.addActor(uiBg)

        val framePatch = skin.getPatch("frame")
        framePatch.topHeight = 2f
        val frame = Image(framePatch)
        frame.x = (table.x - 1).toFloat()
        frame.y = (table.y - 3).toFloat()
        frame.width = table.width + 4
        frame.height = table.height + 4
        stage.addActor(frame)

        val nameLabel = Label("Name: ", skin)
        table.add(nameLabel).padLeft(10f).padTop(10f)

        nameText = TextField("Amber", skin)
        table.add(nameText)
        table.row()

        val guildLabel = Label("Guild: ", skin)
        table.add(guildLabel)

        guildText = TextField("Hakurei", skin)
        table.add(guildText)

        stage.addActor(table)
    }

    override fun processSystem() {
        stage.act()
        stage.draw()
        pccCustomizer.render()
    }

    override fun dispose() {
        stage.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Keys.ENTER) {
            val save = generateSave(globals.schemaList)
            globals.loadSave(save)
            game.globals.writeSave(0)
            game.screen = GuildScreen(game)
            dispose()
        } else {
            stage.keyDown(keycode)
        }
        return false
    }

    private fun generateSave(schemas: SchemaList): AdvSave {
        val save = AdvSave()

        save.changePlayerGuildName(guildText.text)

        val playerPcc: MutableList<PccMetadata> = mutableListOf()
        playerPcc.add(PccMetadata("body", "body_2.png"))
        val playerNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, nameText.text),
                PccTilesetMetadata(playerPcc))
        val playerId = save.globalNpcList.addNpc(playerNpc)
        save.addToGuild(guildText.text, playerId)

        val friendPcc: MutableList<PccMetadata> = mutableListOf()
        friendPcc.add(PccMetadata("body", "body_1.png"))
        friendPcc.add(PccMetadata("eye", "eye_7.png"))
        friendPcc.add(PccMetadata("hair", "hair_0.png"))
        friendPcc.add(PccMetadata("pants", "pants_13.png"))
        friendPcc.add(PccMetadata("cloth", "cloth_63.png"))
        friendPcc.add(PccMetadata("etc", "etc_205.png"))
        val friendNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Len"),
                PccTilesetMetadata(friendPcc))
        val friendId = save.globalNpcList.addNpc(friendNpc)
        save.addToGuild(guildText.text, friendId)

        return save
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        stage.keyboardFocus = null
        pccCustomizer.stage.keyboardFocus = null
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

}
