package com.badlogic.gdx.scenes.scene2d.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
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
    var keySelection = false
    var disabledFontColor = style.fontColorSelected
    var confirmSelectionDrawable = style.selection

    private var confirmCallbacks: MutableList<(T) -> Unit> = mutableListOf()

    internal val items: MutableList<T> = mutableListOf()
    internal var itemHeight: Float = 0f
    internal var textOffsetX: Float = 0f
    internal var textOffsetY: Float = 0f
    internal var prefWidth: Float = 0f
    internal var prefHeight: Float = 0f

    internal var confirmedSelection: Int? = null
    internal val selection: MutableList<Int> = mutableListOf()
    internal val disabledItems: MutableList<Int> = mutableListOf()
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
        setSize(getPrefWidth(), getPrefHeight())

        addListener(object : InputListener() {
            override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                if (keycode == Input.Keys.A && UIUtils.ctrl()) {
                    selection.clear()
                    selection.addAll(0 until items.size)
                    return true
                }
                if (keySelection) {
                    val index = selection.firstOrNull()
                    when (keycode) {
                        Input.Keys.UP -> {
                            if (index == null) {
                                selection.add(items.size - 1)
                            } else {
                                selection.clear()
                                selection.add(if (index == 0) items.size - 1 else index - 1)
                            }
                        }
                        Input.Keys.DOWN -> {
                            if (index == null) {
                                selection.add(0)
                            } else {
                                selection.clear()
                                selection.add(if (index == items.size - 1) 0 else index + 1)
                            }
                        }
                        Input.Keys.ENTER -> {
                            confirmedSelection = index
                            if (index != null) {
                                confirmCallbacks.forEach { it.invoke(getSelected()!!) }
                            }
                        }
                    }
                }
                return false
            }

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (pointer == 0 && button != 0) return false
                this@ImageList.touchDown(y, true)
                return true
            }

            override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
                if (hoverSelect) {
                    this@ImageList.touchDown(y, false)
                }
                return false
            }
        })
    }

    fun lockMenu() {
        lockSelection = true
        selection.clear()
    }

    fun unlockMenu() {
        lockSelection = false
    }

    fun addConfirmCallback(callback: (T) -> Unit) {
        confirmCallbacks.add(callback)
    }

    fun clearConfirmCallbacks() {
        confirmCallbacks.clear()
    }

    private fun touchDown(touchY: Float, isConfirm: Boolean) {
        if (lockSelection) return
        var selectY = touchY
        if (items.size == 0) return
        var height = height
        if (style.background != null) {
            height -= style.background.topHeight + style.background.bottomHeight
            selectY -= style.background.bottomHeight
        }
        val index = ((height - selectY) / itemHeight).toInt()
        if (index < 0 || index >= items.size) return
        if (!disabledItems.contains(index)) {
            selection.clear()
            selection.add(index)
            if (isConfirm) {
                confirmedSelection = index
                confirmCallbacks.forEach { it.invoke(getSelected()!!) }
            }
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
            layout.setText(font, itemView.getItemText(item) + itemView.getItemRightText(item))
            val itemWidth = imageWidth + itemView.getSpacing() + layout.width + itemView.getMinCenterSpacing()
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

            if (confirmedSelection == i) {
                confirmSelectionDrawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight)
                font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha)
            } else if (selection.contains(i)) {
                selectedDrawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight)
                font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha)
            } else {
                font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha)
            }
            val disabled = disabledItems.contains(i)
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
        font.draw(batch, rightText, x + width - glyphLayout.width - itemView.getRightSpacing(), y + textOffsetY)
    }

    fun setItems(newItems: Array<T>) {
        val oldPrefWidth = getPrefWidth()
        val oldPrefHeight = getPrefHeight()

        items.clear()
        items.addAll(newItems)
        confirmedSelection = null
        selection.clear()
        disabledItems.clear()
        lockSelection = false

        invalidate()
        if (oldPrefWidth != getPrefWidth() || oldPrefHeight != getPrefHeight()) invalidateHierarchy()
    }

    fun setItems(newItems: Iterable<T>) {
        setItems(ArrayUtils.iterableToArray(newItems))
    }

    fun clearItems() {
        if (items.size == 0) return
        items.clear()
        selection.clear()
        invalidateHierarchy()
    }

    fun getSelected(): T? {
        return confirmedSelection?.let { items[it] } ?: return null
    }

    fun setSelected(item: T) {
        if (!lockSelection) {
            selection.clear()
            if (items.contains(item)) {
                selection.add(items.indexOf(item))
            }
        }
    }

    fun clearSelection() {
        selection.clear()
    }

    fun setSelectedIndex(index: Int) {
        if (!lockSelection) {
            if (index < -1 || index >= items.size)
                throw IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index)
            if (index == -1) {
                selection.clear()
            } else {
                selection.clear()
                selection.add(index)
            }
        }
    }

    fun setConfirmed(item: T) {
        if (!lockSelection) {
            setSelected(item)
            confirmedSelection = items.indexOf(item)
            confirmCallbacks.forEach { it.invoke(getSelected()!!) }
        }
    }

    fun setConfirmIndex(index: Int) {
        if (!lockSelection) {
            setSelectedIndex(index)
            confirmedSelection = index
            confirmCallbacks.forEach { it.invoke(getSelected()!!) }
        }
    }

    fun setDisabled(item: T, disabled: Boolean) {
        if (items.contains(item)) {
            if (disabled) {
                disabledItems.add(items.indexOf(item))
            } else {
                disabledItems.remove(items.indexOf(item))
            }
        }
    }

    fun setDisabledIndex(index: Int, disabled: Boolean) {
        if (index < -1 || index >= items.size)
            throw IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index)
        if (disabled) {
            disabledItems.add(index)
        } else {
            disabledItems.remove(index)
        }
    }

    fun setDisabledPredicate(predicate: (T) -> Boolean) {
        items.forEach {
            setDisabled(it, predicate(it))
        }
    }

    fun disableAll() {
        disabledItems.addAll(0 until items.size)
    }

    fun enableAll() {
        disabledItems.clear()
    }

    interface ImageListItemView<in T> {
        fun getItemImage(item: T): TextureRegion?
        fun getItemText(item: T): String
        fun getItemRightText(item: T): String = ""
        fun getSpacing(): Float = 0f
        fun getMinCenterSpacing(): Float = 0f
        fun getRightSpacing(): Float = 0f
    }
}
