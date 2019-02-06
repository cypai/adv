package com.pipai.adv.artemis.system.ui

import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.screens.MainMenuScreen
import com.pipai.adv.artemis.screens.ResultsScreen
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.log.BattleEndEvent
import com.pipai.adv.backend.battle.engine.log.EndingType
import com.pipai.adv.domain.ResultsData
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class BattleEndSystem(private val game: AdvGame,
                      private val stage: Stage) : NoProcessingSystem(), InputProcessor {

    private val mBackend by mapper<BattleBackendComponent>()

    private val sTags by system<TagManager>()

    private val table = Table()

    private val titleLabel = Label("", game.skin)
    private val killsLabel = Label("", game.skin)
    private val expLabel = Label("", game.skin)
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
        table.add(killsLabel)
        table.row()
        table.add(expLabel)
        table.row()
        table.add(button)
        table.row()

        adjustEndingForm(event)

        stage.addActor(table)
    }

    private fun adjustEndingForm(event: BattleEndEvent) {
        val results = generateResults(event)
        when (event.endingType) {
            EndingType.GAME_OVER -> {
                titleLabel.setText("Game Over...")
                button.setText("  ...  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = MainMenuScreen(game)
                    }
                })
            }
            EndingType.MAP_CLEAR -> {
                titleLabel.setText("Map Clear!")
                killsLabel.setText("Enemies Defeated: ${event.battleStats.getAllKills().size}")
                expLabel.setText("EXP Gained: ${event.battleStats.getExpGained()}")
                button.setText("  Return to guild  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = ResultsScreen(game, results)
                    }
                })
            }
            EndingType.RAN_AWAY -> {
                titleLabel.setText("Ran Away...")
                killsLabel.setText("Enemies Defeated: ${event.battleStats.getAllKills().size}")
                expLabel.setText("EXP Gained: ${event.battleStats.getExpGained()}")
                button.setText("  Return to guild  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = ResultsScreen(game, results)
                    }
                })
            }
            EndingType.MISSION_COMPLETE -> {
                titleLabel.setText("Mission Complete!")
                killsLabel.setText("Enemies Defeated: ${event.battleStats.getAllKills().size}")
                expLabel.setText("EXP Gained: ${event.battleStats.getExpGained()}")
                button.setText("  Return to guild  ")
                button.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        game.screen = ResultsScreen(game, results)
                    }
                })
            }
        }
    }

    private fun getBackend() = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

    private fun generateResults(event: BattleEndEvent): ResultsData {
        val backend = getBackend()
        val team = backend.getTeam(Team.PLAYER)
        return ResultsData(team.map { it to event.battleStats.getExpGained() }.toMap())
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
