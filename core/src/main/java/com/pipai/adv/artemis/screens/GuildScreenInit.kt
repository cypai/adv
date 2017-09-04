package com.pipai.adv.artemis.system.init

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.CameraFollowComponent
import com.pipai.adv.artemis.components.CollisionBounds.CollisionBoundingBox
import com.pipai.adv.artemis.components.CollisionComponent
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.MainTextboxFlagComponent
import com.pipai.adv.artemis.components.MultipleTextComponent
import com.pipai.adv.artemis.components.NpcComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PartialTextComponent
import com.pipai.adv.artemis.components.TextInteractionComponent
import com.pipai.adv.artemis.components.WallCollisionFlagComponent
import com.pipai.adv.artemis.components.WallComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.MapTilesetMetadata
import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.domain.FullEnvObject.NpcEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.npc.NpcList

@Wire
class GuildScreenInit(private val world: World, private val config: AdvConfig,
                      private val npcList: NpcList, private val map: BattleMap) {

    private lateinit var mBackend: ComponentMapper<BattleBackendComponent>
    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>
    private lateinit var mCameraFollow: ComponentMapper<CameraFollowComponent>
    private lateinit var mEnvObjTile: ComponentMapper<EnvObjTileComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mCollision: ComponentMapper<CollisionComponent>
    private lateinit var mWallCollisionFlag: ComponentMapper<WallCollisionFlagComponent>
    private lateinit var mNpc: ComponentMapper<NpcComponent>
    private lateinit var mWall: ComponentMapper<WallComponent>
    private lateinit var mTextInteraction: ComponentMapper<TextInteractionComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        // This backend is just for rendering the BattleMap, there is no real battle happening
        val backendId = world.create()
        val cBackend = mBackend.create(backendId)
        cBackend.backend = BattleBackend(npcList, map)

        val cameraId = world.create()
        val camera = mCamera.create(cameraId).camera
        // Hard-coded position for the camera on the player character's spawn location
        camera.position.x = config.resolution.tileSize * 1.5f
        camera.position.y = config.resolution.tileSize * 1.5f
        camera.zoom = world.getSystem(ZoomInputSystem::class.java).currentZoom()
        sTags.register(Tags.CAMERA.toString(), cameraId)

        val uiCameraId = world.create()
        mCamera.create(uiCameraId)
        sTags.register(Tags.UI_CAMERA.toString(), uiCameraId)

        npcList.forEach { addNpcTile(it.key, it.key + 1, 2) }

        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val cell = map.getCell(x, y)
                val fullEnvObj = cell.fullEnvObject ?: continue
                handleEnvObj(fullEnvObj, x, y)
            }
        }
    }

    private fun addNpcTile(npcId: Int, x: Int, y: Int) {
        val tileSize = config.resolution.tileSize.toFloat()

        val entityId = world.create()

        val npc = npcList.getNpc(npcId)!!

        if (npcId == 0) {
            sTags.register(Tags.CONTROLLABLE_CHARACTER.toString(), entityId)
            mCameraFollow.create(entityId)
        } else {
            val cTextInteraction = mTextInteraction.create(entityId)
            cTextInteraction.textList.add("Hi, my name is ${npc.unitInstance.nickname}.")
            cTextInteraction.textList.add("Nice to meet you! I hope that this guild will become successful one day under your leadership.")

            mWall.create(entityId)
            val cCollision = mCollision.create(entityId)
            cCollision.bounds = CollisionBoundingBox(0f, 0f, tileSize / 2, tileSize / 2)
        }

        val tilesetMetadata = npc.tilesetMetadata

        val cEnvObjTile = mEnvObjTile.create(entityId)
        cEnvObjTile.tilesetMetadata = tilesetMetadata

        val cXy = mXy.create(entityId)
        cXy.x = tileSize * x
        cXy.y = tileSize * y

        mAnimationFrames.create(entityId)

        mWallCollisionFlag.create(entityId)
        mNpc.create(entityId)
        val cCollision = mCollision.create(entityId)
        cCollision.bounds = CollisionBoundingBox(tileSize / 4, tileSize / 4, tileSize / 2, tileSize / 2)
    }

    private fun handleEnvObj(envObj: FullEnvObject, x: Int, y: Int) {
        val id = world.create()
        val tilesetMetadata = envObj.getTilesetMetadata()

        generateTilesetMetadataComponents(id, x, y, tilesetMetadata)
        generateCollisionComponents(id, envObj)
    }

    private fun generateTilesetMetadataComponents(id: Int, x: Int, y: Int, tilesetMetadata: EnvObjTilesetMetadata) {
        when (tilesetMetadata) {
            is MapTilesetMetadata -> {
                val cEnvObjTile = mEnvObjTile.create(id)
                cEnvObjTile.tilesetMetadata = tilesetMetadata.deepCopy()
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat()
                mAnimationFrames.create(id)
            }
            else -> {
                // Do nothing
            }
        }
    }

    private fun generateCollisionComponents(id: Int, envObj: FullEnvObject) {
        val tileSize = config.resolution.tileSize.toFloat()
        when (envObj) {
            is NpcEnvObject -> {
                val cCollision = mCollision.create(id)
                cCollision.bounds = CollisionBoundingBox(tileSize / 4, tileSize / 4, tileSize / 2, tileSize / 2)
            }
            else -> {
                mWall.create(id)
                val cCollision = mCollision.create(id)
                cCollision.bounds = CollisionBoundingBox(0f, 0f, tileSize, tileSize)
            }
        }
    }

}
