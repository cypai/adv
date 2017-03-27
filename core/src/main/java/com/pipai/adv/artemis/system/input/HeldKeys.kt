package com.pipai.adv.artemis.system.input

class HeldKeys {
    private val heldKeys: MutableMap<Int, Boolean> = mutableMapOf()

    fun keyDown(keycode: Int) {
        heldKeys.put(keycode, true)
    }

    fun keyUp(keycode: Int) {
        heldKeys.put(keycode, false)
    }

    fun isDown(keycode: Int): Boolean {
        return heldKeys.getOrDefault(keycode, false)
    }
}
