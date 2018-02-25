package com.pipai.adv.artemis.system.input

import com.badlogic.gdx.Input
import com.pipai.adv.artemis.components.CollisionComponent
import com.pipai.adv.artemis.components.NpcIdComponent
import com.pipai.adv.artemis.components.PlayerUnitComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.events.KeyDownEvent
import com.pipai.adv.artemis.events.MouseDownEvent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.CollisionUtils
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import net.mostlyoriginal.api.event.common.Subscribe

class SelectedUnitSystem : NoProcessingSystem() {

    private val mNpcId by mapper<NpcIdComponent>()
    private val mPlayerUnit by mapper<PlayerUnitComponent>()
    private val mXy by mapper<XYComponent>()
    private val mCollision by mapper<CollisionComponent>()

    // entityId of the selected unit
    var selectedUnit: Int? = null
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
        if (playerUnitEntityId == null) {
            System.out.println("Cleared selection")
        } else {
            System.out.println("Selected ${mNpcId.get(playerUnitEntityId).npcId}")
        }
    }

    private fun selectNext() {
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