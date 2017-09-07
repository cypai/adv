package com.pipai.adv.artemis.system.collision

import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.CollisionComponent
import com.pipai.adv.artemis.components.WallCollisionFlagComponent
import com.pipai.adv.artemis.components.WallComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.require
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.CollisionUtils

class NpcCollisionSystem : IteratingSystem(allOf()) {

    private val mXy by require<XYComponent>()
    private val mCollision by require<CollisionComponent>()
    private val mWallCollisionFlag by require<WallCollisionFlagComponent>()

    override protected fun process(entityId: Int) {
        val wallEntityBag = world.fetch(allOf(CollisionComponent::class, XYComponent::class, WallComponent::class))

        val cXy = mXy.get(entityId)
        val cCollision = mCollision.get(entityId)

        for (wallEntity in wallEntityBag) {
            if (wallEntity == entityId) {
                continue
            }
            val cWallXy = mXy.get(wallEntity)
            val cWallCollision = mCollision.get(wallEntity)
            val mtv = CollisionUtils.minimumTranslationVector(cXy.x, cXy.y, cCollision.bounds, cWallXy.x, cWallXy.y, cWallCollision.bounds)
            cXy.x += mtv.x
            cXy.y += mtv.y
        }
    }

}
