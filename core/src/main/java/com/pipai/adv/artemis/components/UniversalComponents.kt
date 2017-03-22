package com.pipai.adv.artemis.components

import com.artemis.Component

class XYComponent : Component() {
    var x = 0f
    var y = 0f

    fun setXy(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}
