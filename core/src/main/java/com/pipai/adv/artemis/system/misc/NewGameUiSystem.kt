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
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.gui.PccCustomizer
import com.pipai.adv.index.WeaponSchemaIndex
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

    val pccCustomizer: PccCustomizer

    init {
        val defaultPcc: MutableList<PccMetadata> = mutableListOf()
        defaultPcc.add(PccMetadata("body", "body_1.png"))
        defaultPcc.add(PccMetadata("eye", "eye_0.png"))
        defaultPcc.add(PccMetadata("hair", "hair_0.png"))
        defaultPcc.add(PccMetadata("pants", "pants_13.png"))
        defaultPcc.add(PccMetadata("cloth", "cloth_63.png"))
        defaultPcc.add(PccMetadata("etc", "etc_205.png"))

        pccCustomizer = PccCustomizer(defaultPcc, globals.pccManager, game.skin,
                100f, 100f, config.resolution.width / 3f, config.resolution.height / 3f)
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

        nameText = TextField("Len", skin)
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
            val save = generateSave(globals.schemaList, globals.weaponSchemaIndex)
            globals.loadSave(save)
            game.globals.writeSave(0)
            game.screen = GuildScreen(game)
            dispose()
        } else {
            stage.keyDown(keycode)
        }
        return false
    }

    private fun generateSave(schemas: SchemaList, weaponSchemaIndex: WeaponSchemaIndex): AdvSave {
        val save = AdvSave()

        save.changePlayerGuildName(guildText.text)

        val playerPcc: List<PccMetadata> = pccCustomizer.getPcc()
        val playerNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, nameText.text),
                PccTilesetMetadata(playerPcc))
        playerNpc.unitInstance.weapon = InventoryItem.WeaponInstance(weaponSchemaIndex.getWeaponSchema("Toy Sword")!!, 1)
        val playerId = save.globalNpcList.addNpc(playerNpc)
        save.addToGuild(guildText.text, playerId)

        val friendPcc: MutableList<PccMetadata> = mutableListOf()
        friendPcc.add(PccMetadata("body", "body_2.png"))
        val friendNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Amber"),
                PccTilesetMetadata(friendPcc))
        friendNpc.unitInstance.weapon = InventoryItem.WeaponInstance(weaponSchemaIndex.getWeaponSchema("Toy Bow")!!, 1)
        val friendId = save.globalNpcList.addNpc(friendNpc)
        save.addToGuild(guildText.text, friendId)

        val rivalPcc: MutableList<PccMetadata> = mutableListOf()
        rivalPcc.add(PccMetadata("body", "body_1.png"))
        rivalPcc.add(PccMetadata("eye", "eye_0.png"))
        rivalPcc.add(PccMetadata("cloth", "cloth_155.png"))
        rivalPcc.add(PccMetadata("hair", "hair_4.png"))
        rivalPcc.add(PccMetadata("subhair", "subhair_12.png"))
        val rivalNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Miriam"),
                PccTilesetMetadata(rivalPcc))
        rivalNpc.unitInstance.weapon = InventoryItem.WeaponInstance(weaponSchemaIndex.getWeaponSchema("Toy Staff")!!, 1)
        val rivalId = save.globalNpcList.addNpc(rivalNpc)
        save.addToGuild(guildText.text, rivalId)

        val knightPcc: MutableList<PccMetadata> = mutableListOf()
        knightPcc.add(PccMetadata("body", "body_1.png"))
        knightPcc.add(PccMetadata("eye", "eye_0.png"))
        knightPcc.add(PccMetadata("cloth", "cloth_183.png"))
        knightPcc.add(PccMetadata("hair", "hair_4.png"))
        knightPcc.add(PccMetadata("etc", "etc_13.png"))
        knightPcc.add(PccMetadata("subhair", "subhair_58.png"))
        val knightNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Ayra"),
                PccTilesetMetadata(knightPcc))
        knightNpc.unitInstance.weapon = InventoryItem.WeaponInstance(weaponSchemaIndex.getWeaponSchema("Toy Sword")!!, 1)
        val knightId = save.globalNpcList.addNpc(knightNpc)
        save.addToGuild(guildText.text, knightId)

        val medicPcc: MutableList<PccMetadata> = mutableListOf()
        medicPcc.add(PccMetadata("body", "body_1.png"))
        medicPcc.add(PccMetadata("eye", "eye_0.png"))
        medicPcc.add(PccMetadata("pants", "pants_17.png"))
        medicPcc.add(PccMetadata("etc", "etc_268.png"))
        medicPcc.add(PccMetadata("hair", "hair_2.png"))
        medicPcc.add(PccMetadata("subhair", "subhair_12.png"))
        medicPcc.add(PccMetadata("etc", "etc_12.png"))
        medicPcc.add(PccMetadata("etc", "etc_11.png"))
        medicPcc.add(PccMetadata("etc", "etc_81.png"))
        val medicNpc = Npc(UnitInstance(schemas.getSchema("Human").schema, "Nellie"),
                PccTilesetMetadata(medicPcc))
        medicNpc.unitInstance.weapon = InventoryItem.WeaponInstance(weaponSchemaIndex.getWeaponSchema("Toy Bow")!!, 1)
        val medicId = save.globalNpcList.addNpc(medicNpc)
        save.addToGuild(guildText.text, medicId)

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
