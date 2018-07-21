package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.DrawableComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class WorldMapRenderingSystem(private val game: AdvGame) : IteratingSystem(allOf()) {

    private val batch = game.batchHelper

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mXy by mapper<XYComponent>()
    private val mDrawable by mapper<DrawableComponent>()

    private val sTags by system<TagManager>()

    override fun process(entityId: Int) {
        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        batch.spr.projectionMatrix = camera.combined
        batch.spr.begin()
        batch.spr.color = Color.WHITE
        renderEverything()
        batch.spr.end()
    }

    private fun renderEverything() {
        val drawableIds = world.fetch(allOf(DrawableComponent::class, XYComponent::class))
                .sortedWith(compareBy({ mDrawable.get(it).depth }, { -mXy.get(it).y }))
        drawableIds.forEach {
            val cDrawable = mDrawable.get(it)
            val cXy = mXy.get(it)
            if (cDrawable.centered) {
                cDrawable.drawable.draw(batch.spr, cXy.x - cDrawable.width / 2, cXy.y - cDrawable.height / 2, cDrawable.width, cDrawable.height)
            } else {
                cDrawable.drawable.draw(batch.spr, cXy.x, cXy.y, cDrawable.width, cDrawable.height)
            }
        }
    }
}
