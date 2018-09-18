package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.domain.Npc
import com.pipai.adv.gui.PccCustomizer
import com.pipai.adv.gui.PccPreview
import com.pipai.adv.index.UnitSchemaIndex
import com.pipai.adv.index.WeaponSchemaIndex
import com.pipai.adv.save.AdvSave
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata

class NewGameUiSystem(private val game: AdvGame) : BaseSystem(), InputProcessor {

    val stage = Stage(ScreenViewport(), game.spriteBatch)
    private lateinit var nameText: TextField
    private lateinit var guildText: TextField

    private val pccPreviews: MutableList<PccPreview> = mutableListOf()
    private var pccPreviewFrameTimer = 0
    private val pccPreviewFrameTimerMax = 30

    val pccCustomizer: PccCustomizer

    init {
        val defaultPcc: MutableList<PccMetadata> = mutableListOf()
        defaultPcc.add(PccMetadata("body", "body_1.png", null, null))
        defaultPcc.add(PccMetadata("eye", "eye_0.png", null, null))
        defaultPcc.add(PccMetadata("hair", "hair_0.png", Color.YELLOW, null))
        defaultPcc.add(PccMetadata("pants", "pants_13.png", null, null))
        defaultPcc.add(PccMetadata("cloth", "cloth_63.png", null, null))
        defaultPcc.add(PccMetadata("etc", "etc_205.png", null, null))

        pccCustomizer = PccCustomizer(defaultPcc, game.globals.pccManager, game.skin)
        pccCustomizer.addChangeListener { pccPreviews.forEach { it.setPcc(pccCustomizer.getPcc()) } }
        createMainForm()
    }

    private fun createMainForm() {
        val skin = game.skin

        val formWidth = game.advConfig.resolution.width.toFloat()
        val formHeight = game.advConfig.resolution.height.toFloat()

        val bgRegion = skin.get("mainMenuBg", Texture::class.java)
        val bg = Image(bgRegion)
        bg.width = formWidth
        bg.height = formHeight
        stage.addActor(bg)

        val newGameLabel = Label("New Game", skin)
        newGameLabel.x = game.font.lineHeight / 2
        newGameLabel.y = formHeight - game.font.lineHeight * 1.25f
        stage.addActor(newGameLabel)

        val table = Table()
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
        topTable.add(Label("Guild: ", skin))
        guildText = TextField("Hakurei", skin)
        topTable.add(guildText)

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

        stage.addActor(table)
    }

    override fun processSystem() {
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

    override fun dispose() {
        stage.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Keys.ENTER) {
            val save = generateSave(game.globals.unitSchemaIndex, game.globals.weaponSchemaIndex)
            game.globals.loadSave(save)
            game.globals.autoSave()
            game.screen = GuildScreen(game)
            dispose()
        } else {
            stage.keyDown(keycode)
        }
        return false
    }

    private fun generateSave(schemas: UnitSchemaIndex, weaponSchemaIndex: WeaponSchemaIndex): AdvSave {
        val save = AdvSave()

        save.changePlayerGuildName(guildText.text)

        val playerPcc: List<PccMetadata> = pccCustomizer.getPcc()
        val playerNpc = Npc(
                UnitInstance(
                        schemas.getSchema("Human").schema,
                        nameText.text,
                        weaponSchemaIndex.getWeaponSchema("Toy Sword")!!),
                PccTilesetMetadata(playerPcc))
        val playerId = save.globalNpcList.addNpc(playerNpc)
        save.addToGuild(guildText.text, playerId)

        val friendPcc: MutableList<PccMetadata> = mutableListOf()
        friendPcc.add(PccMetadata("body", "body_2.png", null, null))
        val friendNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Amber"),
                PccTilesetMetadata(friendPcc))
        friendNpc.unitInstance.weapon = InventoryItem.WeaponInstance("Toy Bow", 1)
        val friendId = save.globalNpcList.addNpc(friendNpc)
        save.addToGuild(guildText.text, friendId)

        val rivalPcc: MutableList<PccMetadata> = mutableListOf()
        rivalPcc.add(PccMetadata("body", "body_1.png", null, null))
        rivalPcc.add(PccMetadata("eye", "eye_0.png", null, null))
        rivalPcc.add(PccMetadata("cloth", "cloth_155.png", null, null))
        rivalPcc.add(PccMetadata("hair", "hair_4.png", Color.valueOf("BA6BFFFF"), null))
        rivalPcc.add(PccMetadata("subhair", "subhair_12.png", Color.valueOf("BA6BFFFF"), null))
        val rivalNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Miriam"),
                PccTilesetMetadata(rivalPcc))
        rivalNpc.unitInstance.weapon = InventoryItem.WeaponInstance("Toy Staff", 1)
        val rivalId = save.globalNpcList.addNpc(rivalNpc)
        save.addToGuild(guildText.text, rivalId)

        val knightPcc: MutableList<PccMetadata> = mutableListOf()
        knightPcc.add(PccMetadata("body", "body_1.png", null, null))
        knightPcc.add(PccMetadata("eye", "eye_0.png", null, null))
        knightPcc.add(PccMetadata("cloth", "cloth_183.png", null, null))
        knightPcc.add(PccMetadata("hair", "hair_4.png", Color.valueOf("F58F22FF"), null))
        knightPcc.add(PccMetadata("etc", "etc_13.png", null, null))
        knightPcc.add(PccMetadata("subhair", "subhair_58.png", Color.valueOf("F58F22FF"), null))
        val knightNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Ayra"),
                PccTilesetMetadata(knightPcc))
        knightNpc.unitInstance.weapon = InventoryItem.WeaponInstance("Toy Sword", 1)
        val knightId = save.globalNpcList.addNpc(knightNpc)
        save.addToGuild(guildText.text, knightId)

        val medicPcc: MutableList<PccMetadata> = mutableListOf()
        medicPcc.add(PccMetadata("body", "body_1.png", null, null))
        medicPcc.add(PccMetadata("eye", "eye_0.png", null, null))
        medicPcc.add(PccMetadata("pants", "pants_17.png", null, null))
        medicPcc.add(PccMetadata("etc", "etc_268.png", null, null))
        medicPcc.add(PccMetadata("hair", "hair_2.png", Color.valueOf("FFA8CCFF"), null))
        medicPcc.add(PccMetadata("subhair", "subhair_12.png", Color.valueOf("FFA8CCFF"), null))
        medicPcc.add(PccMetadata("etc", "etc_12.png", null, null))
        medicPcc.add(PccMetadata("etc", "etc_11.png", null, null))
        medicPcc.add(PccMetadata("etc", "etc_81.png", null, null))
        val medicNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Nellie"),
                PccTilesetMetadata(medicPcc))
        medicNpc.unitInstance.weapon = InventoryItem.WeaponInstance("Toy Bow", 1)
        val medicId = save.globalNpcList.addNpc(medicNpc)
        save.addToGuild(guildText.text, medicId)

        val squadName = "Squad 1"
        save.setSquad(squadName, listOf(playerId, friendId, rivalId, knightId, medicId))
        save.squadLocations[squadName] = game.globals.worldMap.villageLocations["Lagos Village"]!!

        game.globals.progressionBackend.scheduleEvents(save)

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
