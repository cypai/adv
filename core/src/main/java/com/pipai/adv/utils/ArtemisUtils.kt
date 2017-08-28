package com.pipai.adv.utils

import com.artemis.Aspect
import com.artemis.BaseSystem
import com.artemis.World

fun World.fetch(aspects: Aspect.Builder): List<Int> {
    val entityBag = this.aspectSubscriptionManager.get(aspects).entities
    return entityBag.data.slice(0 until entityBag.size())
}

fun <T : BaseSystem> World.getSystemSafe(type: Class<out T>): T? {
    return this.getSystem(type)
}
