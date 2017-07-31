package com.pipai.adv.artemis.system.rendering

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class FpsRenderingSystem(private val batch: BatchHelper) : BaseSystem() {

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    override fun processSystem() {

        val uiCameraId = sTags.getEntityId(Tags.UI_CAMERA.toString())
        val uiCamera = mCamera.get(uiCameraId).camera

        val spr = batch.spr
        val font = batch.font
        spr.projectionMatrix = uiCamera.combined
        spr.begin()
        font.color = Color.WHITE
        font.draw(spr, Gdx.graphics.framesPerSecond.toString(),
                Gdx.graphics.width.toFloat() - 24,
                Gdx.graphics.height.toFloat() - font.lineHeight / 2)
        spr.end()
    }

}
