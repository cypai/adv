package com.pipai.adv.artemis.system.init

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.BattleMapScreenTags
import com.pipai.adv.artemis.screens.UniversalTags
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.FullEnvironmentObject.NpcEnvironmentObject
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.npc.NpcList
import com.pipai.adv.tiles.EnvObjTilesetType
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.screens.CharacterTags

@Wire
class GuildScreenInit(private val world: World, private val config: AdvConfig,
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
        // This backend is just for rendering the BattleMap, there is no real battle happening
        val backendId = world.create()
        val cBackend = mBackend.create(backendId)
        cBackend.backend = BattleBackend(npcList, map)

        val cameraId = world.create()
        val cCamera = mCamera.create(cameraId)
        // Hard-coded position for the camera on the player character's spawn location
        cCamera.camera.position.x = config.resolution.tileSize * 1.5f
        cCamera.camera.position.y = config.resolution.tileSize * 1.5f
        sTags.register(BattleMapScreenTags.CAMERA.toString(), cameraId)

        val uiCameraId = world.create()
        mCamera.create(uiCameraId)
        sTags.register(UniversalTags.UI_CAMERA.toString(), uiCameraId)

        npcList.forEach { addPcc(it.key, it.key + 1, 1) }
    }

    private fun addPcc(npcId: Int, x: Int, y: Int) {
        val entityId = world.create()

        if (npcId == 0) {
            sTags.register(CharacterTags.CONTROLLABLE_CHARACTER.toString(), entityId)
        }

        val tilesetMetadata = npcList.getNpc(npcId).tilesetMetadata

        val cPcc = mPccs.create(entityId)
        cPcc.pccs.addAll(tilesetMetadata.pccMetadata!!)
        val cXy = mXy.create(entityId)
        cXy.x = config.resolution.tileSize * x.toFloat()
        cXy.y = config.resolution.tileSize * y.toFloat()

        mAnimationFrames.create(entityId)
    }

}
