package com.badlogic.gdx.scenes.scene2d.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection
import com.badlogic.gdx.scenes.scene2d.utils.Cullable
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools
import com.pipai.adv.utils.ArrayUtils

open class ImageList<T>(internal var style: List.ListStyle, private val itemView: ImageListItemView<T>) : Widget(), Cullable {

    constructor(skin: Skin, itemView: ImageListItemView<T>)
            : this(skin.get(List.ListStyle::class.java), itemView)

    constructor(skin: Skin, styleName: String, itemView: ImageListItemView<T>)
            : this(skin.get(styleName, List.ListStyle::class.java), itemView)

    var hoverSelect = false
    var lockSelection = false
    var disabledFontColor = style.fontColorSelected

    internal val items = com.badlogic.gdx.utils.Array<T>()
    internal var itemHeight: Float = 0f
    internal var textOffsetX: Float = 0f
    internal var textOffsetY: Float = 0f
    internal var prefWidth: Float = 0f
    internal var prefHeight: Float = 0f

    internal val selection: ArraySelection<T> = ArraySelection(items)
    internal val disabledItems = com.badlogic.gdx.utils.Array<T>()
    internal var culling: Rectangle? = null

    override fun setCullingArea(cullingArea: Rectangle?) {
        this.culling = cullingArea
    }

    override fun getPrefWidth(): Float {
        validate()
        return prefWidth
    }

    override fun getPrefHeight(): Float {
        validate()
        return prefHeight
    }

    init {
        selection.setActor(this)
        selection.required = true

        setSize(getPrefWidth(), getPrefHeight())

        addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.A && UIUtils.ctrl() && selection.getMultiple()) {
                    selection.clear()
                    selection.addAll(items)
                    return true
                }
                return false
            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (pointer == 0 && button != 0) return false
                if (selection.isDisabled) return false
                if (selection.multiple) stage.keyboardFocus = this@ImageList
                this@ImageList.touchDown(y)
                return true
            }

            override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
                if (hoverSelect) {
                    this@ImageList.touchDown(y)
                }
                return false
            }
        })
    }

    private fun touchDown(touchY: Float) {
        if (lockSelection) return
        var selectY = touchY
        if (items.size == 0) return
        var height = height
        if (style.background != null) {
            height -= style.background.topHeight + style.background.bottomHeight
            selectY -= style.background.bottomHeight
        }
        var index = ((height - selectY) / itemHeight).toInt()
        index = Math.max(0, index)
        index = Math.min(items.size - 1, index)
        val item = items[index]
        if (!disabledItems.contains(item)) {
            selection.choose(item)
        }
    }

    override fun layout() {
        val font = style.font
        val selectedDrawable = style.selection

        itemHeight = font.capHeight - font.descent * 2
        itemHeight += selectedDrawable.topHeight + selectedDrawable.bottomHeight

        textOffsetX = selectedDrawable.leftWidth
        textOffsetY = selectedDrawable.topHeight - font.descent

        prefWidth = 0f
        val layoutPool = Pools.get(GlyphLayout::class.java)
        val layout = layoutPool.obtain()
        for (item in items) {
            val image = itemView.getItemImage(item)
            var imageWidth = 0f
            if (image != null) {
                val scale = font.lineHeight / image.regionHeight
                imageWidth = image.regionWidth * scale
            }
            layout.setText(font, itemView.getItemText(item))
            val itemWidth = imageWidth + itemView.getSpacing() + layout.width
            prefWidth = Math.max(itemWidth, prefWidth)
        }
        layoutPool.free(layout)
        prefWidth += selectedDrawable.leftWidth + selectedDrawable.rightWidth
        prefHeight = items.size * itemHeight

        val background = style.background
        if (background != null) {
            prefWidth += background.leftWidth + background.rightWidth
            prefHeight += background.topHeight + background.bottomHeight
        }
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        validate()

        val font = style.font
        val selectedDrawable = style.selection
        val fontColorSelected = style.fontColorSelected
        val fontColorUnselected = style.fontColorUnselected

        val color = color
        batch!!.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        var x = x
        val y = y
        var width = width
        val height = height
        var itemY = height

        val background = style.background
        if (background != null) {
            background.draw(batch, x, y, width, height)
            val leftWidth = background.leftWidth
            x += leftWidth
            itemY -= background.topHeight
            width -= leftWidth + background.rightWidth
        }

        font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha)
        for (i in 0 until items.size) {
            val item = items.get(i)
            val selected = selection.contains(item)
            font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha)
            if (selected) {
                selectedDrawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight)
                font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha)
            }
            val disabled = disabledItems.contains(item, false)
            if (disabled) {
                font.setColor(disabledFontColor.r, disabledFontColor.g, disabledFontColor.b, disabledFontColor.a * parentAlpha)
            }
            drawItem(batch, font, item, x, y + itemY, textOffsetX, -textOffsetY, width)
            itemY -= itemHeight
        }
    }

    private fun drawItem(batch: Batch, font: BitmapFont, item: T, x: Float, y: Float, textOffsetX: Float, textOffsetY: Float, width: Float) {
        val image = itemView.getItemImage(item)
        val text = itemView.getItemText(item)

        if (image != null) {
            val scale = font.lineHeight / image.regionHeight
            val imageWidth = image.regionWidth * scale
            batch.draw(image, x, y - font.lineHeight, imageWidth, font.lineHeight)
        }

        font.draw(batch, text, x + textOffsetX + itemView.getSpacing(), y + textOffsetY)

        val rightText = itemView.getItemRightText(item)
        val glyphLayout = Pools.get(GlyphLayout::class.java).obtain()
        glyphLayout.setText(font, rightText)
        font.draw(batch, rightText, width - glyphLayout.width - 2, y + textOffsetY)
    }

    fun setItems(newItems: Array<T>) {
        val oldPrefWidth = getPrefWidth()
        val oldPrefHeight = getPrefHeight()

        items.clear()
        items.addAll(newItems)
        selection.validate()
        disabledItems.clear()
        lockSelection = false

        invalidate()
        if (oldPrefWidth != getPrefWidth() || oldPrefHeight != getPrefHeight()) invalidateHierarchy()
    }

    fun setItems(newItems: Iterable<T>) {
        setItems(ArrayUtils.iterableToArray(newItems))
    }

    fun getSelected(): T {
        return selection.first()
    }

    fun setSelected(item: T) {
        if (!lockSelection) {
            if (items.contains(item, false))
                selection.set(item)
            else if (selection.required && items.size > 0)
                selection.set(items.first())
            else
                selection.clear()
        }
    }

    fun setSelectedIndex(index: Int) {
        if (!lockSelection) {
            if (index < -1 || index >= items.size)
                throw IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index)
            if (index == -1) {
                selection.clear()
            } else {
                selection.set(items.get(index))
            }
        }
    }

    fun setDisabled(item: T, disabled: Boolean) {
        if (items.contains(item, false)) {
            if (disabled) {
                disabledItems.add(item)
            } else {
                disabledItems.removeValue(item, false)
            }
        }
    }

    fun setDisabledIndex(index: Int, disabled: Boolean) {
        if (index < -1 || index >= items.size)
            throw IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index)
        val item = items[index]
        if (disabled) {
            disabledItems.add(item)
        } else {
            disabledItems.removeValue(item, false)
        }
    }

    interface ImageListItemView<in T> {
        fun getItemImage(item: T): TextureRegion?
        fun getItemText(item: T): String
        fun getItemRightText(item: T): String = ""
        fun getSpacing(): Float = 0f
    }
}
