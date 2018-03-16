package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.screens.MainMenuScreen
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.gui.LoadGameDisplay
import com.pipai.adv.gui.SaveGameDisplay
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class PauseUiSystem(private val game: AdvGame, private val stage: Stage) : NoProcessingSystem(), InputProcessor {

    private val sEvent by system<EventSystem>()

    private val stateMachine = StackStateMachine<PauseUiSystem, PauseUiState>(this)

    private val table = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", object : ImageList.ImageListItemView<StringMenuItem> {
        override fun getItemImage(item: StringMenuItem): TextureRegion? = null
        override fun getItemText(item: StringMenuItem): String = item.text
        override fun getItemRightText(item: StringMenuItem): String = item.rightText
        override fun getSpacing(): Float = 10f
    })
    private val saveGameDisplay = SaveGameDisplay(game)
    private val loadGameDisplay = LoadGameDisplay(game)

    init {
        stateMachine.setInitialState(PauseUiState.DISABLED)
        createMainForm()
    }

    override fun initialize() {
        isEnabled = false
    }

    private fun createMainForm() {
        val skin = game.skin

        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        table.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        table.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        table.width = mainMenuWidth
        table.height = mainMenuHeight
        table.background = skin.getDrawable("frameDrawable")

        table.top().pad(10f)
        table.add(Label("Pause", skin))
        table.row()

        mainMenuList.setItems(listOf(
                StringMenuItem("Save Game", null, ""),
                StringMenuItem("Load Game", null, ""),
                StringMenuItem("Options", null, ""),
                StringMenuItem("Quit to Main Menu", null, ""),
                StringMenuItem("Quit Game", null, "")))
        mainMenuList.addConfirmCallback { handleMainMenuConfirm(it) }
        mainMenuList.hoverSelect = true
        mainMenuList.keySelection = true
        table.add(mainMenuList)
                .width(mainMenuWidth - 20f)
                .left()
        table.validate()
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            "Save Game" -> {
                stateMachine.changeState(PauseUiState.SHOWING_SAVE_MENU)
            }
            "Load Game" -> {
                stateMachine.changeState(PauseUiState.SHOWING_LOAD_MENU)
            }
            "Quit to Main Menu" -> {
                showDialog("Are you sure you want to quit?",
                        {
                            val currentScreen = game.screen
                            game.screen = MainMenuScreen(game)
                            currentScreen.dispose()
                        },
                        {})
            }
            "Quit Game" -> {
                showDialog("Are you sure you want to quit?", { Gdx.app.exit() }, {})
            }
        }
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

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                if (stateMachine.isInState(PauseUiState.DISABLED)) {
                    stateMachine.changeState(PauseUiState.SHOWING_MAIN_MENU)
                } else {
                    stateMachine.revertToPreviousState()
                }
                return true
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

    enum class PauseUiState : State<PauseUiSystem> {
        DISABLED() {
            override fun enter(uiSystem: PauseUiSystem) {
                uiSystem.table.remove()
                uiSystem.sEvent.dispatch(PauseEvent(false))
            }
        },
        SHOWING_MAIN_MENU() {
            override fun enter(uiSystem: PauseUiSystem) {
                uiSystem.stage.addActor(uiSystem.table)
                uiSystem.stage.keyboardFocus = uiSystem.mainMenuList
                uiSystem.sEvent.dispatch(PauseEvent(true))
            }
        },
        SHOWING_SAVE_MENU() {
            override fun enter(uiSystem: PauseUiSystem) {
                uiSystem.saveGameDisplay.show(uiSystem.stage)
            }

            override fun exit(uiSystem: PauseUiSystem) {
                uiSystem.saveGameDisplay.remove()
            }
        },
        SHOWING_LOAD_MENU() {
            override fun enter(uiSystem: PauseUiSystem) {
                uiSystem.loadGameDisplay.show(uiSystem.stage)
            }

            override fun exit(uiSystem: PauseUiSystem) {
                uiSystem.loadGameDisplay.remove()
            }
        };

        override fun enter(uiSystem: PauseUiSystem) {
        }

        override fun exit(uiSystem: PauseUiSystem) {
        }

        override fun onMessage(uiSystem: PauseUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: PauseUiSystem) {
        }
    }

}
