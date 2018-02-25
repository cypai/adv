package com.pipai.adv.artemis.system.input

import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.KeyDownEvent
import com.pipai.adv.artemis.events.MouseDownEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.MapGraph
import com.pipai.adv.utils.CollisionUtils
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.Subscribe

class SelectedUnitSystem : NoProcessingSystem() {

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mInterpolation by mapper<InterpolationComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mNpcId by mapper<NpcIdComponent>()
    private val mPlayerUnit by mapper<PlayerUnitComponent>()
    private val mXy by mapper<XYComponent>()
    private val mCollision by mapper<CollisionComponent>()

    private val sTags by system<TagManager>()

    // entityId of the selected unit
    var selectedUnit: Int? = null
        private set

    var selectedMapGraph: MapGraph? = null
        private set

    @Subscribe
    fun mouseDownListener(event: MouseDownEvent) {
        val playerUnitEntities = fetchPlayerUnits()

        var minY = Float.MAX_VALUE
        var minYId: Int? = null
        for (entityId in playerUnitEntities) {
            val cXy = mXy.get(entityId)
            val cCollision = mCollision.get(entityId)
            if (cXy.y < minY && CollisionUtils.withinBounds(event.x, event.y, cXy.x, cXy.y, cCollision.bounds)) {
                minY = cXy.y
                minYId = entityId
            }
        }
        if (minYId != null) {
            select(minYId)
        }
    }

    @Subscribe
    fun keyDownListener(event: KeyDownEvent) {
        if (event.keycode == Input.Keys.SHIFT_LEFT) {
            selectNext()
        }
    }

    private fun fetchPlayerUnits(): List<Int> {
        val playerUnitEntityBag = world.aspectSubscriptionManager.get(allOf(
                NpcIdComponent::class, PlayerUnitComponent::class, XYComponent::class, CollisionComponent::class)).entities
        return playerUnitEntityBag.data.slice(0 until playerUnitEntityBag.size())
    }

    private fun select(playerUnitEntityId: Int?) {
        selectedUnit = playerUnitEntityId
        if (playerUnitEntityId != null) {
            val cPlayerXy = mXy.get(playerUnitEntityId)

            val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
            val cCamera = mCamera.get(cameraId)
            val cInterpolation = mInterpolation.create(cameraId)
            cInterpolation.interpolation = Interpolation.sineOut
            cInterpolation.start.x = cCamera.camera.position.x
            cInterpolation.start.y = cCamera.camera.position.y
            cInterpolation.end.x = cPlayerXy.x
            cInterpolation.end.y = cPlayerXy.y
            cInterpolation.maxT = 20

            val backend = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend
            val battleState = backend.getBattleState()
            val npcId = mNpcId.get(playerUnitEntityId).npcId
            val unitInstance = battleState.npcList.getNpc(npcId)!!.unitInstance
            val mapGraph = MapGraph(backend.getBattleMapState(),
                    backend.getNpcPositions()[npcId]!!,
                    unitInstance.schema.baseStats.mobility,
                    battleState.apState.getNpcAp(npcId), ActionPointState.startingNumAPs)
            selectedMapGraph = mapGraph
        }
    }

    fun selectNext() {
        val playerUnits = fetchPlayerUnits()
                .map { Pair(mPlayerUnit.get(it).index, it) }
                .sortedBy { it.first }
        val currentSelectedUnit = selectedUnit
        if (currentSelectedUnit == null) {
            select(playerUnits.firstOrNull()?.second)
        } else {
            val currentIndex = mPlayerUnit.get(currentSelectedUnit).index
            val next = playerUnits.firstOrNull { it.first > currentIndex }
            if (next == null) {
                select(playerUnits.minBy { it.first }?.second)
            } else {
                select(next.second)
            }
        }
    }
}
