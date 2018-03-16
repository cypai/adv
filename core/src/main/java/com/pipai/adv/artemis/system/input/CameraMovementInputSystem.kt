package com.pipai.adv.artemis.system.input

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.events.EndTurnEvent
import com.pipai.adv.artemis.events.MouseCameraMoveDisableEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.ui.BattleUiSystem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.Subscribe

class CameraMovementInputSystem(val config: AdvConfig) : BaseSystem(), InputProcessor {

    private val speed = 3
    private val mouseEdgeBuffer = 48

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    private var mouseCameraMoveDisabled = false

    private val heldKeys: HeldKeys = HeldKeys()
    private val mouseEdges: MutableMap<Direction, Boolean> = mutableMapOf()

    init {
        mouseEdges[Direction.N] = false
        mouseEdges[Direction.S] = false
        mouseEdges[Direction.W] = false
        mouseEdges[Direction.E] = false
    }

    @Subscribe
    fun mouseCameraMoveDisableListener(event: MouseCameraMoveDisableEvent) {
        mouseCameraMoveDisabled = event.disabled
        if (event.disabled) mouseEdges.replaceAll { _, _ -> false }
    }

    @Subscribe
    fun endTurnListener(event: EndTurnEvent) {
        when (event.team) {
            Team.PLAYER -> isEnabled = false
            Team.AI -> isEnabled = true
        }
    }

    override fun processSystem() {
        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera
        translateCamera(camera)
        camera.update()
    }

    private fun translateCamera(camera: OrthographicCamera) {
        if (heldKeys.isDown(Keys.W) || (!mouseCameraMoveDisabled && mouseEdges[Direction.N]!!)) {
            camera.position.y += speed
        }
        if (heldKeys.isDown(Keys.A) || (!mouseCameraMoveDisabled && mouseEdges[Direction.W]!!)) {
            camera.position.x -= speed
        }
        if (heldKeys.isDown(Keys.S) || (!mouseCameraMoveDisabled && mouseEdges[Direction.S]!!)) {
            camera.position.y -= speed
        }
        if (heldKeys.isDown(Keys.D) || (!mouseCameraMoveDisabled && mouseEdges[Direction.E]!!)) {
            camera.position.x += speed
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        heldKeys.keyDown(keycode)
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        heldKeys.keyUp(keycode)
        return false
    }

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        mouseEdges[Direction.N] = screenY <= mouseEdgeBuffer
        mouseEdges[Direction.S] = screenY >= config.resolution.height - mouseEdgeBuffer
        mouseEdges[Direction.W] = screenX <= mouseEdgeBuffer
        mouseEdges[Direction.E] = screenX >= config.resolution.width - mouseEdgeBuffer
        return false
    }

    override fun scrolled(amount: Int) = false
}
