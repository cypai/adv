package com.pipai.adv.gui

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener
import com.badlogic.gdx.utils.Array
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.tiles.PccFrame
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata


class PccCustomizer(private val pccManager: PccManager,
                    private val skin: Skin,
                    x: Float, y: Float,
                    width: Float, height: Float) {

    private val pccParts: MutableList<PccMetadata> = mutableListOf()

    val stage = Stage()
    private val table = Table()
    private val verticalGroup = VerticalGroup()

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

        pccParts.add(PccMetadata("body", "body_1.png"))
        pccParts.add(PccMetadata("etc", "etc_1.png"))
        rebuildList()
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
                addPart(PccMetadata("etc", "etc_1.png"), pccParts.size)
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
            }
        })

        val shiftDownButton = TextButton("v", skin)
        shiftDownButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                shiftDown(index)
            }
        })

        val shiftUpButton = TextButton("^", skin)
        shiftUpButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                shiftUp(index)
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

        listTable.add(generateCategoryDropDown(metadata.type))
                .width(80f).minWidth(80f).padLeft(4f)

        listTable.add(generatePartsDropDown(metadata.type))
                .minWidth(80f).padLeft(4f)

        return listTable
    }

    private fun generateCategoryDropDown(defaultSelected: String?): SelectBox<String> {
        val dropDownList = SelectBox<String>(skin)
        dropDownList.setItems("body", "cloth", "etc", "eye", "hair", "pants", "subhair")
        if (defaultSelected != null && dropDownList.items.contains(defaultSelected)) {
            dropDownList.selected = defaultSelected
        }
        return dropDownList
    }

    private fun generatePartsDropDown(type: String): ImageSelectBox<PccMetadata> {
        val arr = Array<PccMetadata>()
        pccManager.listPccs(type).forEach { arr.add(it) }

        val view = object : ImageList.ImageListItemView<PccMetadata> {
            override fun getItemImage(item: PccMetadata): TextureRegion {
                return pccManager.getPccFrame(item, PccFrame(Direction.S, 0))
            }

            override fun getItemText(item: PccMetadata): String {
                return item.filename
            }

            override fun getSpacing(): Float = 30f
        }

        pccManager.loadPccTextures(arr.toList())
        val dropDownList = ImageSelectBox(skin, view)
        dropDownList.setItems(arr)
        return dropDownList
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
