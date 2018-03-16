package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.ScreenResolution
import com.pipai.adv.artemis.events.ScreenResolutionChangeEvent
import com.pipai.adv.artemis.screens.NewGameScreen
import com.pipai.adv.gui.LoadGameDisplay
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class MainMenuUiSystem(private val game: AdvGame, private val stage: Stage) : BaseSystem(), InputProcessor {

    private val logger = getLogger()

    private val sEvent by system<EventSystem>()

    private val batch = game.batchHelper
    private val config = game.advConfig

    private val background = game.skin.get("mainMenuBg", Texture::class.java)

    private val skin = Skin(Gdx.files.internal("assets/binassets/graphics/skins/neutralizer-ui.json"))

    private val loadGameDisplay = LoadGameDisplay(game)
    private val stateMachine = StackStateMachine<MainMenuUiSystem, MainMenuUiSystem.MainMenuUiState>(this)

    private val BUTTON_WIDTH_RATIO = 0.3f
    private val BUTTON_HEIGHT_RATIO = 0.1f
    private val BUTTON_HPADDING_RATIO = 0.03f

    init {
        initUi(game.advConfig.resolution)
        stateMachine.setInitialState(MainMenuUiState.INITIAL)
    }

    @Subscribe
    public fun resolutionChangeListener(event: ScreenResolutionChangeEvent) {
        logger.info("Screen resolution changed to ${event}")
        stage.viewport.update(event.resolution.width, event.resolution.height, true)
        stage.clear()
        initUi(event.resolution)
        batch.spr.projectionMatrix = stage.camera.combined
    }

    private fun initUi(resolution: ScreenResolution) {
        val width = resolution.width.toFloat()
        val height = resolution.height.toFloat()

        val buttonHeight = height * BUTTON_HEIGHT_RATIO
        val buttonHPadding = height * BUTTON_HPADDING_RATIO

        val newGameBtn = menuButton("New Game", width, height)
        newGameBtn.setPosition(width * 2 / 3, height / 2)

        newGameBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                game.screen = NewGameScreen(game)
                dispose()
            }
        })

        val loadGameBtn = menuButton("Load Game", width, height)
        loadGameBtn.setPosition(width * 2 / 3, height / 2 - buttonHeight - buttonHPadding)

        loadGameBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                stateMachine.changeState(MainMenuUiState.SHOWING_LOAD_MENU)
            }
        })

        val optionsBtn = menuButton("Options", width, height)
        optionsBtn.setPosition(width * 2 / 3, height / 2 - 2 * (buttonHeight + buttonHPadding))

        optionsBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                if (config.resolution == ScreenResolution.RES_1280_720) {
                    config.resolution = ScreenResolution.RES_1024_768
                    Gdx.graphics.setWindowedMode(ScreenResolution.RES_1024_768.width, ScreenResolution.RES_1024_768.height)
                    config.writeToFile()
                    sEvent.dispatch(ScreenResolutionChangeEvent(config.resolution))
                } else {
                    config.resolution = ScreenResolution.RES_1280_720
                    Gdx.graphics.setWindowedMode(ScreenResolution.RES_1280_720.width, ScreenResolution.RES_1280_720.height)
                    config.writeToFile()
                    sEvent.dispatch(ScreenResolutionChangeEvent(config.resolution))
                }
            }
        })

        val quitGameBtn = menuButton("Quit Game", width, height)
        quitGameBtn.setPosition(width * 2 / 3, height / 2 - 3 * (buttonHeight + buttonHPadding))

        quitGameBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        stage.addActor(newGameBtn)
        stage.addActor(loadGameBtn)
        stage.addActor(optionsBtn)
        stage.addActor(quitGameBtn)
    }

    private fun menuButton(text: String, screenWidth: Float, screenHeight: Float): TextButton {
        val btn = TextButton(text, skin, "default")
        btn.width = screenWidth * BUTTON_WIDTH_RATIO
        btn.height = screenHeight * BUTTON_HEIGHT_RATIO
        return btn
    }

    override fun processSystem() {
        batch.spr.color = Color.WHITE
        batch.spr.begin()
        batch.spr.draw(background, 0f, 0f, config.resolution.width.toFloat(), config.resolution.height.toFloat())
        batch.spr.end()
    }

    override fun dispose() {
        skin.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.ESCAPE -> {
                if (stateMachine.isInState(MainMenuUiState.SHOWING_LOAD_MENU)) {
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

    enum class MainMenuUiState : State<MainMenuUiSystem> {
        INITIAL() {
            override fun enter(uiSystem: MainMenuUiSystem) {
                uiSystem.loadGameDisplay.remove()
            }
        },
        SHOWING_LOAD_MENU() {
            override fun enter(uiSystem: MainMenuUiSystem) {
                uiSystem.loadGameDisplay.show(uiSystem.stage)
            }

            override fun exit(uiSystem: MainMenuUiSystem) {
                uiSystem.loadGameDisplay.remove()
            }
        };

        override fun enter(uiSystem: MainMenuUiSystem) {
        }

        override fun exit(uiSystem: MainMenuUiSystem) {
        }

        override fun onMessage(uiSystem: MainMenuUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: MainMenuUiSystem) {
        }
    }

}
