package com.pipai.adv.artemis.system.input

import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.*
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.MapGraph
import com.pipai.adv.utils.CollisionUtils
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class SelectedUnitSystem : NoProcessingSystem() {

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mPath by mapper<PathInterpolationComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mNpcId by mapper<NpcIdComponent>()
    private val mPlayerUnit by mapper<PlayerUnitComponent>()
    private val mXy by mapper<XYComponent>()
    private val mCollision by mapper<CollisionComponent>()

    private val sNpcId by system<NpcIdSystem>()
    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    // npcId of the selected unit
    var selectedUnit: Int? = null
        private set

    @Subscribe
    fun mouseDownListener(event: MouseDownEvent) {
        if (event.button != Input.Buttons.LEFT) return

        val npcUnitEntities = fetchNpcUnits()

        var minY = Float.MAX_VALUE
        var minYId: Int? = null
        for (entityId in npcUnitEntities) {
            val cXy = mXy.get(entityId)
            val cCollision = mCollision.get(entityId)
            if (cXy.y < minY && CollisionUtils.withinBounds(event.x, event.y, cXy.x, cXy.y, cCollision.bounds)) {
                minY = cXy.y
                minYId = entityId
            }
        }
        if (minYId != null) {
            val npcId = mNpcId.get(minYId).npcId
            if (selectedUnit != npcId) {
                select(npcId)
            }
        }
    }

    @Subscribe
    fun keyDownListener(event: KeyDownEvent) {
        if (event.keycode == Input.Keys.SHIFT_LEFT) {
            selectNext()
        }
    }

    private fun fetchNpcUnits(): List<Int> {
        val npcUnitEntityBag = world.aspectSubscriptionManager.get(allOf(
                NpcIdComponent::class, XYComponent::class, CollisionComponent::class)).entities
        return npcUnitEntityBag.data.slice(0 until npcUnitEntityBag.size())
    }

    private fun fetchPlayerUnits(): List<Int> {
        val playerUnitEntityBag = world.aspectSubscriptionManager.get(allOf(
                NpcIdComponent::class, PlayerUnitComponent::class, XYComponent::class, CollisionComponent::class)).entities
        return playerUnitEntityBag.data.slice(0 until playerUnitEntityBag.size())
    }

    private fun select(npcId: Int?) {
        val backend = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

        selectedUnit?.let {
            val currentSelectedTeam = backend.getNpcTeams()[it]!!
            when (currentSelectedTeam) {
                Team.PLAYER -> sEvent.dispatch(PlayerUnitUnselectedEvent(it))
                Team.AI -> sEvent.dispatch(NonPlayerUnitUnselectedEvent(it))
            }
        }

        npcId?.let {
            val nextSelectedTeam = backend.getNpcTeams()[it]!!
            when (nextSelectedTeam) {
                Team.PLAYER -> sEvent.dispatch(PlayerUnitSelectedEvent(it))
                Team.AI -> sEvent.dispatch(NonPlayerUnitSelectedEvent(it))
            }
            val unitEntityId = sNpcId.getNpcEntityId(npcId)
            if (unitEntityId != null) {
                val cUnitXy = mXy.get(unitEntityId)

                val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
                val cCamera = mCamera.get(cameraId)
                val cInterpolation = mPath.create(cameraId)
                cInterpolation.interpolation = Interpolation.sineOut
                cInterpolation.endpoints.clear()
                cInterpolation.endpoints.add(Vector2(cCamera.camera.position.x, cCamera.camera.position.y))
                cInterpolation.endpoints.add(Vector2(cUnitXy.x, cUnitXy.y))
                cInterpolation.t = 0
                cInterpolation.maxT = 20

                if (nextSelectedTeam == Team.PLAYER) {
                    val battleState = backend.getBattleState()
                    val unitInstance = battleState.npcList.getNpc(npcId)!!.unitInstance
                    val mapGraph = MapGraph(backend.getBattleMapState(),
                            backend.getNpcPositions()[npcId]!!,
                            unitInstance.schema.baseStats.mobility,
                            battleState.apState.getNpcAp(npcId), ActionPointState.startingNumAPs)
                    sEvent.dispatch(MovementTileUpdateEvent(mapGraph))
                } else {
                    sEvent.dispatch(MovementTileUpdateEvent(null))
                }
            }
        }
        selectedUnit = npcId
    }

    fun selectNext() {
        val playerUnits = fetchPlayerUnits()
                .map { Pair(mPlayerUnit.get(it).index, mNpcId.get(it).npcId) }
                .sortedBy { it.first }
        val currentSelectedUnit = selectedUnit
        if (currentSelectedUnit == null) {
            select(playerUnits.firstOrNull()?.second)
        } else {
            val currentIndex = mPlayerUnit.getSafe(sNpcId.getNpcEntityId(currentSelectedUnit)!!, null)?.index
            if (currentIndex != null) {
                val next = playerUnits.firstOrNull { it.first > currentIndex }
                if (next == null) {
                    select(playerUnits.minBy { it.first }?.second)
                } else {
                    select(next.second)
                }
            } else {
                select(playerUnits.firstOrNull()?.second)
            }
        }
    }
}
