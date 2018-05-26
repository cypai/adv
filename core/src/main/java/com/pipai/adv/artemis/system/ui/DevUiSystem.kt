package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class DevUiSystem(private val game: AdvGame,
                  private val stage: Stage) : NoProcessingSystem(), InputProcessor {

    private val sEvent by system<EventSystem>()

    private val stateMachine = DefaultStateMachine<DevUiSystem, DevUiState>(this)

    private val table = Table()
    private val inputField = TextField("", game.skin)

    private val terminalOutput: MutableList<String> = mutableListOf()
    private val terminalLabels: MutableList<Label> = mutableListOf()

    init {
        stateMachine.setInitialState(DevUiState.DISABLED)
        createMainForm()
    }

    override fun initialize() {
        isEnabled = false
    }

    private fun createMainForm() {
        val skin = game.skin

        val width = game.advConfig.resolution.width.toFloat() - 64f
        val height = game.advConfig.resolution.height.toFloat() - 64f

        table.x = 32f
        table.y = 32f
        table.width = width
        table.height = height
        table.background = skin.getDrawable("white")

        table.bottom().left()

        repeat(20) {
            val label = Label("", skin, "dev")
            terminalOutput.add("")
            terminalLabels.add(label)
            table.add(label).left()
            table.row()
        }
        val inputRow = HorizontalGroup()
        val label = Label("$ ", skin, "dev")
        inputRow.addActor(label)
        inputRow.addActor(Container(inputField).prefWidth(width - label.width))
        table.add(inputRow).width(width)
        table.validate()
    }

    private fun evaluateCommand() {
        val commandLine = inputField.text
        inputField.text = ""
        addTerminalOutput("$ $commandLine")
        val tokens = commandLine.split(" ").filter { it.isNotBlank() }
        val command = tokens.firstOrNull()
        when (command) {
            "killall" -> {
                addTerminalOutput("Killing all")
            }
            else -> {
                addTerminalOutput("Unknown command.")
            }
        }
        for (i in terminalLabels.size - 1 downTo 0) {
            terminalLabels[i].setText(terminalOutput.asReversed()[terminalLabels.size - 1 - i])
        }
    }

    private fun addTerminalOutput(line: String) {
        terminalOutput.removeAt(0)
        terminalOutput.add(line)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.BACKSLASH -> {
                when {
                    stateMachine.isInState(DevUiState.DISABLED) -> {
                        stateMachine.changeState(DevUiState.SHOWING_TERMINAL)
                    }
                    else -> {
                        stateMachine.changeState(DevUiState.DISABLED)
                    }
                }
                return true
            }
            Keys.ESCAPE -> {
                if (!stateMachine.isInState(DevUiState.DISABLED)) {
                    stateMachine.changeState(DevUiState.DISABLED)
                    return true
                }
            }
            Keys.ENTER -> {
                evaluateCommand()
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char): Boolean {
        if (stateMachine.isInState(DevUiState.DISABLED)) {
            return false
        } else {
            if (character != '\\') {
                stage.keyTyped(character)
            }
            return true
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

    enum class DevUiState : State<DevUiSystem> {
        DISABLED() {
            override fun enter(uiSystem: DevUiSystem) {
                uiSystem.table.remove()
                uiSystem.sEvent.dispatch(PauseEvent(false))
            }
        },
        SHOWING_TERMINAL() {
            override fun enter(uiSystem: DevUiSystem) {
                uiSystem.stage.addActor(uiSystem.table)
                uiSystem.stage.keyboardFocus = uiSystem.inputField
                uiSystem.sEvent.dispatch(PauseEvent(true))
            }
        };

        override fun enter(uiSystem: DevUiSystem) {
        }

        override fun exit(uiSystem: DevUiSystem) {
        }

        override fun onMessage(uiSystem: DevUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: DevUiSystem) {
        }
    }

}
