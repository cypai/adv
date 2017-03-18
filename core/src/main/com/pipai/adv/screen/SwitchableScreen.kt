package com.pipai.adv.screen

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen

abstract class SwitchableScreen(val game: Game) : Screen {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SwitchableScreen::class.java)
    }

    fun switchScreen(screen: Screen) {
        LOGGER.debug("Switching Gui to " + screen.javaClass)
        dispose()
        game.setScreen(screen)
    }
}
