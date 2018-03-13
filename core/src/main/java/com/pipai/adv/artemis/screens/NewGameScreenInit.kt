package com.pipai.adv.artemis.screens

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.gui.PccCustomizer

@Wire
class NewGameScreenInit(private val world: World, private val game: AdvGame, private val config: AdvConfig,
                        private val pccCustomizer: PccCustomizer) {

    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        val cameraId = world.create()
        mCamera.create(cameraId).camera
        sTags.register(Tags.CAMERA.toString(), cameraId)
    }
}
