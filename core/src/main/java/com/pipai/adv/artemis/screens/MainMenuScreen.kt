package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.input.ExitInputProcessor
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.screen.SwitchableScreen
import net.mostlyoriginal.api.event.common.EventSystem

class MainMenuScreen(game: AdvGame) : SwitchableScreen(game) {

    private val batch: BatchHelper = game.batchHelper

    private val world: World

    init {
        val config = WorldConfigurationBuilder()
                .with(
                        // Managers
                        TagManager(),
                        GroupManager(),
                        EventSystem(),

                        InputProcessingSystem())
                .build()

        world = World(config)

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addAlwaysOnProcessor(ExitInputProcessor())
        inputProcessor.activateInput()
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
