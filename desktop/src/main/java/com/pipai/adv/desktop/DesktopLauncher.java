package com.pipai.adv.desktop;

import java.io.File;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.pipai.adv.AdvConfig;
import com.pipai.adv.AdvGame;

public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    // @cs.suppress [UncommentedMain] this is the main entry point
    public static void main(String[] arg) {
        AdvConfig advConfig = new AdvConfig(new FileHandle(new File("config/config.properties")));
        advConfig.writeToFile();

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = advConfig.getResolution().getWidth();
        config.height = advConfig.getResolution().getHeight();
        config.resizable = false;
        new LwjglApplication(new AdvGame(advConfig), config);
    }
}
