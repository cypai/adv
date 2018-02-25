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
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mPcc: ComponentMapper<PccComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        val cameraId = world.create()
        mCamera.create(cameraId).camera
        sTags.register(Tags.CAMERA.toString(), cameraId)

        val previewHeight = pccCustomizer.y + pccCustomizer.height + 10f
        val previewHorizontalPadding = 40f

        val previewS = world.create()
        createPreviewEntity(previewS, pccCustomizer.x, previewHeight, Direction.S)
        val previewE = world.create()
        createPreviewEntity(previewE, pccCustomizer.x + previewHorizontalPadding, previewHeight, Direction.E)
        val previewW = world.create()
        createPreviewEntity(previewW, pccCustomizer.x + 2 * previewHorizontalPadding, previewHeight, Direction.W)
        val previewN = world.create()
        createPreviewEntity(previewN, pccCustomizer.x + 3 * previewHorizontalPadding, previewHeight, Direction.N)
    }

    private fun createPreviewEntity(id: Int, previewX: Float, previewY: Float, direction: Direction) {
        val xy = mXy.create(id)
        xy.x = previewX
        xy.y = previewY
        val animationFrames = mAnimationFrames.create(id)
        animationFrames.frameMax = 3
        animationFrames.tMax = 30
        val pcc = mPcc.create(id)
        pcc.pcc = pccCustomizer.getPcc()
        pcc.direction = direction
    }
}
