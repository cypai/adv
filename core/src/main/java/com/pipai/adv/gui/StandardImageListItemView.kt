package com.pipai.adv.gui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.pipai.adv.artemis.system.ui.menu.MenuItem

class StandardImageListItemView<in T : MenuItem> : ImageList.ImageListItemView<T> {
    override fun getItemImage(item: T): TextureRegion? = null
    override fun getItemText(item: T): String = item.text
    override fun getItemRightText(item: T): String = item.rightText
    override fun getSpacing(): Float = 10f
    override fun getRightSpacing(): Float = 16f
}
