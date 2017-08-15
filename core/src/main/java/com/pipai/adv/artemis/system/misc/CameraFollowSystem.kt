package com.pipai.adv.artemis.system.misc

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.CameraFollowComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system

class CameraFollowSystem(private val config: AdvConfig) : IteratingSystem(allOf()) {

    private val mXy by require<XYComponent>()
    private val mCameraFollow by require<CameraFollowComponent>()

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    override fun process(entityId: Int) {
        val cXy = mXy.get(entityId)

        val camera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString())).camera
        camera.position.x = cXy.x + config.resolution.tileSize / 2
        camera.position.y = cXy.y + config.resolution.tileSize / 2
        camera.update()
    }

}
