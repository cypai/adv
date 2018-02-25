package com.pipai.adv.artemis.system.input

import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.events.KeyDownEvent
import com.pipai.adv.artemis.events.MouseDownEvent
import com.pipai.adv.artemis.events.MouseUpEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class InputEventSystem : NoProcessingSystem(), InputProcessor {

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    override fun keyDown(keycode: Int): Boolean {
        sEvent.dispatch(KeyDownEvent(keycode))
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        sEvent.dispatch(MouseDownEvent(pickRay.origin.x, pickRay.origin.y, button))
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        sEvent.dispatch(MouseUpEvent(pickRay.origin.x, pickRay.origin.y, button))
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }


}