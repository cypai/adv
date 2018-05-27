package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.artemis.screens.MainMenuScreen
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.backend.battle.engine.log.BattleEndEvent
import com.pipai.adv.backend.battle.engine.log.EndingType

class BattleEndSystem(private val game: AdvGame,
                      private val stage: Stage) : NoProcessingSystem(), InputProcessor {

    private val table = Table()

    private val titleLabel = Label("", game.skin)
    private val button = TextButton("  Return to guild  ", game.skin)

    override fun initialize() {
        isEnabled = false
    }

    fun activateEndSequence(event: BattleEndEvent) {
        isEnabled = true
        val skin = game.skin

        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        table.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        table.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        table.width = mainMenuWidth
        table.height = mainMenuHeight
        table.background = skin.getDrawable("frameDrawable")

        table.top().pad(10f)
        table.add(titleLabel)
        table.row()
        table.add(button)
        table.row()

        adjustEndingForm(event.endingType)

        stage.addActor(table)
    }

    private fun adjustEndingForm(endingType: EndingType) {
        when (endingType) {
            EndingType.MAP_CLEAR -> {
                titleLabel.setText("Map Clear!")
                button.setText("  Return to guild  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = GuildScreen(game)
                    }
                })
            }
            EndingType.GAME_OVER -> {
                titleLabel.setText("Game Over...")
                button.setText("  ...  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = MainMenuScreen(game)
                    }
                })
            }
            EndingType.RAN_AWAY -> {
                titleLabel.setText("Ran Away...")
                button.setText("  Return to guild  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = GuildScreen(game)
                    }
                })
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
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

}
