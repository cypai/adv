package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2

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
}

class PathInterpolationComponent : Component() {
    lateinit var interpolation: Interpolation
    var t = 0
    var maxT = 0
    var tIncrement = 1

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
