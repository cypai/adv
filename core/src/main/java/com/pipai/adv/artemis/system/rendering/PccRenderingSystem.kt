package com.pipai.adv.artemis.system.rendering

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class PccRenderingSystem(private val batch: BatchHelper,
                         private val advConfig: AdvConfig,
                         private val pccManager: PccManager) : BaseSystem() {

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mXy by mapper<XYComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()
    private val mPcc by mapper<PccComponent>()

    private val sTags by system<TagManager>()

    override fun processSystem() {
        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        val tileSize = advConfig.resolution.tileSize.toFloat()

        val pccEntityBag = world.aspectSubscriptionManager.get(allOf(
                XYComponent::class, AnimationFramesComponent::class, PccComponent::class)).entities
        val pccEntities = pccEntityBag.data.slice(0 until pccEntityBag.size())

        batch.spr.projectionMatrix = camera.combined
        batch.spr.begin()
        for (entityId in pccEntities) {
            val cXy = mXy.get(entityId)
            val cPcc = mPcc.get(entityId)
            val cAnimationFrames = mAnimationFrames.get(entityId)

            for (pcc in cPcc.pcc) {
                val pccTexture = pccManager.getPccFrame(pcc, UnitAnimationFrame(cPcc.direction, cAnimationFrames.frame))
                val scaleFactor = tileSize / pccTexture.regionWidth
                batch.spr.draw(pccTexture, cXy.x, cXy.y, tileSize, pccTexture.regionHeight * scaleFactor)
            }
        }
        batch.spr.end()
    }
}
