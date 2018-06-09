package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.NpcIdComponent
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.classes.ClassTree
import com.pipai.adv.classes.ClassTreeInitializer
import com.pipai.adv.domain.Npc
import com.pipai.adv.domain.UnitSkill
import com.pipai.adv.gui.PccCustomizer
import com.pipai.adv.gui.PccPreview
import com.pipai.adv.gui.StandardImageListItemView
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class GuildManagementUiSystem(private val game: AdvGame,
                              private val stage: Stage) : BaseSystem(), InputProcessor {

    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mNpcId by mapper<NpcIdComponent>()

    private val sEvent by system<EventSystem>()

    private val stateMachine = StackStateMachine<GuildManagementUiSystem, ClassCustomizationUiState>(this)

    private val skin = game.skin

    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val doubleColumnTable = Table()
    private val doubleColumnTitle = Label("", game.skin)
    private val leftColumn = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val rightColumn = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val descriptionLable = Label("", game.skin, "small")

    private val appearanceCustomTable = Table()
    private val appearanceCustomTitle = Label("", game.skin)
    private val appearanceLeftColumn = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val appearanceNameField = TextField("", game.skin)
    private val confirmButton = TextButton("  Confirm  ", game.skin)
    private val pccCustomizer = PccCustomizer(listOf(), game.globals.pccManager, game.skin)

    private var classSelection: ClassTree? = null
    private var skillSelection: UnitSkill? = null

    private var selectedNpcId = 0
    private val pccPreviews: MutableList<PccPreview> = mutableListOf()
    private var pccPreviewFrameTimer = 0
    private val pccPreviewFrameTimerMax = 30

    init {
        stateMachine.setInitialState(ClassCustomizationUiState.DISABLED)
        createForms()
    }

    override fun initialize() {
        isEnabled = false
    }

    fun enable() {
        isEnabled = true
        stateMachine.changeState(ClassCustomizationUiState.SHOWING_MAIN_MENU)
    }

    private fun createForms() {
        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        mainTable.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        mainTable.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        mainTable.width = mainMenuWidth
        mainTable.height = mainMenuHeight
        mainTable.background = skin.getDrawable("frameDrawable")

        mainTable.top().pad(10f)
        mainTable.add(Label("Guild Management", skin))
        mainTable.row()

        val menuItems = mutableListOf(
                StringMenuItem("Change Member Appearance", null, ""),
                StringMenuItem("Assign Class", null, ""),
                StringMenuItem("Assign Skill Points", null, ""),
                StringMenuItem("Cancel", null, ""))

        mainMenuList.setItems(menuItems)
        mainMenuList.addConfirmCallback { handleMainMenuConfirm(it) }
        mainMenuList.hoverSelect = true
        mainMenuList.keySelection = true
        mainTable.add(mainMenuList)
                .width(mainMenuWidth - 20f)
                .left()
        mainTable.validate()

        val doubleTableHeight = game.advConfig.resolution.height / 2f
        val doubleColumnWidth = game.advConfig.resolution.width / 2f

        doubleColumnTable.x = (game.advConfig.resolution.width - doubleColumnWidth) / 2
        doubleColumnTable.y = (game.advConfig.resolution.height - doubleTableHeight) / 2
        doubleColumnTable.width = doubleColumnWidth
        doubleColumnTable.background = skin.getDrawable("frameDrawable")

        doubleColumnTable.add(doubleColumnTitle).padTop(10f)
        doubleColumnTable.row()

        val innerColumnTable = Table()
        innerColumnTable.add(leftColumn)
                .prefWidth(doubleColumnWidth / 2)
                .minHeight(doubleTableHeight * 0.8f)
        innerColumnTable.add(rightColumn)
                .prefWidth(doubleColumnWidth / 2)
                .minHeight(doubleTableHeight * 0.8f)
        doubleColumnTable.add(innerColumnTable)
        doubleColumnTable.row()

        doubleColumnTable.add(descriptionLable)
                .pad(10f)
                .bottom()
                .left()
                .expandX()

        doubleColumnTable.height = doubleColumnTable.prefHeight

        rightColumn.disabledFontColor = Color.GRAY

        val appearanceTableWidth = game.advConfig.resolution.width * 2f / 3f
        val appearanceTableHeight = game.advConfig.resolution.height * 3f / 4f
        appearanceCustomTable.x = (game.advConfig.resolution.width - appearanceTableWidth) / 2
        appearanceCustomTable.y = (game.advConfig.resolution.height - appearanceTableHeight) / 2
        appearanceCustomTable.width = appearanceTableWidth
        appearanceCustomTable.background = skin.getDrawable("frameDrawable")

        appearanceCustomTable.add(appearanceCustomTitle).padTop(10f)
        appearanceCustomTable.row()

        val appearanceRightTable = Table().top().left()
        val nameTable = Table()

        nameTable.add(Label("Name: ", skin)).padLeft(10f).padTop(10f)
        nameTable.add(appearanceNameField)
        nameTable.row()

        appearanceRightTable.add(nameTable).left()
        appearanceRightTable.row()

        pccCustomizer.addChangeListener { pccPreviews.forEach { it.setPcc(pccCustomizer.getPcc()) } }
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
        appearanceRightTable.add(imageTable).left()
        appearanceRightTable.row()
        appearanceRightTable.add(pccCustomizer).pad(10f)
        appearanceRightTable.row()
        appearanceRightTable.add(confirmButton).pad(10f)
        confirmButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                confirmAppearanceChange()
            }
        })
        appearanceRightTable.validate()
        val appearanceRightScrollPane = ScrollPane(appearanceRightTable)
        appearanceCustomTable.add(appearanceLeftColumn)
                .prefWidth(appearanceTableWidth / 2)
                .minHeight(appearanceTableHeight * 0.8f)
        appearanceCustomTable.add(appearanceRightScrollPane)
                .top()
                .prefWidth(doubleColumnWidth)
                .minHeight(appearanceTableHeight * 0.8f)
        appearanceCustomTable.row()

        appearanceCustomTable.height = appearanceCustomTable.prefHeight
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            "Change Member Appearance" -> {
                stateMachine.changeState(ClassCustomizationUiState.SHOWING_APPEARANCE_CUSTOMIZATION)
            }
            "Assign Class" -> {
                stateMachine.changeState(ClassCustomizationUiState.SHOWING_CLASS_ASSIGNMENT)
            }
            "Assign Skill Points" -> {
                stateMachine.changeState(ClassCustomizationUiState.SHOWING_SP_ASSIGNMENT)
            }
            "Cancel" -> {
                stateMachine.revertToPreviousState()
            }
        }
    }

    private fun initializeAppearanceNpcs() {
        val save = game.globals.save!!
        appearanceLeftColumn.setItems(save.guilds[save.playerGuild]!!
                .map {
                    StringMenuItem(
                            save.globalNpcList.getNpc(it)!!.unitInstance.nickname,
                            null,
                            "")
                            .withData("npcId", it)
                })

        appearanceLeftColumn.clearConfirmCallbacks()
        appearanceLeftColumn.addConfirmCallback { initializeAppearanceCustomization(it) }
        appearanceLeftColumn.clearSelection()
    }

    private fun getNpc(): Npc = game.globals.save!!.globalNpcList.getNpc(selectedNpcId)!!

    private fun initializeAppearanceCustomization(selection: StringMenuItem) {
        this.selectedNpcId = selection.getData("npcId") as Int
        val pcc = (getNpc().tilesetMetadata as EnvObjTilesetMetadata.PccTilesetMetadata).pccMetadata
        appearanceNameField.text = getNpc().unitInstance.nickname
        pccCustomizer.setPcc(pcc)
        pccPreviews.forEach { it.setPcc(pcc) }
    }

    private fun confirmAppearanceChange() {
        val npc = getNpc()
        val pcc: List<PccMetadata> = pccCustomizer.getPcc()
        val newNpc = npc.copy(
                unitInstance = npc.unitInstance.copy(nickname = appearanceNameField.text),
                tilesetMetadata = EnvObjTilesetMetadata.PccTilesetMetadata(pcc))
        game.globals.save!!.globalNpcList.setNpc(newNpc, selectedNpcId)
        val targetEntityId = world.fetch(allOf(NpcIdComponent::class, EnvObjTileComponent::class))
                .firstOrNull { mNpcId.get(it).npcId == selectedNpcId }
        targetEntityId?.let {
            val cEnvObjTile = mEnvObjTile.get(it)
            cEnvObjTile.tilesetMetadata = EnvObjTilesetMetadata.PccTilesetMetadata(pcc)
        }
    }

    private fun initializeClassAssignmentNpcs() {
        val save = game.globals.save!!
        leftColumn.setItems(save.guilds[save.playerGuild]!!
                .map {
                    StringMenuItem(
                            save.globalNpcList.getNpc(it)!!.unitInstance.nickname,
                            null,
                            save.classes[it] ?: "Rookie")
                            .withData("npcId", it)
                })

        leftColumn.clearConfirmCallbacks()
        leftColumn.addConfirmCallback { initializeClassList() }
        leftColumn.clearSelection()
        rightColumn.clearItems()
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
        descriptionLable.setText("")
    }

    private fun initializeClassList() {
        rightColumn.setItems(ClassTreeInitializer(game.globals.skillIndex).availableClasses()
                .map {
                    StringMenuItem(it.name, null, "")
                            .withData("class", it)
                })

        if (leftColumn.getSelected().rightText != "Rookie") {
            rightColumn.disableAll()
        }
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
        rightColumn.addConfirmCallback { selectClass(it) }
        descriptionLable.setText("")
    }

    private fun selectClass(selection: StringMenuItem) {
        if (classSelection == selection.getData("class")) {
            showDialog("Are you sure you want to pick ${classSelection!!.name}?",
                    { assignClass(leftColumn.getSelected().getData("npcId") as Int, classSelection!!) },
                    {})
        } else {
            classSelection = selection.getData("class") as ClassTree
            descriptionLable.setText(classSelection!!.description)
        }
    }

    private fun assignClass(npcId: Int, classTree: ClassTree) {
        game.globals.save!!.classes[npcId] = classTree.name
        initializeClassAssignmentNpcs()
    }

    private fun showDialog(text: String, yesCallback: () -> Unit, noCallback: () -> Unit) {
        val dialog = object : Dialog("", game.skin) {
            override fun result(item: Any?) {
                when (item) {
                    true -> yesCallback.invoke()
                    false -> noCallback.invoke()
                }
            }
        }
        dialog.text(text)

        dialog.button("  Yes  ", true)
        dialog.button("  No  ", false)
        dialog.key(Keys.ENTER, true)
        dialog.key(Keys.Z, true)
        dialog.key(Keys.ESCAPE, false)
        dialog.key(Keys.X, false)
        dialog.contentTable.pad(16f)
        dialog.buttonTable.pad(16f)
        dialog.show(stage)
    }

    private fun initializeSkillAssignmentNpcs() {
        val save = game.globals.save!!

        leftColumn.setItems(save.guilds[save.playerGuild]!!
                .map {
                    StringMenuItem(
                            save.globalNpcList.getNpc(it)!!.unitInstance.nickname,
                            null,
                            "${save.classes[it] ?: "Rookie"}/SP: ${save.sp[it]!!}")
                            .withData("npcId", it)
                })

        leftColumn.clearConfirmCallbacks()
        leftColumn.addConfirmCallback { initializeSkillAssignmentList() }
        leftColumn.clearSelection()
        rightColumn.clearItems()
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
        descriptionLable.setText("")
    }

    private fun initializeSkillAssignmentList() {
        val save = game.globals.save!!
        val selectedNpcId = leftColumn.getSelected().getData("npcId") as Int

        val theClass = ClassTreeInitializer(game.globals.skillIndex).generateTree(selectedNpcId, save)
        val availableSkills = theClass.getSkillMap()

        rightColumn.setItems(availableSkills
                .map {
                    StringMenuItem(it.value.skill.name, null, "${it.value.skill.level}")
                            .withData("skill", it.value.skill)
                })
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
        rightColumn.addConfirmCallback { selectSkill(it) }

        if (game.globals.save!!.sp[selectedNpcId]!! == 0) {
            rightColumn.disableAll()
        }
        descriptionLable.setText("")
    }

    private fun selectSkill(selection: StringMenuItem) {
        if (skillSelection == selection.getData("skill")) {
            if (skillSelection!!.level == 0) {
                showDialog("Learn ${skillSelection!!.name}?",
                        { increaseSkill(leftColumn.getSelected().getData("npcId") as Int, skillSelection!!) },
                        {})
            } else {
                showDialog("Improve ${skillSelection!!.name}?",
                        { increaseSkill(leftColumn.getSelected().getData("npcId") as Int, skillSelection!!) },
                        {})
            }
        } else {
            skillSelection = selection.getData("skill") as UnitSkill
            val description = game.globals.skillIndex.getSkillSchema(skillSelection!!.name)!!.description
            descriptionLable.setText(description)
        }
    }

    private fun increaseSkill(npcId: Int, skill: UnitSkill) {
        val npc = game.globals.save!!.globalNpcList.getNpc(npcId)!!
        skill.level += 1
        npc.unitInstance.skills.removeIf { it.name == skill.name }
        npc.unitInstance.skills.add(skill)
        game.globals.save!!.sp[npcId] = game.globals.save!!.sp[npcId]!! - 1
        initializeSkillAssignmentNpcs()
        leftColumn.setSelected(leftColumn.items.find { it.getData("npcId") == npcId }!!)
        initializeSkillAssignmentList()
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
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                if (!stateMachine.isInState(ClassCustomizationUiState.DISABLED)) {
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

    enum class ClassCustomizationUiState : State<GuildManagementUiSystem> {
        DISABLED() {
            override fun enter(uiSystem: GuildManagementUiSystem) {
                uiSystem.mainTable.remove()
                uiSystem.sEvent.dispatch(PauseEvent(false))
                uiSystem.isEnabled = false
            }
        },
        SHOWING_MAIN_MENU() {
            override fun enter(uiSystem: GuildManagementUiSystem) {
                uiSystem.stage.addActor(uiSystem.mainTable)
                uiSystem.stage.keyboardFocus = uiSystem.mainMenuList
                uiSystem.sEvent.dispatch(PauseEvent(true))
            }
        },
        SHOWING_APPEARANCE_CUSTOMIZATION() {
            override fun enter(uiSystem: GuildManagementUiSystem) {
                uiSystem.stage.addActor(uiSystem.appearanceCustomTable)
                uiSystem.appearanceCustomTitle.setText("Change Appearance")
                uiSystem.initializeAppearanceNpcs()
            }

            override fun exit(uiSystem: GuildManagementUiSystem) {
                uiSystem.appearanceCustomTable.remove()
            }
        },
        SHOWING_CLASS_ASSIGNMENT() {
            override fun enter(uiSystem: GuildManagementUiSystem) {
                uiSystem.stage.addActor(uiSystem.doubleColumnTable)
                uiSystem.doubleColumnTitle.setText("Assign Class")
                uiSystem.initializeClassAssignmentNpcs()
            }

            override fun exit(uiSystem: GuildManagementUiSystem) {
                uiSystem.doubleColumnTable.remove()
            }
        },
        SHOWING_SP_ASSIGNMENT() {
            override fun enter(uiSystem: GuildManagementUiSystem) {
                uiSystem.stage.addActor(uiSystem.doubleColumnTable)
                uiSystem.doubleColumnTitle.setText("Assign SP")
                uiSystem.initializeSkillAssignmentNpcs()
            }

            override fun exit(uiSystem: GuildManagementUiSystem) {
                uiSystem.doubleColumnTable.remove()
            }
        };

        override fun enter(uiSystem: GuildManagementUiSystem) {
        }

        override fun exit(uiSystem: GuildManagementUiSystem) {
        }

        override fun onMessage(uiSystem: GuildManagementUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: GuildManagementUiSystem) {
        }
    }

}
