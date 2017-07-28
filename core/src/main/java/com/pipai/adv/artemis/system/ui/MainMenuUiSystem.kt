package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.BattleMapScreen

class MainMenuUiSystem(private val game: AdvGame) : BaseSystem() {

    val stage = Stage()

    private val batch = game.batchHelper
    private val config = game.advConfig

    private val background = Texture(Gdx.files.internal("assets/binassets/graphics/textures/mainmenu.jpg"))

    private val skin = Skin(Gdx.files.internal("assets/binassets/graphics/skins/neutralizer-ui.json"))

    private val BUTTON_WIDTH_RATIO = 0.3f
    private val BUTTON_HEIGHT_RATIO = 0.1f
    private val BUTTON_HPADDING_RATIO = 0.03f

    init {
        val width = config.resolution.width.toFloat()
        val height = config.resolution.height.toFloat()

        val buttonHeight = height * BUTTON_HEIGHT_RATIO
        val buttonHPadding = height * BUTTON_HPADDING_RATIO

        val newGameBtn = menuButton("New Game", width, height)
        newGameBtn.setPosition(width * 2 / 3, height / 2)

        newGameBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                game.screen = BattleMapScreen(game)
                dispose()
            }
        })

        val loadGameBtn = menuButton("Load Game", width, height)
        loadGameBtn.setPosition(width * 2 / 3, height / 2 - buttonHeight - buttonHPadding)

        loadGameBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                game.screen = BattleMapScreen(game)
                dispose()
            }
        })

        val optionsBtn = menuButton("Options", width, height)
        optionsBtn.setPosition(width * 2 / 3, height / 2 - 2 * (buttonHeight + buttonHPadding))

        optionsBtn.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                optionsBtn.setText("Options: Not yet implemented")
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
        background.dispose()
        skin.dispose()
        stage.dispose()
    }

}
