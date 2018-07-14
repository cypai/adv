package com.pipai.adv.backend.battle.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.pipai.adv.utils.DeepCopyable

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class InventoryItem : DeepCopyable<InventoryItem> {
    data class WeaponInstance(val name: String, var ammo: Int) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    data class EquipableItem(val name: String) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    data class MiscItem(val name: String) : InventoryItem() {
        override fun deepCopy() = copy()
    }
}
