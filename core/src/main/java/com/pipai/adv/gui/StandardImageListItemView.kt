package com.pipai.adv.gui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem

class StandardImageListItemView : ImageList.ImageListItemView<StringMenuItem> {
    override fun getItemImage(item: StringMenuItem): TextureRegion? = null
    override fun getItemText(item: StringMenuItem): String = item.text
    override fun getItemRightText(item: StringMenuItem): String = item.rightText
    override fun getSpacing(): Float = 10f
    override fun getRightSpacing(): Float = 16f
}
