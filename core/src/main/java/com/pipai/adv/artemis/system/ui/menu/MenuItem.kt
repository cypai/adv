package com.pipai.adv.artemis.system.ui.menu

import com.badlogic.gdx.graphics.g2d.TextureRegion

interface MenuItem {
    val image: TextureRegion?
    val text: String
    val rightText: String
}

data class StringMenuItem(override val text: String,
                          override val image: TextureRegion?,
                          override val rightText: String) : MenuItem {

    private val dataStore: MutableMap<String, Any> = mutableMapOf()

    fun withData(key: String, data: Any): StringMenuItem {
        dataStore[key] = data
        return this
    }

    fun getData(key: String): Any? = dataStore[key]

}
