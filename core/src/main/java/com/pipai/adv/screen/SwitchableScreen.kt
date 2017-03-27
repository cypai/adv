package com.pipai.adv.screen

import com.badlogic.gdx.Game
import com.badlogic.gdx.Screen
import org.slf4j.LoggerFactory

abstract class SwitchableScreen(val game: Game) : Screen {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SwitchableScreen::class.java)
    }

    fun switchScreen(screen: Screen) {
        LOGGER.debug("Switching Gui to " + screen.javaClass)
        dispose()
        game.screen = screen
    }
}
