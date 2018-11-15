package com.pipai.adv.artemis.system.input

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.MainTextboxUiSystem
import com.pipai.adv.domain.Cutscene
import com.pipai.adv.utils.getSystemSafe
import com.pipai.adv.utils.system

class CutsceneInputSystem(private val game: AdvGame, var cutscene: Cutscene) : NoProcessingSystem(), InputProcessor {

    private val sMainTextbox by system<MainTextboxUiSystem>()

    var scene: List<String>? = null
        private set
    private var currentIndex: Int = 0

    override fun keyDown(keycode: Int): Boolean {
        if (isEnabled) {
            if (keycode == Keys.Z) {
                if (sMainTextbox.isDone()) {
                } else {
                    sMainTextbox.showFullText()
                }
            }
        }
        return false
    }

    fun showScene(sceneName: String) {
        currentIndex = 0
    }

    private fun performLine(line: String) {

    }

    private fun showText(text: String) {
        disableSystems()
        sMainTextbox.setToText(text)
        sMainTextbox.isEnabled = true
    }

    private fun finishText() {
        enableSystems()
        sMainTextbox.isEnabled = false
    }

    private fun disableSystems() {
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.disable()
    }

    private fun enableSystems() {
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.enable()
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

}
