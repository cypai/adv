package com.pipai.adv.artemis.screens

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGameGlobals
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.artemis.system.misc.BattleAiSystem
import com.pipai.adv.artemis.system.ui.BattleUiSystem
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.*
import com.pipai.adv.backend.battle.domain.EnvObject
import com.pipai.adv.backend.battle.domain.NpcEnvObject
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.rules.ending.MapClearEndingRule
import com.pipai.adv.domain.Npc
import com.pipai.adv.tiles.AnimatedTilesetManager
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.utils.AutoIncrementIdMap
import com.pipai.adv.utils.fetch

@Wire
class BattleMapScreenInit(private val world: World, private val config: AdvConfig,
                          private val globals: AdvGameGlobals,
                          private val npcList: AutoIncrementIdMap<Npc>,
                          private val envObjList: AutoIncrementIdMap<EnvObject>,
                          private val partyList: List<Int>,
                          private val map: BattleMap) {

    private lateinit var mBackend: ComponentMapper<BattleBackendComponent>
    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>
    private lateinit var mEnvObjTile: ComponentMapper<EnvObjTileComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mCollision: ComponentMapper<CollisionComponent>
    private lateinit var mNpcId: ComponentMapper<NpcIdComponent>
    private lateinit var mPlayerUnit: ComponentMapper<PlayerUnitComponent>
    private lateinit var mSideUiBox: ComponentMapper<SideUiBoxComponent>
    private lateinit var mUnitHealthbar: ComponentMapper<UnitHealthbarComponent>

    private lateinit var sTags: TagManager
    private lateinit var sAi: BattleAiSystem

    private var playerUnitIndex = 0

    companion object {
        const val UI_VERTICAL_PADDING = 4f
    }

    init {
        world.inject(this)
    }

    fun initialize() {
        val backendId = world.create()
        val cBackend = mBackend.create(backendId)
        cBackend.backend = BattleBackend(globals.weaponSchemaIndex, globals.skillIndex, npcList, envObjList, map, MapClearEndingRule())
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
                val fullEnvObj = cell.fullEnvObjId.fetch(cBackend.backend.getBattleState().envObjList) ?: continue
                handleEnvObj(cBackend.backend, fullEnvObj, x, y)
            }
        }
        sAi.initializeAi()
    }

    private fun handleEnvObj(backend: BattleBackend, envObj: EnvObject, x: Int, y: Int) {
        val id = world.create()
        val tilesetMetadata = envObj.getTilesetMetadata()

        when (tilesetMetadata) {
            is PccTilesetMetadata -> {
                val cEnvObjTile = mEnvObjTile.create(id)
                cEnvObjTile.tilesetMetadata = tilesetMetadata.deepCopy()
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat() + config.resolution.tileOffset
                val cAnimationFrames = mAnimationFrames.create(id)
                cAnimationFrames.frameMax = 3
                cAnimationFrames.tMax = 30
                cAnimationFrames.tStartNoise = 5

                val cCollision = mCollision.create(id)
                cCollision.bounds = CollisionBounds.CollisionBoundingBox(0f, 0f,
                        PccManager.PCC_WIDTH.toFloat(), PccManager.PCC_HEIGHT.toFloat())

                if (envObj is NpcEnvObject) {
                    mNpcId.create(id).npcId = envObj.npcId
                    if (backend.getNpcTeam(envObj.npcId) == Team.PLAYER) {
                        mPlayerUnit.create(id).index = playerUnitIndex
                        val uiId = world.create()
                        val cUi = mSideUiBox.create(uiId)
                        cUi.setToNpc(envObj.npcId, backend)
                        val cUiXy = mXy.create(uiId)
                        cUiXy.x = config.resolution.width - BattleUiSystem.UI_WIDTH + BattleUiSystem.SELECTION_DISTANCE
                        cUiXy.y = config.resolution.height - (BattleUiSystem.UI_HEIGHT + UI_VERTICAL_PADDING) * (playerUnitIndex + 1)
                        playerUnitIndex++
                    }
                }
            }
            is SingleTilesetMetadata -> {
                val cEnvObjTile = mEnvObjTile.create(id)
                cEnvObjTile.tilesetMetadata = tilesetMetadata.deepCopy()
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat()
                mAnimationFrames.create(id)
            }
            is MapTilesetMetadata -> {
                val cEnvObjTile = mEnvObjTile.create(id)
                cEnvObjTile.tilesetMetadata = tilesetMetadata.deepCopy()
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat()
                mAnimationFrames.create(id)
            }
            is AnimatedUnitTilesetMetadata -> {
                val cEnvObjTile = mEnvObjTile.create(id)
                cEnvObjTile.tilesetMetadata = tilesetMetadata.deepCopy()
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat() + config.resolution.tileOffset

                val cAnimationFrames = mAnimationFrames.create(id)
                cAnimationFrames.frameMax = 3
                cAnimationFrames.tMax = 30
                cAnimationFrames.tStartNoise = 5

                val cCollision = mCollision.create(id)
                cCollision.bounds = CollisionBounds.CollisionBoundingBox(0f, 0f,
                        AnimatedTilesetManager.TILE_WIDTH.toFloat(), AnimatedTilesetManager.TILE_HEIGHT.toFloat())

                if (envObj is NpcEnvObject) {
                    mNpcId.create(id).npcId = envObj.npcId
                    if (backend.getNpcTeam(envObj.npcId) == Team.PLAYER) {
                        mPlayerUnit.create(id).index = playerUnitIndex
                        playerUnitIndex++
                    } else {
                        val cUnitHealthbar = mUnitHealthbar.create(id)
                        cUnitHealthbar.setToNpc(envObj.npcId, backend)
                    }
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

}
