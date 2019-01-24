package com.pipai.adv.artemis.system.misc

import com.artemis.EntitySubscription
import com.artemis.utils.IntBag
import com.pipai.adv.artemis.components.EnvObjIdComponent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.forEach
import com.pipai.adv.utils.mapper

class EnvObjIdSystem : NoProcessingSystem() {

    private val mEnvObjId by mapper<EnvObjIdComponent>()

    // envObjId -> entityId mapping
    private val envObjIdIndex: MutableMap<Int, Int> = mutableMapOf()

    override fun initialize() {
        world.aspectSubscriptionManager.get(allOf(EnvObjIdComponent::class)).addSubscriptionListener(object : EntitySubscription.SubscriptionListener {
            override fun inserted(entities: IntBag?) {
                entities?.forEach {
                    envObjIdIndex[mEnvObjId.get(it).envObjId] = it
                }
            }

            override fun removed(entities: IntBag?) {
                entities?.forEach {
                    envObjIdIndex.remove(mEnvObjId.get(it).envObjId)
                }
            }
        })
    }

    fun getEnvObjEntityId(envObjId: Int) = envObjIdIndex[envObjId]

}
