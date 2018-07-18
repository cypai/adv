package com.pipai.adv.backend.battle.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.pipai.adv.utils.DeepCopyable

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class InventoryItem : DeepCopyable<InventoryItem> {
    abstract val name: String

    data class WeaponInstance(override val name: String, var ammo: Int) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    data class EquipableItem(override val name: String) : InventoryItem() {
        override fun deepCopy() = copy()
    }

    data class MiscItem(override val name: String) : InventoryItem() {
        override fun deepCopy() = copy()
    }
}

data class InventorySlot(var item: InventoryItem?)
