package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.utils.DirectionUtils
import com.pipai.adv.utils.GridUtils
import net.mostlyoriginal.api.event.common.EventSystem

class MoveAnimationHandler(val config: AdvConfig, world: World) {

    private lateinit var mPath: ComponentMapper<PathInterpolationComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mEnvObjTile: ComponentMapper<EnvObjTileComponent>

    private lateinit var sNpcId: NpcIdSystem
    private lateinit var sEvent: EventSystem

    private var movingEntityId: Int? = null
    private lateinit var previousDirection: Direction

    init {
        world.inject(this)
    }

    fun animate(moveEvent: MoveEvent) {
        val entityId = sNpcId.getNpcEntityId(moveEvent.npcId)
        movingEntityId = entityId

        if (entityId != null) {
            val cAnimationFrames = mAnimationFrames.get(entityId)
            setMovingAnimation(cAnimationFrames)
            val cPath = mPath.create(entityId)
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.clear()
            cPath.endpoints.addAll(moveEvent.path.map {
                GridUtils.gridPositionToLocalOffset(
                        it,
                        config.resolution.tileSize.toFloat(),
                        0f,
                        config.resolution.tileOffset.toFloat())
            })
            cPath.setUsingSpeed(4.0)
            cPath.onEndpoint = { onReachedEndpoint(it, moveEvent) }
            previousDirection = mEnvObjTile.get(entityId).direction
            onReachedEndpoint(cPath, moveEvent)
        }
    }

    private fun onReachedEndpoint(cPath: PathInterpolationComponent, originalEvent: MoveEvent) {
        val entityId = movingEntityId!!
        val cAnimationFrames = mAnimationFrames.get(entityId)
        val cEnvObjTile = mEnvObjTile.get(entityId)
        if (cPath.endpointIndex < cPath.endpoints.size - 1) {
            val position = cPath.endpoints[cPath.endpointIndex]
            val nextPosition = cPath.endpoints[cPath.endpointIndex + 1]
            val nextDirection = DirectionUtils.directionFor(position.x, position.y, nextPosition.x, nextPosition.y)
            if (previousDirection != nextDirection) {
                cEnvObjTile.direction = DirectionUtils.cardinalMoveDirection(cEnvObjTile.direction, nextDirection)
            }
            previousDirection = nextDirection
        } else {
            setIdlingAnimation(cAnimationFrames)
            sEvent.dispatch(BattleEventAnimationEndEvent(originalEvent))
        }
    }

    private fun setMovingAnimation(cAnimationFrames: AnimationFramesComponent) {
        cAnimationFrames.frameMax = 3
        cAnimationFrames.tMax = 15
        cAnimationFrames.tStartNoise = 0
    }

    private fun setIdlingAnimation(cAnimationFrames: AnimationFramesComponent) {
        cAnimationFrames.frameMax = 3
        cAnimationFrames.tMax = 30
        cAnimationFrames.tStartNoise = 5
    }

}
