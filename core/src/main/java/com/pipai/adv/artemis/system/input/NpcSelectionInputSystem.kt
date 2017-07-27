package com.pipai.adv.artemis.system.input

import com.badlogic.gdx.InputProcessor
import com.pipai.adv.artemis.system.NoProcessingSystem

class NpcSelectionInputSystem : NoProcessingSystem(), InputProcessor {

    var active = true

    var selectedNpc: Int? = null
        private set

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun keyDown(keycode: Int): Boolean = false

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int) = false

}
