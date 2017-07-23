package com.pipai.adv.artemis.system.input

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import com.pipai.utils.getLogger
import com.pipai.adv.artemis.screens.BattleMapScreenTags

class CameraMovementInputSystem : BaseSystem(), InputProcessor {

    private val speed = 8

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    private val heldKeys: HeldKeys = HeldKeys()

    override protected fun processSystem() {
        val cameraId = sTags.getEntityId(BattleMapScreenTags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera
        translateCamera(camera)
        camera.update()
    }

    private fun translateCamera(camera: OrthographicCamera) {
        if (heldKeys.isDown(Keys.W)) {
            camera.position.y += speed
        }
        if (heldKeys.isDown(Keys.A)) {
            camera.position.x -= speed
        }
        if (heldKeys.isDown(Keys.S)) {
            camera.position.y -= speed
        }
        if (heldKeys.isDown(Keys.D)) {
            camera.position.x += speed
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
