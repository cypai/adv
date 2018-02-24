package com.badlogic.gdx.scenes.scene2d.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.utils.ArraySelection
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.Disableable
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools

class ImageSelectBox<T>(private var style: SelectBox.SelectBoxStyle, private val itemView: ImageList.ImageListItemView<T>) : Widget(), Disableable {

    constructor(skin: Skin, itemView: ImageList.ImageListItemView<T>)
            : this(skin.get<SelectBox.SelectBoxStyle>(SelectBox.SelectBoxStyle::class.java), itemView)

    constructor(skin: Skin, styleName: String, itemView: ImageList.ImageListItemView<T>)
            : this(skin.get<SelectBox.SelectBoxStyle>(styleName, SelectBox.SelectBoxStyle::class.java), itemView)

    val items: Array<T> = Array()
    val selection: ArraySelection<T> = ArraySelection(items)
    private var selectBoxList: ImageSelectBoxList<T> = ImageSelectBoxList(this, itemView)
    private var prefWidth: Float = 0.toFloat()
    private var prefHeight: Float = 0.toFloat()
    private var clickListener: ClickListener
    internal var disabled: Boolean = false
    private val layout = GlyphLayout()

    var maxListCount: Int
        get() = selectBoxList.maxListCount
        set(maxListCount) {
            selectBoxList.maxListCount = maxListCount
        }

    var selected: T
        get() = selection.first()
        set(item) = if (items.contains(item, false))
            selection.set(item)
        else if (items.size > 0)
            selection.set(items.first())
        else
            selection.clear()

    var selectedIndex: Int
        get() {
            val selected = selection.items()
            return if (selected.size == 0) -1 else items.indexOf(selected.first(), false)
        }
        set(index) = selection.set(items.get(index))

    val list: ImageList<T>
        get() = selectBoxList.list

    val scrollPane: ScrollPane?
        get() = selectBoxList

