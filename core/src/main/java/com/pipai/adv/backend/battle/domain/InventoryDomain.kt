package com.pipai.adv.backend.battle.domain

import com.pipai.utils.DeepCopyable

sealed class InventoryItem : DeepCopyable<InventoryItem> {
    data class WeaponInstance(val schema: WeaponSchema) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    sealed class ConsumableItem {
        class Potion
    }
}
