package com.pipai.adv.artemis.system.init

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.npc.NpcList
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata

@Wire
class BattleMapScreenInit(private val world: World, private val config: AdvConfig,
                          private val npcList: NpcList, private val map: BattleMap) {

    private lateinit var mBackend: ComponentMapper<BattleBackendComponent>
    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>
    private lateinit var mPccs: ComponentMapper<PccComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        val backendId = world.create()
        val cBackend = mBackend.create(backendId)
        cBackend.backend = BattleBackend(npcList, map)

        val cameraId = world.create()
        val camera = mCamera.create(cameraId).camera
        sTags.register(Tags.CAMERA.toString(), cameraId)
        camera.zoom = world.getSystem(ZoomInputSystem::class.java).currentZoom()

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
                val cPcc = mPccs.create(id)
                cPcc.pccs.addAll(tilesetMetadata.pccMetadata)
                val cXy = mXy.create(id)
                cXy.x = config.resolution.tileSize * x.toFloat()
                cXy.y = config.resolution.tileSize * y.toFloat()
                val cAnimationFrames = mAnimationFrames.create(id)
                cAnimationFrames.frameMax = 3
                cAnimationFrames.tMax = 30
                cAnimationFrames.tStartNoise = 5
            }
            else -> {
                // Do nothing
            }
        }
    }

}
