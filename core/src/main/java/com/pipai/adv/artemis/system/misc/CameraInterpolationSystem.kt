package com.pipai.adv.artemis.system.misc

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system

class CameraInterpolationSystem : IteratingSystem(allOf()) {

    private val mPath by require<PathInterpolationComponent>()
    private val mCamera by require<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    override fun process(entityId: Int) {
        val camera = mCamera.get(entityId).camera
        val cPath = mPath.get(entityId)
        val currentPos = cPath.getCurrentPos()
        camera.position.x = currentPos.x
        camera.position.y = currentPos.y
        camera.update()
    }

    fun sendCameraToPosition(position: Vector2) {
        sendCameraToPosition(position, null)
    }

    fun sendCameraToPosition(position: Vector2, callback: (() -> Unit)?) {
        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val cCamera = mCamera.get(cameraId)
        val cInterpolation = mPath.create(cameraId)
        cInterpolation.clear()
        cInterpolation.interpolation = Interpolation.sineOut
        cInterpolation.endpoints.add(Vector2(cCamera.camera.position.x, cCamera.camera.position.y))
        cInterpolation.endpoints.add(position)
        cInterpolation.maxT = 20
        cInterpolation.onEndpoint = { callback?.invoke() }
    }
}
