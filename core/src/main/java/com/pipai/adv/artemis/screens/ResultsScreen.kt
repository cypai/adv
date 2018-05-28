package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.animation.AnimationFrameIncrementSystem
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.artemis.system.ui.ResultsUiSystem
import com.pipai.adv.domain.ResultsData
import com.pipai.adv.screen.SwitchableScreen
import com.pipai.adv.utils.getLogger
import net.mostlyoriginal.api.event.common.EventSystem

class ResultsScreen(game: AdvGame, resultsData: ResultsData) : SwitchableScreen(game) {

    private val logger = getLogger()

    private val stage = Stage(ScreenViewport(), game.spriteBatch)

    private val world: World

    init {
        logger.debug("Starting ResultsScreen")

        val config = WorldConfigurationBuilder()
                .with(
                        // Managers
                        TagManager(),
                        GroupManager(),
                        EventSystem(),

                        ResultsUiSystem(game, stage, resultsData),
                        InputProcessingSystem(),
                        AnimationFrameIncrementSystem())
                .build()

        world = World(config)

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addAlwaysOnProcessor(world.getSystem(ResultsUiSystem::class.java))
        inputProcessor.addAlwaysOnProcessor(stage)
        inputProcessor.activateInput()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.setDelta(delta)
        world.process()
        stage.act()
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun show() {
    }

    override fun hide() {
    }

    override fun dispose() {
        world.dispose()
    }
}
