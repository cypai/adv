package com.pipai.adv.artemis.system.init

import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.screens.BattleMapScreenTags
import com.pipai.adv.artemis.screens.UniversalTags
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.save.NpcList

@Wire
class BattleMapScreenInit(private val world: World, private val npcList: NpcList, private val map: BattleMap) {

    private lateinit var mBackend: ComponentMapper<BattleBackendComponent>
    private lateinit var mCamera: ComponentMapper<OrthographicCameraComponent>

    private lateinit var sTags: TagManager

    init {
        world.inject(this)
    }

    fun initialize() {
        val backendId = world.create()
        val cBackend = mBackend.create(backendId)
        cBackend.backend = BattleBackend(npcList, map)

        val cameraId = world.create()
        mCamera.create(cameraId)
        sTags.register(BattleMapScreenTags.CAMERA.toString(), cameraId)

        val uiCameraId = world.create()
        mCamera.create(uiCameraId)
        sTags.register(UniversalTags.UI_CAMERA.toString(), uiCameraId)
    }

}
