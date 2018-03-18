package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.engine.log.MoveEvent
import com.pipai.adv.map.TileVisibility
import com.pipai.adv.utils.DirectionUtils
import com.pipai.adv.utils.GridUtils
import net.mostlyoriginal.api.event.common.EventSystem

class MoveAnimationHandler(val config: AdvConfig, world: World) {

    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mPath: ComponentMapper<PathInterpolationComponent>
    private lateinit var mAnimationFrames: ComponentMapper<AnimationFramesComponent>
    private lateinit var mEnvObjTile: ComponentMapper<EnvObjTileComponent>
    private lateinit var mCameraFollow: ComponentMapper<CameraFollowComponent>

    private lateinit var sNpcId: NpcIdSystem
    private lateinit var sCameraInterpolation: CameraInterpolationSystem
    private lateinit var sBattleMapRenderer: BattleMapRenderingSystem
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
            val cXy = mXy.get(entityId)

            val visiblePath = moveEvent.path.any { sBattleMapRenderer.fogOfWar.getPlayerTileVisibility(it) == TileVisibility.VISIBLE }
            if (visiblePath) {
                sCameraInterpolation.sendCameraToPosition(cXy.toVector2(), { animateMovement(entityId, moveEvent) })
            } else {
                val newPosition = GridUtils.gridPositionToLocalOffset(
                        moveEvent.path.last(),
                        config.resolution.tileSize.toFloat(),
                        0f,
                        config.resolution.tileOffset.toFloat())
                cXy.setXy(newPosition)
                sEvent.dispatch(BattleEventAnimationEndEvent(moveEvent))
            }
        }
    }

    private fun animateMovement(entityId: Int, moveEvent: MoveEvent) {
        mCameraFollow.create(entityId)
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
            mCameraFollow.remove(entityId)
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
