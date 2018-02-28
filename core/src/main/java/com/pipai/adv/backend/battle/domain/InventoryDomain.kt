package com.pipai.adv.backend.battle.domain

import com.pipai.adv.utils.DeepCopyable

sealed class InventoryItem : DeepCopyable<InventoryItem> {
    data class WeaponInstance(val schema: WeaponSchema, var ammo: Int) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    sealed class ConsumableItem {
        class Potion
    }
}
