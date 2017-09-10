package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.SchemaList
import com.pipai.adv.ScreenResolution
import com.pipai.adv.artemis.events.ScreenResolutionChangeEvent
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.artemis.screens.NewGameScreen
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.UnitInstance
import com.pipai.adv.npc.Npc
import com.pipai.adv.save.AdvSave
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class MainMenuUiSystem(private val game: AdvGame) : BaseSystem() {

    private val logger = getLogger()

    private val sEvent by system<EventSystem>()

    private val batch = game.batchHelper
    private val config = game.advConfig

    val stage = Stage(ScreenViewport())

    private val background = game.skin.get("mainMenuBg", Texture::class.java)

    private val skin = Skin(Gdx.files.internal("assets/binassets/graphics/skins/neutralizer-ui.json"))

    private val BUTTON_WIDTH_RATIO = 0.3f
    private val BUTTON_HEIGHT_RATIO = 0.1f
    private val BUTTON_HPADDING_RATIO = 0.03f

    init {
        initUi(game.advConfig.resolution)
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
                game.globals.load(0)
                game.screen = GuildScreen(game)
                dispose()
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
        batch.spr.begin()
        batch.spr.draw(background, 0f, 0f, config.resolution.width.toFloat(), config.resolution.height.toFloat())
        batch.spr.end()
        stage.act()
        stage.draw()
    }

    override fun dispose() {
        skin.dispose()
        stage.dispose()
    }

}