    init {
        setStyle(style)
        setSize(getPrefWidth(), getPrefHeight())

        selection.setActor(this)
        selection.required = true

        selectBoxList = ImageSelectBoxList(this, itemView)

        clickListener = object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (pointer == 0 && button != 0) return false
                if (disabled) return false
                if (selectBoxList.hasParent())
                    hideList()
                else
                    showList()
                return true
            }
        }

        addListener(clickListener)
    }

    override fun setStage(stage: Stage?) {
        if (stage == null) selectBoxList.hide()
        super.setStage(stage)
    }

    fun setStyle(style: SelectBox.SelectBoxStyle) {
        this.style = style
        selectBoxList.style = style.scrollStyle
        selectBoxList.list.style = style.listStyle
        invalidateHierarchy()
    }

    fun getStyle(): SelectBox.SelectBoxStyle {
        return style
    }

    fun setItems(newItems: Array<T>) {
        val oldPrefWidth = getPrefWidth()

        items.clear()
        items.addAll(newItems)
        selection.validate()
        selectBoxList.list.setItems(items)

        invalidate()
        if (oldPrefWidth != getPrefWidth()) invalidateHierarchy()
    }

    fun clearItems() {
        if (items.size == 0) return
        items.clear()
        selection.clear()
        invalidateHierarchy()
    }

    override fun layout() {
        val bg = style.background
        val font = style.font

        if (bg != null) {
            prefHeight = Math.max(bg.topHeight + bg.bottomHeight + font.capHeight - font.descent * 2,
                    bg.minHeight)
        } else
            prefHeight = font.capHeight - font.descent * 2

        var maxItemWidth = 0f
        val layoutPool = Pools.get(GlyphLayout::class.java)
        val layout = layoutPool.obtain()
        for (item in items) {
            val image = itemView.getItemImage(item)
            val scale = font.lineHeight / image.regionHeight
            val imageWidth = image.regionWidth * scale
            layout.setText(font, itemView.getItemText(item))
            maxItemWidth = Math.max(imageWidth + itemView.getSpacing() + layout.width, maxItemWidth)
        }
        layoutPool.free(layout)

        prefWidth = maxItemWidth
        if (bg != null) prefWidth += bg.leftWidth + bg.rightWidth

        val listStyle = style.listStyle
        val scrollStyle = style.scrollStyle
        var listWidth = maxItemWidth + listStyle.selection.leftWidth + listStyle.selection.rightWidth
        if (scrollStyle.background != null)
            listWidth += scrollStyle.background.leftWidth + scrollStyle.background.rightWidth
        if (!selectBoxList.disableY)
            listWidth += Math.max(
                    if (style.scrollStyle.vScroll != null) style.scrollStyle.vScroll.minWidth else 0f,
                    if (style.scrollStyle.vScrollKnob != null) style.scrollStyle.vScrollKnob.minWidth else 0f)
        prefWidth = Math.max(prefWidth, listWidth)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        validate()

        val background: Drawable?
        if (disabled && style.backgroundDisabled != null)
            background = style.backgroundDisabled
        else if (selectBoxList.hasParent() && style.backgroundOpen != null)
            background = style.backgroundOpen
        else if (clickListener.isOver && style.backgroundOver != null)
            background = style.backgroundOver
        else if (style.background != null)
            background = style.background
        else
            background = null
        val font = style.font
        val fontColor = if (disabled && style.disabledFontColor != null) style.disabledFontColor else style.fontColor

        val color = color
        var drawX = x
        var drawY = y
        var drawWidth = width
        var drawHeight = height

        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        background?.draw(batch, drawX, drawY, drawWidth, drawHeight)

        val selected = selection.first()
        if (selected != null) {
            if (background != null) {
                drawWidth -= background.leftWidth + background.rightWidth
                drawHeight -= background.bottomHeight + background.topHeight
                drawX += background.leftWidth
                drawY += (drawHeight / 2 + background.bottomHeight + font.data.capHeight / 2).toInt().toFloat()
            } else {
                drawY += (drawHeight / 2 + font.data.capHeight / 2).toInt().toFloat()
            }
            font.setColor(fontColor!!.r, fontColor.g, fontColor.b, fontColor.a * parentAlpha)

            val image = itemView.getItemImage(selected)
            val text = itemView.getItemText(selected)

            val scale = font.lineHeight / image.regionHeight
            val imageWidth = image.regionWidth * scale

            batch.draw(image, drawX, drawY - font.lineHeight - font.descent, imageWidth, font.lineHeight)
            layout.setText(font, text, 0, text.length, font.color, drawWidth, Align.left, false, "...")

            font.draw(batch, layout, drawX + itemView.getSpacing(), drawY)
        }
    }

    override fun setDisabled(disabled: Boolean) {
        if (disabled && !this.disabled) hideList()
        this.disabled = disabled
    }

    override fun isDisabled(): Boolean {
        return disabled
    }

    override fun getPrefWidth(): Float {
        validate()
        return prefWidth
    }

    override fun getPrefHeight(): Float {
        validate()
        return prefHeight
    }

    protected fun toString(obj: T): String {
        return obj.toString()
    }

    fun showList() {
        if (items.size == 0) return
        selectBoxList.show(stage)
    }

    fun hideList() {
        selectBoxList.hide()
    }

    fun setScrollingDisabled(y: Boolean) {
        selectBoxList.setScrollingDisabled(true, y)
        invalidateHierarchy()
    }

    fun onShow(selectBoxList: Actor) {
        selectBoxList.color.a = 0f
        selectBoxList.addAction(fadeIn(0.3f, Interpolation.fade))
    }

    protected fun onHide(selectBoxList: Actor) {
        selectBoxList.color.a = 1f
        selectBoxList.addAction(sequence(fadeOut(0.15f, Interpolation.fade), removeActor()))
    }

    internal class ImageSelectBoxList<T>(private val selectBox: ImageSelectBox<T>, private val itemView: ImageList.ImageListItemView<T>)
        : ScrollPane(null, selectBox.style.scrollStyle) {

        var maxListCount: Int = 0
        private val screenPosition = Vector2()
        val list: ImageList<T>
        private val hideListener: InputListener
        private var previousScrollFocus: Actor? = null

        init {

            setOverscroll(false, false)
            setFadeScrollBars(false)
            setScrollingDisabled(true, false)

            list = ImageList(selectBox.style.listStyle, itemView)
            list.touchable = Touchable.disabled
            widget = list

            list.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    selectBox.selection.choose(list.getSelected())
                    hide()
                }

                override fun mouseMoved(event: InputEvent?, x: Float, y: Float): Boolean {
                    list.setSelectedIndex(Math.min(selectBox.items.size - 1, ((list.height - y) / list.itemHeight).toInt()))
                    return true
                }
            })

            addListener(object : InputListener() {
                override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                    if (toActor == null || !isAscendantOf(toActor)) list.selection.set(selectBox.selected)
                }
            })

            hideListener = object : InputListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    val target = event!!.target
                    if (isAscendantOf(target)) return false
                    list.selection.set(selectBox.selected)
                    hide()
                    return false
                }

                override fun keyDown(event: InputEvent?, keycode: Int): Boolean {
                    if (keycode == Input.Keys.ESCAPE) hide()
                    return false
                }
            }
        }

        fun show(stage: Stage) {
            if (list.isTouchable) return

            stage.removeCaptureListener(hideListener)
            stage.addCaptureListener(hideListener)
            stage.addActor(this)

            selectBox.localToStageCoordinates(screenPosition.set(0f, 0f))

            // Show the list above or below the select box, limited to a number of items and the available height in the stage.
            val itemHeight = list.itemHeight
            var height = itemHeight * if (maxListCount <= 0) selectBox.items.size else Math.min(maxListCount, selectBox.items.size)
            val scrollPaneBackground = style.background
            if (scrollPaneBackground != null) height += scrollPaneBackground.topHeight + scrollPaneBackground.bottomHeight
            val listBackground = list.style.background
            if (listBackground != null) height += listBackground.topHeight + listBackground.bottomHeight

            val heightBelow = screenPosition.y
            val heightAbove = stage.camera.viewportHeight - screenPosition.y - selectBox.height
            var below = true
            if (height > heightBelow) {
                if (heightAbove > heightBelow) {
                    below = false
                    height = Math.min(height, heightAbove)
                } else
                    height = heightBelow
            }

            if (below)
                setY(screenPosition.y - height)
            else
                setY(screenPosition.y + selectBox.height)
            x = screenPosition.x
            setHeight(height)
            validate()
            var width = Math.max(prefWidth, selectBox.width)
            if (prefHeight > height && !disableY) width += scrollBarWidth
            setWidth(width)

            validate()
            scrollTo(0f, list.height - selectBox.selectedIndex * itemHeight - itemHeight / 2, 0f, 0f, true, true)
            updateVisualScroll()

            previousScrollFocus = null
            val actor = stage.scrollFocus
            if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor
            stage.scrollFocus = this

            list.selection.set(selectBox.selected)
            list.touchable = Touchable.enabled
            clearActions()
            selectBox.onShow(this)
        }

        fun hide() {
            if (!list.isTouchable || !hasParent()) return
            list.touchable = Touchable.disabled

            val stage = stage
            if (stage != null) {
                stage.removeCaptureListener(hideListener)
                if (previousScrollFocus != null && previousScrollFocus!!.stage == null) previousScrollFocus = null
                val actor = stage.scrollFocus
                if (actor == null || isAscendantOf(actor)) stage.scrollFocus = previousScrollFocus
            }

            clearActions()
            selectBox.onHide(this)
        }

        override fun draw(batch: Batch?, parentAlpha: Float) {
            selectBox.localToStageCoordinates(temp.set(0f, 0f))
            if (temp != screenPosition) hide()
            super.draw(batch, parentAlpha)
        }

        override fun act(delta: Float) {
            super.act(delta)
            toFront()
        }
    }

    companion object {
        val temp = Vector2()
    }
}
