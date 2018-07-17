package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.VillageScreen
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.backend.battle.domain.InventoryItem
import com.pipai.adv.gui.StandardImageListItemView

class MarketUiSystem(private val game: AdvGame,
                     private val stage: Stage) : NoProcessingSystem(), InputProcessor {

    private val stateMachine = StackStateMachine<MarketUiSystem, MarketUiState>(this)

    private val skin = game.skin

    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())

    private val saleTable = Table()
    private val saleTitle = Label("", game.skin)
    private val saleGoldLabel = Label("", game.skin, "small")
    private val saleList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())

    init {
        createTables()
        stateMachine.changeState(MarketUiState.SHOWING_MAIN_MENU)
    }

    private fun createTables() {
        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        mainTable.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        mainTable.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        mainTable.width = mainMenuWidth
        mainTable.height = mainMenuHeight
        mainTable.background = skin.getDrawable("frameDrawable")

        val menuItems = mutableListOf(
                StringMenuItem("Buy", null, ""),
                StringMenuItem("Sell", null, ""),
                StringMenuItem("Back", null, ""))

        mainMenuList.setItems(menuItems)
        mainMenuList.hoverSelect = true
        mainMenuList.keySelection = true
        mainMenuList.addConfirmCallback { handleMainMenuConfirm(it) }
        mainTable.add(mainMenuList)
                .width(mainMenuWidth - 20f)
                .left()
                .top()


        saleTable.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        saleTable.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        saleTable.width = mainMenuWidth
        saleTable.height = mainMenuHeight
        saleTable.background = skin.getDrawable("frameDrawable")

        saleList.setItems(menuItems)
        saleList.hoverSelect = true
        saleList.keySelection = true
        saleList.disabledFontColor = Color.GRAY
        saleList.addConfirmCallback { handleSaleMenuConfirm(it) }
        saleTable.add(saleTitle)
        saleTable.row()
        saleTable.add(saleGoldLabel)
        saleTable.row()
        saleTable.add(saleList)
                .width(mainMenuWidth - 20f)
                .left()
                .top()
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            "Buy" -> {
                stateMachine.changeState(MarketUiState.SHOWING_BUY_MENU)
            }
            "Sell" -> {
                stateMachine.changeState(MarketUiState.SHOWING_SELL_MENU)
            }
            "Back" -> {
                game.screen = VillageScreen(game)
            }
        }
    }

    private fun populateBuyList() {
        saleTitle.setText("Buy Items")
        saleGoldLabel.setText("Gold: ${game.globals.save!!.gold}")
        val weaponIndex = game.globals.weaponSchemaIndex
        val weapons = weaponIndex.index.keys
        saleList.setItems(weapons.map {
            StringMenuItem(it, null, weaponIndex.getWeaponSchema(it)!!.value.toString())
                    .withData("item", InventoryItem.WeaponInstance(it, 1))
        })
        saleList.setDisabledPredicate { it.rightText.toInt() > game.globals.save!!.gold }
    }

    private fun handleSaleMenuConfirm(menuItem: StringMenuItem) {
        when (stateMachine.currentState) {
            MarketUiState.SHOWING_BUY_MENU -> {
                showDialog("Buy the ${menuItem.text} for ${menuItem.rightText} gold?",
                        {
                            val save = game.globals.save!!
                            save.gold -= menuItem.rightText.toInt()
                            save.inventory.add((menuItem.getData("item") as InventoryItem).deepCopy())
                            populateBuyList()
                        },
                        {})
            }
            else -> {
                // do nothing
            }
        }
    }

    private fun showDialog(text: String, yesCallback: () -> Unit, noCallback: () -> Unit) {
        val dialog = object : Dialog("", game.skin) {
            override fun result(item: Any?) {
                when (item) {
                    true -> yesCallback.invoke()
                    false -> noCallback.invoke()
                }
            }
        }
        dialog.text(text)

        dialog.button("  Yes  ", true)
        dialog.button("  No  ", false)
        dialog.key(Keys.ENTER, true)
        dialog.key(Keys.Z, true)
        dialog.key(Keys.ESCAPE, false)
        dialog.key(Keys.X, false)
        dialog.contentTable.pad(16f)
        dialog.buttonTable.pad(16f)
        dialog.show(stage)
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                if (stateMachine.isInState(MarketUiState.SHOWING_MAIN_MENU)) {
                    game.screen = VillageScreen(game)
                } else {
                    stateMachine.revertToPreviousState()
                    return true
                }
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

    enum class MarketUiState : State<MarketUiSystem> {
        SHOWING_MAIN_MENU() {
            override fun enter(uiSystem: MarketUiSystem) {
                uiSystem.stage.addActor(uiSystem.mainTable)
                uiSystem.stage.keyboardFocus = uiSystem.mainMenuList
            }
        },
        SHOWING_BUY_MENU() {
            override fun enter(uiSystem: MarketUiSystem) {
                uiSystem.populateBuyList()
                uiSystem.stage.addActor(uiSystem.saleTable)
                uiSystem.stage.keyboardFocus = uiSystem.saleList
            }

            override fun exit(uiSystem: MarketUiSystem) {
                uiSystem.saleTable.remove()
            }
        },
        SHOWING_SELL_MENU() {
            override fun enter(uiSystem: MarketUiSystem) {
            }

            override fun exit(uiSystem: MarketUiSystem) {
            }
        };

        override fun enter(uiSystem: MarketUiSystem) {
        }

        override fun exit(uiSystem: MarketUiSystem) {
        }

        override fun onMessage(uiSystem: MarketUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: MarketUiSystem) {
        }
    }

}
