package com.pipai.adv.artemis.screens

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.badlogic.gdx.graphics.Color
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*

@Wire
class WorldMapScreenInit(private val world: World, private val game: AdvGame, private val config: AdvConfig) {

    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mDrawable: ComponentMapper<DrawableComponent>
    private lateinit var mSquad: ComponentMapper<SquadComponent>
    private lateinit var mPoi: ComponentMapper<PointOfInterestComponent>
    private lateinit var mEnvObjTile: ComponentMapper<EnvObjTileComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>

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
        worldMap.villageLocations.forEach { village, worldMapLocation ->
            val entityId = world.create()
            val cXy = mXy.create(entityId)
            cXy.setXy(worldMapLocation.x.toFloat(), worldMapLocation.y.toFloat())
            val cDrawable = mDrawable.create(entityId)
            cDrawable.drawable = game.skin.newDrawable("white", Color.RED)
            cDrawable.depth = -1
            cDrawable.width = 24f
            cDrawable.height = 24f
            cDrawable.centered = true
            val cPoi = mPoi.create(entityId)
            cPoi.name = village
            cPoi.screenCallback = { VillageScreen(game) }
        }
    }

    private fun initializeSquads() {
        val save = game.globals.save!!
        save.squadLocations.forEach { squad, worldMapLocation ->
            val entityId = world.create()
            val cXy = mXy.create(entityId)
            cXy.setXy(worldMapLocation.x.toFloat(), worldMapLocation.y.toFloat())
            val squadLeader = save.squads[squad]!!.first()
            val squadPcc = save.globalNpcList.getNpc(squadLeader)!!.tilesetMetadata
            val cEnvObjTile = mEnvObjTile.create(entityId)
            cEnvObjTile.tilesetMetadata = squadPcc
            val cAnimationFrames = mAnimationFrames.create(entityId)
            cAnimationFrames.frameMax = 3
            cAnimationFrames.tMax = 60
            cAnimationFrames.tStartNoise = 5
            val cSquad = mSquad.create(entityId)
            cSquad.squad = squad
        }
    }

}
