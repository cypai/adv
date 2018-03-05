package com.pipai.adv.artemis.system.input

import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.events.TileHighlightUpdateEvent
import com.pipai.adv.artemis.events.ZoomScrollDisableEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.MathUtils
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class ZoomInputSystem : NoProcessingSystem(), InputProcessor {

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    private val zoomSettings = listOf(0.25f, 0.5f, 1f, 1.3f)
    private var zoomIndex = 1
    private var disabled = false

    @Subscribe
    fun zoomScrollDisableListener(event: ZoomScrollDisableEvent) {
        disabled = event.disabled
    }

    fun currentZoom() = zoomSettings.get(zoomIndex)

    override fun keyDown(keycode: Int): Boolean = false

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean {
        if (!disabled) {
            zoomIndex = MathUtils.clamp(zoomIndex + amount, 0, zoomSettings.size - 1)
            val camera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString())).camera
            camera.zoom = zoomSettings.get(zoomIndex)
        }
        return false
    }
}
