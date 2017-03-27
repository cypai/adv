package com.pipai.adv.artemis.system.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.pipai.adv.artemis.system.NoProcessingSystem

class InputProcessingSystem : NoProcessingSystem() {

    private val multiplexer: InputMultiplexer = InputMultiplexer()
    private val inactiveMultiplexer: InputMultiplexer = InputMultiplexer()

    override protected fun initialize() {
        multiplexer.addProcessor(ExitInputProcessor())
        Gdx.input.inputProcessor = multiplexer
        inactiveMultiplexer.addProcessor(ExitInputProcessor())
    }

    fun activateInput() {
        Gdx.input.inputProcessor = multiplexer
    }

    fun deactivateInput() {
        Gdx.input.inputProcessor = inactiveMultiplexer
    }
}
