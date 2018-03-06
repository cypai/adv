package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.pipai.adv.utils.MathUtils

class XYComponent : Component() {
    var x = 0f
    var y = 0f

    fun setXy(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    fun setXy(vec: Vector2) {
        this.x = vec.x
        this.y = vec.y
    }

    fun toVector2() = Vector2(x, y)
}

class PathInterpolationComponent : Component() {
    lateinit var interpolation: Interpolation
    var speed = 0.0
    var t = 0
    var maxT = 0
    var tIncrement = 1

    var onEndpoint: ((PathInterpolationComponent) -> Unit)? = null
    var onEnd = PathInterpolationEndStrategy.REMOVE

    val endpoints: MutableList<Vector2> = mutableListOf()
    var endpointIndex = 0

    fun getCurrentPos(): Vector2 {
        val a = t.toFloat() / maxT.toFloat()
        val start = endpoints[endpointIndex]
        val end = endpoints[endpointIndex + 1]
        return Vector2(
                interpolation.apply(start.x, end.x, a),
                interpolation.apply(start.y, end.y, a))
    }

    fun setUsingSpeed(speed: Double) {
        this.speed = speed
        if (endpointIndex < endpoints.size - 1) {
            val start = endpoints[endpointIndex]
            val end = endpoints[endpointIndex + 1]
            val distance = MathUtils.distance(start.x, start.y, end.x, end.y)
            val time = distance / speed
            t = 0
            maxT = time.toInt()
        }
    }
}

enum class PathInterpolationEndStrategy {
    REMOVE, DESTROY, RESTART
}

class OrthographicCameraComponent : Component() {

    val camera: OrthographicCamera = OrthographicCamera(Gdx.graphics.getWidth().toFloat(), Gdx.graphics.getHeight().toFloat())

    init {
        camera.setToOrtho(false, Gdx.graphics.getWidth().toFloat(), Gdx.graphics.getHeight().toFloat())
    }

}

class CameraFollowComponent : Component()
