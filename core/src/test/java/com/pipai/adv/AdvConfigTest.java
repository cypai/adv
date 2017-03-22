package com.pipai.adv;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.pipai.test.libgdx.GdxMockedTest;
import com.pipai.test.libgdx.GdxTestUtilsKt;

public class AdvConfigTest extends GdxMockedTest {

    public static final FileHandle TEST_FILE = GdxTestUtilsKt.getTestResourceFileHandle(AdvConfigTest.class, "config.properties");
    public static final FileHandle BAD_TEST_FILE = GdxTestUtilsKt.getTestResourceFileHandle(AdvConfigTest.class, "bad-config.properties");
    public static final FileHandle TEST_WRITE_FILE = Gdx.files.local("test-config.properties");

    @After
    public void teardown() {
        if (TEST_WRITE_FILE.exists()) {
            boolean deleted = TEST_WRITE_FILE.delete();
            if (!deleted) {
                throw new IllegalStateException("Test file was not deleted");
            }
        }
    }

    @Test
    public void testReadConfig() {
        AdvConfig config = new AdvConfig(TEST_FILE);
        Assert.assertEquals(ScreenResolution.RES_1280_720, config.getResolution());
    }

    @Test
    public void testConfigWhenFileDoesNotExist() {
        AdvConfig config = new AdvConfig(TEST_WRITE_FILE);
        Assert.assertEquals(ScreenResolution.RES_1024_768, config.getResolution());
    }

    @Test
    public void testReadBadConfig() {
        AdvConfig config = new AdvConfig(BAD_TEST_FILE);
        Assert.assertEquals(ScreenResolution.RES_1024_768, config.getResolution());
    }

    @Test
    public void testWriteConfig() {
        AdvConfig config = new AdvConfig(TEST_WRITE_FILE);
        Assert.assertEquals(ScreenResolution.RES_1024_768, config.getResolution());

        config.setResolution(ScreenResolution.RES_1920_1080);
        Assert.assertEquals(ScreenResolution.RES_1920_1080, config.getResolution());

        config.writeToFile();

        AdvConfig loadConfig = new AdvConfig(TEST_WRITE_FILE);
        Assert.assertEquals(ScreenResolution.RES_1920_1080, loadConfig.getResolution());
    }

}
