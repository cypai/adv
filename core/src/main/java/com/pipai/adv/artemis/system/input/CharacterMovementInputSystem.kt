package com.pipai.adv.artemis.system.input

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.CharacterTags
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class CharacterMovementInputSystem : BaseSystem(), InputProcessor {

    private val speed = 8

    private val mXy by mapper<XYComponent>()

    private val sTags by system<TagManager>()

    private val heldKeys: HeldKeys = HeldKeys()

    override protected fun processSystem() {
        val charId = sTags.getEntityId(CharacterTags.CONTROLLABLE_CHARACTER.toString())
        translateCharacter(mXy.get(charId))
    }

    private fun translateCharacter(xy: XYComponent) {
        if (heldKeys.isDown(Keys.W) || heldKeys.isDown(Keys.UP)) {
            xy.y += speed
        }
        if (heldKeys.isDown(Keys.A) || heldKeys.isDown(Keys.LEFT)) {
            xy.x -= speed
        }
        if (heldKeys.isDown(Keys.S) || heldKeys.isDown(Keys.DOWN)) {
            xy.y -= speed
        }
        if (heldKeys.isDown(Keys.D) || heldKeys.isDown(Keys.RIGHT)) {
            xy.x += speed
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        heldKeys.keyDown(keycode)
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        heldKeys.keyUp(keycode)
        return true
    }

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int) = false
}
