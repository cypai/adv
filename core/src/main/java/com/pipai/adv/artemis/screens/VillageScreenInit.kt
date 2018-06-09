package com.pipai.adv.artemis.screens

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.OrthographicCameraComponent

@Wire
class VillageScreenInit(private val world: World, private val game: AdvGame, private val config: AdvConfig) {

    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        val uiCameraId = world.create()
        mCamera.create(uiCameraId)
        sTags.register(Tags.UI_CAMERA.toString(), uiCameraId)
    }

}
