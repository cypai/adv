package com.pipai.adv.artemis.system.ui.menu

import com.badlogic.gdx.graphics.g2d.TextureRegion

interface MenuItem {
    val image: TextureRegion?
    val text: String
    val rightText: String
}

data class StringMenuItem(override val text: String,
                          override val image: TextureRegion?,
                          override val rightText: String) : MenuItem
