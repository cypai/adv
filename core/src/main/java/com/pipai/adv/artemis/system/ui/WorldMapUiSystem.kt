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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.gui.StandardImageListItemView
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class WorldMapUiSystem(private val game: AdvGame,
                       private val stage: Stage) : BaseSystem(), InputProcessor {

    private val sEvent by system<EventSystem>()

    private val stateMachine = StackStateMachine<WorldMapUiSystem, WorldMapUiState>(this)

    private val skin = game.skin

    private val screenTable = Table()
    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())
    private val runTimeButton = TextButton("  Pass Time  ", skin)

    init {
        stateMachine.setInitialState(WorldMapUiState.DISABLED)
        createUi()
    }

    private fun createUi() {
        screenTable.x = 0f
        screenTable.y = 0f
        screenTable.width = game.advConfig.resolution.width.toFloat()
        screenTable.height = game.advConfig.resolution.height.toFloat()
        screenTable.add(runTimeButton)
                .expand()
                .top()
                .padTop(32f)

        stage.addActor(screenTable)

        runTimeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (stateMachine.isInState(WorldMapUiState.DISABLED)) {
                    stateMachine.changeState(WorldMapUiState.RUNNING_TIME)
                }
            }
        })
    }

    override fun processSystem() {
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                if (!stateMachine.isInState(WorldMapUiState.DISABLED)) {
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

    enum class WorldMapUiState : State<WorldMapUiSystem> {
        DISABLED() {
            override fun enter(uiSystem: WorldMapUiSystem) {
                uiSystem.mainTable.remove()
                uiSystem.sEvent.dispatch(PauseEvent(false))
            }
        },
        SELECTED_SQUAD() {
            override fun enter(uiSystem: WorldMapUiSystem) {
                uiSystem.stage.addActor(uiSystem.mainTable)
                uiSystem.stage.keyboardFocus = uiSystem.mainMenuList
            }
        },
        RUNNING_TIME() {
            override fun enter(uiSystem: WorldMapUiSystem) {
            }
        };

        override fun enter(uiSystem: WorldMapUiSystem) {
        }

        override fun exit(uiSystem: WorldMapUiSystem) {
        }

        override fun onMessage(uiSystem: WorldMapUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: WorldMapUiSystem) {
        }
    }

}
