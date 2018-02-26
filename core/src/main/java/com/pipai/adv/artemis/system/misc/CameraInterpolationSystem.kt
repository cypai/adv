package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.require

class CameraInterpolationSystem : IteratingSystem(allOf()) {

    private val mPath by require<PathInterpolationComponent>()
    private val mCamera by require<OrthographicCameraComponent>()

    override fun process(entityId: Int) {
        val camera = mCamera.get(entityId).camera
        val cPath = mPath.get(entityId)
        val currentPos = cPath.getCurrentPos()
        camera.position.x = currentPos.x
        camera.position.y = currentPos.y
        camera.update()
    }

}
