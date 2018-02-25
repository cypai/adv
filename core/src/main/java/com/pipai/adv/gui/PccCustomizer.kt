package com.pipai.adv.gui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener
import com.badlogic.gdx.utils.Array
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.tiles.PccFrame
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata
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
                addPart(PccMetadata("etc", "etc_0.png"), pccParts.size)
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

        val pccFilenameField = TextField(metadata.filename, skin.get("small", TextField.TextFieldStyle::class.java))
        pccFilenameField.addListener(object : FocusListener() {
            override fun keyboardFocusChanged(event: FocusEvent?, actor: Actor?, focused: Boolean) {
                if (!focused) {
                    val pccPart = pccParts[index]
                    val pccFilename = pccFilenameField.text
                    pccParts[index] = PccMetadata(pccPart.type, pccFilename)
                }
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
                    pccParts[index] = dropDown.selection.first()
                    invokeChange()
                }
            }
        })

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
            override fun getItemImage(item: PccMetadata): TextureRegion {
                return pccManager.getPccFrame(item, PccFrame(Direction.S, 0))
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
