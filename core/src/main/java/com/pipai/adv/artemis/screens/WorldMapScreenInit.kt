package com.pipai.adv.artemis.screens

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.badlogic.gdx.graphics.Color
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.DrawableComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.XYComponent

@Wire
class WorldMapScreenInit(private val world: World, private val game: AdvGame, private val config: AdvConfig) {

    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mDrawable: ComponentMapper<DrawableComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        val startPosition = game.globals.worldMap.villageLocations["Lagos Village"]!!

        val cameraId = world.create()
        sTags.register(Tags.CAMERA.toString(), cameraId)
        val camera = mCamera.create(cameraId)
        camera.camera.position.x = startPosition.x.toFloat()
        camera.camera.position.y = startPosition.y.toFloat()
        camera.camera.update()

        val uiCameraId = world.create()
        mCamera.create(uiCameraId)
        sTags.register(Tags.UI_CAMERA.toString(), uiCameraId)

        initializePointsOfInterest()
        initializeSquads()
    }

    private fun initializePointsOfInterest() {
        val worldMap = game.globals.worldMap
        worldMap.villageLocations.forEach { _, worldMapLocation ->
            val entityId = world.create()
            val cXy = mXy.create(entityId)
            cXy.setXy(worldMapLocation.x.toFloat(), worldMapLocation.y.toFloat())
            val cDrawable = mDrawable.create(entityId)
            cDrawable.drawable = game.skin.newDrawable("white", Color.RED)
            cDrawable.width = 16f
            cDrawable.height = 16f
            cDrawable.centered = true
        }
    }

    private fun initializeSquads() {
        game.globals.save!!.squadLocations.forEach { _, worldMapLocation ->
            val entityId = world.create()
            val cXy = mXy.create(entityId)
            cXy.setXy(worldMapLocation.x.toFloat(), worldMapLocation.y.toFloat())
            val cDrawable = mDrawable.create(entityId)
            cDrawable.drawable = game.skin.newDrawable("white", Color.CYAN)
            cDrawable.width = 7f
            cDrawable.height = 7f
            cDrawable.depth = 1
            cDrawable.centered = true
        }
    }

}
