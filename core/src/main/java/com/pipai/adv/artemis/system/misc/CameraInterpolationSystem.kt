package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.InterpolationComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class CameraInterpolationSystem : IteratingSystem(allOf()) {

    private val mInterpolation by require<InterpolationComponent>()
    private val mCamera by require<OrthographicCameraComponent>()

    override fun process(entityId: Int) {
        val camera = mCamera.get(entityId).camera
        val cInterpolation = mInterpolation.get(entityId)
        val a = cInterpolation.t.toFloat() / cInterpolation.maxT.toFloat()
        camera.position.x = cInterpolation.interpolation.apply(cInterpolation.start.x, cInterpolation.end.x, a)
        camera.position.y = cInterpolation.interpolation.apply(cInterpolation.start.y, cInterpolation.end.y, a)
        camera.update()
    }

}