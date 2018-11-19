package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.CutsceneEvent
import com.pipai.adv.artemis.screens.VillageScreen
import com.pipai.adv.artemis.system.input.CutsceneInputSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.domain.Npc
import com.pipai.adv.generators.CharacterGenerator
import com.pipai.adv.gui.PccPreview
import com.pipai.adv.gui.StandardImageListItemView
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.utils.DirectionUtils
import com.pipai.adv.utils.RNG
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.Subscribe

class OrphanageUiSystem(private val game: AdvGame,
                        private val stage: Stage) : BaseSystem(), InputProcessor {

    private val sCutscene by system<CutsceneInputSystem>()

    private val stateMachine = StackStateMachine<OrphanageUiSystem, OrphanageUiState>(this)

    private val skin = game.skin

    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val doubleColumnTable = Table()
    private val doubleColumnTitle = Label("", game.skin)
    private val leftColumn = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val rightColumn = Table()
    private val rightColumnTitle = Label("", game.skin)
    private val rightColumnList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val rerollBtn = TextButton("  Reroll  ", skin)
    private val recruitBtn = TextButton("  Recruit  ", skin)

    private val pccPreview = PccPreview(listOf(), Direction.E, game.globals.pccManager, skin)
    private var pccPreviewFrameTimer = 0
    private val pccPreviewFrameTimerMax = 15

    private val recruits: MutableList<List<PccMetadata>> = mutableListOf()

    init {
        generateRecruits()
        createTables()
        stateMachine.changeState(OrphanageUiState.SHOWING_MAIN_MENU)
    }

    @Subscribe
    fun handleCutsceneEvent(event: CutsceneEvent) {
        if (event.start) {
            mainTable.isVisible = false
        } else {
            mainTable.isVisible = true
            mainMenuList.setSelectedIndex(0)
        }
    }

    private fun createTables() {
        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        mainTable.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        mainTable.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        mainTable.width = mainMenuWidth
        mainTable.height = mainMenuHeight
        mainTable.background = skin.getDrawable("frameDrawable")

        val menuItems = mutableListOf(
                StringMenuItem("Chat", null, ""),
                StringMenuItem("Recruit", null, ""),
                StringMenuItem("Back", null, ""))

        mainMenuList.setItems(menuItems)
        mainMenuList.hoverSelect = true
        mainMenuList.keySelection = true
        mainMenuList.addConfirmCallback { handleMainMenuConfirm(it) }
        mainTable.add(mainMenuList)
                .width(mainMenuWidth - 20f)
                .left()
                .top()

        val doubleTableHeight = game.advConfig.resolution.height / 2f
        val doubleColumnWidth = game.advConfig.resolution.width / 2f

        doubleColumnTable.x = (game.advConfig.resolution.width - doubleColumnWidth) / 2
        doubleColumnTable.y = (game.advConfig.resolution.height - doubleTableHeight) / 2
        doubleColumnTable.width = doubleColumnWidth
        doubleColumnTable.height = doubleTableHeight
        doubleColumnTable.background = skin.getDrawable("frameDrawable")

        doubleColumnTitle.setText("Recruit")
        doubleColumnTable.add(doubleColumnTitle)
        doubleColumnTable.row()
        doubleColumnTable.add(leftColumn)
        doubleColumnTable.add(rightColumn)
        doubleColumnTable.row()
        doubleColumnTable.add(rerollBtn)
        doubleColumnTable.row()
        doubleColumnTable.add(recruitBtn)

        val rightTitleGroup = Table()
        rightTitleGroup.add(rightColumnTitle).left()
        rightTitleGroup.add(pccPreview).right()
        rightColumn.add(rightTitleGroup)
        rightColumn.row()
        rightColumn.add(rightColumnList)

        leftColumn.hoverSelect = true
        leftColumn.keySelection = true
        leftColumn.addConfirmCallback { initializeRecruitDetails() }
        recruitBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                recruitMember(pccPreview.getPcc())
            }
        })
        rerollBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                generateRecruits()
                populateRecruitList()
                leftColumn.setConfirmIndex(0)
            }
        })
    }

    private fun generateRecruits() {
        recruits.clear()
        val generator = CharacterGenerator()
        repeat(10, {
            val boy = RNG.nextBoolean()
            if (boy) {
                recruits.add(generator.generateSchoolBoy())
            } else {
                recruits.add(generator.generateSchoolGirl())
            }
        })
        recruits.forEach { game.globals.pccManager.loadPccTextures(it) }
    }

    private fun recruitMember(pcc: List<PccMetadata>) {
        val npc = Npc(
                UnitInstance(game.globals.unitSchemaIndex.getSchema("Human").schema, "Orphan"),
                EnvObjTilesetMetadata.PccTilesetMetadata(pcc))
        val npcId = game.globals.save!!.globalNpcList.addNpc(npc)
        val save = game.globals.save!!
        save.addToGuild(save.playerGuild, npcId)
        game.globals.autoSave()
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            "Chat" -> {
                sCutscene.showScene("orphanageOrpheliaChat")
            }
            "Recruit" -> {
                stateMachine.changeState(OrphanageUiState.SHOWING_RECRUITING_LIST)
            }
            "Back" -> {
                game.screen = VillageScreen(game)
            }
        }
    }

    private fun populateRecruitList() {
        leftColumn.setItems(recruits.map {
            StringMenuItem("Orphan", null, "").withData("pcc", it)
        })
        doubleColumnTable.validate()
    }

    private fun initializeRecruitDetails() {
        @Suppress("UNCHECKED_CAST")
        val selectedRecruit = leftColumn.getSelected()!!.getData("pcc") as List<PccMetadata>
        pccPreview.setPcc(selectedRecruit)
        rightColumnTitle.setText("Orphan")
    }

    override fun processSystem() {
        if (isEnabled) {
            pccPreviewFrameTimer++
            if (pccPreviewFrameTimer >= pccPreviewFrameTimerMax) {
                pccPreviewFrameTimer = 0
                pccPreview.incrementFrame()
                if (pccPreview.frame == 0) {
                    pccPreview.direction = DirectionUtils.cwRotation(pccPreview.direction)
                }
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                if (stateMachine.isInState(OrphanageUiState.SHOWING_MAIN_MENU)) {
                    game.screen = VillageScreen(game)
                } else {
                    stateMachine.revertToPreviousState()
                    return true
                }
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

    enum class OrphanageUiState : State<OrphanageUiSystem> {
        SHOWING_MAIN_MENU() {
            override fun enter(uiSystem: OrphanageUiSystem) {
                uiSystem.stage.addActor(uiSystem.mainTable)
                uiSystem.stage.keyboardFocus = uiSystem.mainMenuList
            }
        },
        SHOWING_RECRUITING_LIST() {
            override fun enter(uiSystem: OrphanageUiSystem) {
                uiSystem.populateRecruitList()
                uiSystem.stage.addActor(uiSystem.doubleColumnTable)
                uiSystem.stage.keyboardFocus = uiSystem.leftColumn
            }

            override fun exit(uiSystem: OrphanageUiSystem) {
                uiSystem.doubleColumnTable.remove()
            }
        };

        override fun enter(uiSystem: OrphanageUiSystem) {
        }

        override fun exit(uiSystem: OrphanageUiSystem) {
        }

        override fun onMessage(uiSystem: OrphanageUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: OrphanageUiSystem) {
        }
    }

}
