package com.pipai.adv.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pipai.adv.AdvGame;

public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    // @cs.suppress [UncommentedMain] this is the main entry point
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1024;
        config.height = 768;
        config.resizable = false;
        new LwjglApplication(new AdvGame(), config);
    }
}
