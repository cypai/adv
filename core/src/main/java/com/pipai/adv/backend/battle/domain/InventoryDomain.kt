package com.pipai.adv.backend.battle.domain

import com.pipai.adv.utils.DeepCopyable

sealed class InventoryItem : DeepCopyable<InventoryItem> {
    data class WeaponInstance(val name: String, var ammo: Int) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    data class EquipableItem(val name: String) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    data class ConsumableItem(val name: String) : InventoryItem() {
        override fun deepCopy() = copy()
    }
}
