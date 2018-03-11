package com.pipai.adv.gui

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener
import com.badlogic.gdx.utils.Array
import com.kotcrab.vis.ui.widget.color.ColorPicker
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.utils.PccComparator

class PccCustomizer(pcc: List<PccMetadata>,
                    private val pccManager: PccManager,
                    private val skin: Skin,
                    val x: Float, val y: Float,
                    val width: Float, val height: Float) {

    private val pccParts: MutableList<PccMetadata> = pcc.toMutableList()

    val stage = Stage()
    private val table = Table()
    private val verticalGroup = VerticalGroup()
    private val changeListeners: MutableList<(List<PccMetadata>) -> Unit> = mutableListOf()
    private var colorPickerTarget: Image? = null
    private var colorPickerPccIndex: Int = 0
    private var colorPickerColorIndex: Int = 0
    private val colorPicker = ColorPicker(object : ColorPickerAdapter() {
        override fun changed(newColor: Color) {
            colorPickerTarget?.let {
                it.drawable = skin.newDrawable("white", newColor)
                val oldPcc = pccParts[colorPickerPccIndex]
                pccParts[colorPickerPccIndex] = PccMetadata(oldPcc.type, oldPcc.filename,
                        if (colorPickerColorIndex == 0) newColor else oldPcc.color1,
                        if (colorPickerColorIndex == 1) newColor else oldPcc.color2)
                invokeChange()
            }
        }
    })

    init {
        table.x = x
        table.y = y
        table.width = width
        table.height = height

        val framePatch = skin.getPatch("frame")
        framePatch.topHeight = 2f
        val frame = Image(framePatch)
        frame.x = table.x - 1
        frame.y = table.y - 3
        frame.width = table.width + 4
        frame.height = table.height + 4
        stage.addActor(frame)
        stage.addActor(table)

        verticalGroup.columnLeft()
        table.left().top()
        table.add(verticalGroup).padLeft(10f).padTop(10f)

        rebuildList()
    }

    fun addChangeListener(listener: (List<PccMetadata>) -> Unit) {
        changeListeners.add(listener)
    }

    fun getPcc() = pccParts.toList()

    private fun rebuildList() {
        verticalGroup.clear()
        for (i in 0 until pccParts.size) {
            verticalGroup.addActor(buildListElement(i))
        }

        val addButton = Container(TextButton("+", skin))
        addButton.width(20f)
        addButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                addPart(PccMetadata("etc", "etc_0.png", null, null), pccParts.size)
                invokeChange()
            }
        })
        verticalGroup.addActor(addButton)
    }

    private fun buildListElement(index: Int): Actor {
        val metadata = pccParts[index]
        val listTable = Table()
        listTable.left().padBottom(4f)

        val deleteButton = TextButton("X", skin)
        deleteButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                removePart(index)
                invokeChange()
            }
        })

        val shiftDownButton = TextButton("v", skin)
        shiftDownButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                shiftDown(index)
                invokeChange()
            }
        })

        val shiftUpButton = TextButton("^", skin)
        shiftUpButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                shiftUp(index)
                invokeChange()
            }
        })

        listTable.add(deleteButton)
                .width(20f).minWidth(20f)
        listTable.add(shiftDownButton)
                .width(20f).minWidth(20f).padLeft(4f)
        listTable.add(shiftUpButton)
                .width(20f).minWidth(20f).padLeft(4f)

        val categoryDropDown = generateCategoryDropDown(metadata.type)
        listTable.add(categoryDropDown)
                .width(80f).minWidth(80f).padLeft(4f)

        val partsDropDown = generatePartsDropDown(metadata.type, pccParts[index])
        listTable.add(partsDropDown)
                .minWidth(200f).padLeft(4f)

        categoryDropDown.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (actor is SelectBox<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val dropDown = actor as SelectBox<String>
                    setPartsDropDownParts(partsDropDown, dropDown.selection.first())
                    invokeChange()
                }
            }
        })

        partsDropDown.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (actor is ImageSelectBox<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val dropDown = actor as ImageSelectBox<PccMetadata>
                    val selection = dropDown.selection.first()
                    pccParts[index] = PccMetadata(selection.type, selection.filename, pccParts[index].color1, pccParts[index].color2)
                    invokeChange()
                }
            }
        })

        val colorBox1Drawable = if (metadata.color1 == null) skin.getDrawable("transparencyBg") else skin.newDrawable("white", metadata.color1)
        val colorBox1 = Image(colorBox1Drawable)
        colorBox1.addListener(object : ClickListener(Input.Buttons.LEFT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                colorPickerTarget = colorBox1
                colorPickerPccIndex = index
                colorPickerColorIndex = 0
                colorPicker.width = width
                colorPicker.height = 320f
                colorPicker.color = pccParts[index].color1 ?: Color.WHITE
                stage.addActor(colorPicker)
            }
        })
        colorBox1.addListener(object : ClickListener(Input.Buttons.RIGHT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                colorBox1.drawable = skin.getDrawable("transparencyBg")
                val oldPcc = pccParts[index]
                pccParts[index] = PccMetadata(oldPcc.type, oldPcc.filename, null, null)
                invokeChange()
            }
        })
        listTable.add(colorBox1)
                .width(20f).minWidth(20f).height(20f).maxHeight(20f).padLeft(4f)

        val colorBox2Drawable = if (metadata.color2 == null) skin.getDrawable("transparencyBg") else skin.newDrawable("white", metadata.color2)
        val colorBox2 = Image(colorBox2Drawable)
        colorBox2.addListener(object : ClickListener(Input.Buttons.LEFT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                colorPickerTarget = colorBox2
                colorPickerPccIndex = index
                colorPickerColorIndex = 1
                colorPicker.width = width
                colorPicker.height = 320f
                colorPicker.color = pccParts[index].color2 ?: Color.WHITE
                stage.addActor(colorPicker)
            }
        })
        colorBox2.addListener(object : ClickListener(Input.Buttons.RIGHT) {
            override fun clicked(event: InputEvent, x: Float, y: Float) {
                colorBox2.drawable = skin.getDrawable("transparencyBg")
                val oldPcc = pccParts[index]
                pccParts[index] = PccMetadata(oldPcc.type, oldPcc.filename, null, null)
                invokeChange()
            }
        })
        listTable.add(colorBox2)
                .width(20f).minWidth(20f).height(20f).maxHeight(20f).padLeft(4f)

        return listTable
    }

    private fun invokeChange() {
        val currentPcc = getPcc()
        changeListeners.forEach { it.invoke(currentPcc) }
    }

    private fun generateCategoryDropDown(defaultSelected: String?): SelectBox<String> {
        val dropDownList = SelectBox<String>(skin)
        dropDownList.setItems("body", "cloth", "etc", "eye", "hair", "pants", "subhair")
        if (defaultSelected != null && dropDownList.items.contains(defaultSelected)) {
            dropDownList.selected = defaultSelected
        }
        return dropDownList
    }

    private fun generatePartsDropDown(type: String, defaultSelected: PccMetadata?): ImageSelectBox<PccMetadata> {

        val view = object : ImageList.ImageListItemView<PccMetadata> {
            override fun getItemImage(item: PccMetadata): TextureRegion? {
                return pccManager.getPccFrame(item, UnitAnimationFrame(Direction.S, 0))
            }

            override fun getItemText(item: PccMetadata): String {
                return item.filename
            }

            override fun getSpacing(): Float = 30f
        }
        val dropDownList = ImageSelectBox(skin, view)
        setPartsDropDownParts(dropDownList, type)
        if (defaultSelected != null) {
            dropDownList.selection.set(defaultSelected)
        }
        return dropDownList
    }

    private fun setPartsDropDownParts(dropDownList: ImageSelectBox<PccMetadata>, type: String) {
        val arr = Array<PccMetadata>()
        pccManager.listPccs(type)
                .sortedWith(PccComparator())
                .forEach { arr.add(it) }

        pccManager.loadPccTextures(arr.toList())
        dropDownList.setItems(arr)
    }

    fun addPart(metadata: PccMetadata, index: Int) {
        pccParts.add(index, metadata)
        rebuildList()
    }

    fun removePart(index: Int) {
        pccParts.removeAt(index)
        rebuildList()
    }

    fun shiftUp(index: Int) {
        if (index > 0 && index < pccParts.size) {
            val pccPart = pccParts.removeAt(index)
            pccParts.add(index - 1, pccPart)
        }
        rebuildList()
    }

    fun shiftDown(index: Int) {
        if (index >= 0 && index < pccParts.size - 1) {
            val part = pccParts.removeAt(index)
            pccParts.add(index + 1, part)
        }
        rebuildList()
    }

    fun render() {
        stage.act()
        stage.draw()
    }
}
