package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.animation.AnimationFrameIncrementSystem
import com.pipai.adv.artemis.system.input.ExitInputProcessor
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.artemis.system.rendering.PccRenderingSystem
import com.pipai.adv.artemis.system.ui.NewGameUiSystem
import com.pipai.adv.utils.getLogger
import net.mostlyoriginal.api.event.common.EventSystem

class NewGameScreen(game: AdvGame) : Screen {

    private val logger = getLogger()

    private val world: World

    init {
        logger.debug("Starting NewGameScreen")

        val uiSystem = NewGameUiSystem(game)

        val config = WorldConfigurationBuilder()
                .with(
                        // Managers
                        TagManager(),
                        GroupManager(),
                        EventSystem(),

                        uiSystem,
                        InputProcessingSystem(),
                        AnimationFrameIncrementSystem())
                .withPassive(-2,
                        PccRenderingSystem(game.batchHelper, game.globals, game.advConfig, game.globals.pccManager))
                .build()

        world = World(config)

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addAlwaysOnProcessor(ExitInputProcessor())
        inputProcessor.addAlwaysOnProcessor(uiSystem)
        inputProcessor.addAlwaysOnProcessor(uiSystem.stage)
        inputProcessor.activateInput()

        StandardScreenInit(world, game, game.advConfig).initialize()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.setDelta(delta)
        world.process()
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
