package com.pipai.adv.artemis.system.init

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.MapTilesetMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.npc.NpcList
import com.pipai.adv.save.AdvSave
import com.pipai.adv.tiles.PccManager

@Wire
class BattleMapScreenInit(private val world: World, private val config: AdvConfig,
                          private val save: AdvSave,
                          private val npcList: NpcList, private val partyList: List<Int>,
                          private val map: BattleMap) {

    private lateinit var mBackend: ComponentMapper<BattleBackendComponent>
    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>
    private lateinit var mEnvObjTile: ComponentMapper<EnvObjTileComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mCollision: ComponentMapper<CollisionComponent>
    private lateinit var mNpcId: ComponentMapper<NpcIdComponent>
    private lateinit var mPlayerUnit: ComponentMapper<PlayerUnitComponent>

    private lateinit var sTags: TagManager

    private var playerUnitIndex = 0

    init {
        world.inject(this)
    }

    fun initialize() {
        val backendId = world.create()
        val cBackend = mBackend.create(backendId)
        cBackend.backend = BattleBackend(save, npcList, map)
        sTags.register(Tags.BACKEND.toString(), backendId)

        val cameraId = world.create()
        val camera = mCamera.create(cameraId).camera
        sTags.register(Tags.CAMERA.toString(), cameraId)
        camera.zoom = world.getSystem(ZoomInputSystem::class.java).currentZoom()

        val npcPositions = cBackend.backend.getNpcPositions()
        val playerPosition = npcPositions[partyList[0]]!!
        camera.position.x = playerPosition.x.toFloat() * config.resolution.tileSize
        camera.position.y = playerPosition.y.toFloat() * config.resolution.tileSize

        val uiCameraId = world.create()
        mCamera.create(uiCameraId)
        sTags.register(Tags.UI_CAMERA.toString(), uiCameraId)

        for (x in 0 until map.width) {
            for (y in 0 until map.height) {
                val cell = map.getCell(x, y)
                val fullEnvObj = cell.fullEnvObject ?: continue
                handleEnvObj(fullEnvObj, x, y)
            }
        }
    }

    private fun handleEnvObj(envObj: FullEnvObject, x: Int, y: Int) {
        val id = world.create()
        val tilesetMetadata = envObj.getTilesetMetadata()

        when (tilesetMetadata) {
            is PccTilesetMetadata -> {
                val cEnvObjTile = mEnvObjTile.create(id)
                cEnvObjTile.tilesetMetadata = tilesetMetadata.deepCopy()
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat()
                val cAnimationFrames = mAnimationFrames.create(id)
                cAnimationFrames.frameMax = 3
                cAnimationFrames.tMax = 30
                cAnimationFrames.tStartNoise = 5

                val cCollision = mCollision.create(id)
                cCollision.bounds = CollisionBounds.CollisionBoundingBox(0f, 0f,
                        PccManager.PCC_WIDTH.toFloat(), PccManager.PCC_HEIGHT.toFloat())

                if (envObj is FullEnvObject.NpcEnvObject) {
                    mNpcId.create(id).npcId = envObj.npcId
                    if (save.npcInPlayerGuild(envObj.npcId)) {
                        mPlayerUnit.create(id).index = playerUnitIndex
                        playerUnitIndex++
                    }
                }
            }
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

}
