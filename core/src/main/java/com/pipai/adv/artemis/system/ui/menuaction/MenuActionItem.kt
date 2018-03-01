package com.pipai.adv.artemis.system.ui.menuaction

import com.badlogic.gdx.graphics.g2d.TextureRegion

interface MenuActionItem {
    var image: TextureRegion?
    var text: String
    var rightText: String
    var spacing: Float

    var onSelect: () -> Unit
}
