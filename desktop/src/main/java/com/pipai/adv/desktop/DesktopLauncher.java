package com.pipai.adv.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.pipai.adv.AdvConfig;
import com.pipai.adv.AdvGame;

import java.io.File;

public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    // @cs.suppress [UncommentedMain] this is the main entry point
    public static void main(String[] arg) {
        AdvConfig advConfig = new AdvConfig(new FileHandle(new File("config/config.properties")));
        advConfig.writeToFile();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(advConfig.getResolution().getWidth(), advConfig.getResolution().getHeight());
        config.setResizable(false);
        new Lwjgl3Application(new AdvGame(advConfig), config);
    }
}
