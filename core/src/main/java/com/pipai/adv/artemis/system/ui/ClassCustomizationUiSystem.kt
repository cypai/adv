package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.classes.ClassTree
import com.pipai.adv.classes.ClassTreeInitializer
import com.pipai.adv.domain.UnitSkill
import com.pipai.adv.gui.StandardImageListItemView
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class ClassCustomizationUiSystem(private val game: AdvGame,
                                 private val stage: Stage) : NoProcessingSystem(), InputProcessor {

    private val sEvent by system<EventSystem>()

    private val stateMachine = StackStateMachine<ClassCustomizationUiSystem, ClassCustomizationUiState>(this)

    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView())
    private val doubleColumnTable = Table()
    private val doubleColumnTitle = Label("", game.skin)
    private val leftColumn = ImageList(game.skin, "smallMenuList", StandardImageListItemView())
    private val rightColumn = ImageList(game.skin, "smallMenuList", StandardImageListItemView())

    private var classSelection: ClassTree? = null
    private var skillSelection: UnitSkill? = null

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
        val skin = game.skin

        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        mainTable.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        mainTable.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        mainTable.width = mainMenuWidth
        mainTable.height = mainMenuHeight
        mainTable.background = skin.getDrawable("frameDrawable")

        mainTable.top().pad(10f)
        mainTable.add(Label("Class Customization", skin))
        mainTable.row()

        val menuItems = mutableListOf(
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

        val doubleColumnWidth = game.advConfig.resolution.width * 0.8f

        doubleColumnTable.x = (game.advConfig.resolution.width - doubleColumnWidth) / 2
        doubleColumnTable.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        doubleColumnTable.width = doubleColumnWidth
        doubleColumnTable.height = mainMenuHeight
        doubleColumnTable.background = skin.getDrawable("frameDrawable")

        doubleColumnTable.add(doubleColumnTitle).padTop(32f)
        doubleColumnTable.row()

        doubleColumnTable.add(leftColumn)
                .prefWidth(doubleColumnWidth / 4)
                .minHeight(mainMenuHeight)
        doubleColumnTable.add(rightColumn)
                .prefWidth(doubleColumnWidth * 3 / 4)
                .minHeight(mainMenuHeight)

        rightColumn.disabledFontColor = Color.GRAY
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
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

    private fun initializeClassAssignmentNpcs() {
        val save = game.globals.save!!
        leftColumn.setItems(save.guilds[save.playerGuild]!!
                .map {
                    StringMenuItem(
                            save.globalNpcList.getNpc(it)!!.unitInstance.nickname,
                            null,
                            save.classes[it]?.name ?: "Rookie")
                            .withData("npcId", it)
                })

        leftColumn.clearConfirmCallbacks()
        leftColumn.addConfirmCallback { initializeClassList() }
        leftColumn.clearSelection()
        rightColumn.clearItems()
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
    }

    private fun initializeClassList() {
        rightColumn.setItems(ClassTreeInitializer(game.globals.skillIndex).availableClasses()
                .map {
                    StringMenuItem("${it.name}: ${it.description}", null, "")
                            .withData("class", it)
                })

        if (leftColumn.getSelected().rightText != "Rookie") {
            rightColumn.disableAll()
        }
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
        rightColumn.addConfirmCallback { selectClass(it) }
    }

    private fun selectClass(selection: StringMenuItem) {
        if (classSelection == selection.getData("class")) {
            showDialog("Are you sure you want to pick ${classSelection!!.name}?",
                    { assignClass(leftColumn.getSelected().getData("npcId") as Int, classSelection!!) },
                    {})
        } else {
            classSelection = selection.getData("class") as ClassTree
        }
    }

    private fun assignClass(npcId: Int, classTree: ClassTree) {
        game.globals.save!!.classes[npcId] = classTree
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
                            "${save.classes[it]?.name ?: "Rookie"}/SP: ${save.sp[it]!!}")
                            .withData("npcId", it)
                })

        leftColumn.clearConfirmCallbacks()
        leftColumn.addConfirmCallback { initializeSkillAssignmentList() }
        leftColumn.clearSelection()
        rightColumn.clearItems()
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
    }

    private fun initializeSkillAssignmentList() {
        val save = game.globals.save!!
        val selectedNpcId = leftColumn.getSelected().getData("npcId") as Int

        val availableSkills = save.classes[selectedNpcId]?.getSkillMap() ?: mapOf()

        rightColumn.setItems(availableSkills
                .map {
                    StringMenuItem(it.value.skill.schema.name, null, "${it.value.skill.level}")
                            .withData("skill", it.value.skill)
                })
        rightColumn.clearSelection()
        rightColumn.clearConfirmCallbacks()
        rightColumn.addConfirmCallback { selectSkill(it) }
    }

    private fun selectSkill(selection: StringMenuItem) {
        if (skillSelection == selection.getData("skill")) {
            if (skillSelection!!.level == 0) {
                showDialog("Learn ${skillSelection!!.schema.name}?",
                        { increaseSkill(leftColumn.getSelected().getData("npcId") as Int, skillSelection!!) },
                        {})
            } else {
                showDialog("Improve ${skillSelection!!.schema.name}?",
                        { increaseSkill(leftColumn.getSelected().getData("npcId") as Int, skillSelection!!) },
                        {})
            }
        } else {
            skillSelection = selection.getData("skill") as UnitSkill
        }
    }

    private fun increaseSkill(npcId: Int, skill: UnitSkill) {
        val npc = game.globals.save!!.globalNpcList.getNpc(npcId)!!
        skill.level += 1
        npc.unitInstance.skills.removeIf { it.schema.name == skill.schema.name }
        npc.unitInstance.skills.add(skill)
        initializeSkillAssignmentList()
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

    enum class ClassCustomizationUiState : State<ClassCustomizationUiSystem> {
        DISABLED() {
            override fun enter(uiSystem: ClassCustomizationUiSystem) {
                uiSystem.mainTable.remove()
                uiSystem.sEvent.dispatch(PauseEvent(false))
                uiSystem.isEnabled = false
            }
        },
        SHOWING_MAIN_MENU() {
            override fun enter(uiSystem: ClassCustomizationUiSystem) {
                uiSystem.stage.addActor(uiSystem.mainTable)
                uiSystem.stage.keyboardFocus = uiSystem.mainMenuList
                uiSystem.sEvent.dispatch(PauseEvent(true))
            }
        },
        SHOWING_CLASS_ASSIGNMENT() {
            override fun enter(uiSystem: ClassCustomizationUiSystem) {
                uiSystem.stage.addActor(uiSystem.doubleColumnTable)
                uiSystem.doubleColumnTitle.setText("Assign Class")
                uiSystem.initializeClassAssignmentNpcs()
            }

            override fun exit(uiSystem: ClassCustomizationUiSystem) {
                uiSystem.doubleColumnTable.remove()
            }
        },
        SHOWING_SP_ASSIGNMENT() {
            override fun enter(uiSystem: ClassCustomizationUiSystem) {
                uiSystem.stage.addActor(uiSystem.doubleColumnTable)
                uiSystem.doubleColumnTitle.setText("Assign SP")
                uiSystem.initializeSkillAssignmentNpcs()
            }

            override fun exit(uiSystem: ClassCustomizationUiSystem) {
                uiSystem.doubleColumnTable.remove()
            }
        };

        override fun enter(uiSystem: ClassCustomizationUiSystem) {
        }

        override fun exit(uiSystem: ClassCustomizationUiSystem) {
        }

        override fun onMessage(uiSystem: ClassCustomizationUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: ClassCustomizationUiSystem) {
        }
    }

}
