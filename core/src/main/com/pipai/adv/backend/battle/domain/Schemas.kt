package com.pipai.adv.backend.battle.domain

data class UnitSchema(
        val name: String,
        val hp: Int,
        val mp: Int,
        val strength: Int,
        val dexterity: Int,
        val constitution: Int,
        val intelligence: Int,
        val resistance: Int,
        val avoid: Int,
        val mobility: Int)

enum class WeaponType {
    BOW, SWORD
}

data class WeaponSchema(
        val name: String,
        val type: WeaponType,
        val atk: Int,
        val rarity: Int)
