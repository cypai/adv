package com.pipai.adv

import org.junit.After
import org.junit.Assert
import org.junit.Test

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.*

class AdvConfigTest : GdxMockedTest() {

    @After
    fun teardown() {
        if (TEST_WRITE_FILE.exists()) {
            val deleted = TEST_WRITE_FILE.delete()
            if (!deleted) {
                throw IllegalStateException("Test file was not deleted")
            }
        }
    }

    @Test
    fun testReadConfig() {
        val config = AdvConfig(TEST_FILE)
        Assert.assertEquals(ScreenResolution.RES_1280_720, config.resolution)
    }

    @Test
    fun testConfigWhenFileDoesNotExist() {
        val config = AdvConfig(TEST_WRITE_FILE)
        Assert.assertEquals(ScreenResolution.RES_1024_768, config.resolution)
    }

    @Test
    fun testReadBadConfig() {
        val config = AdvConfig(BAD_TEST_FILE)
        Assert.assertEquals(ScreenResolution.RES_1024_768, config.resolution)
    }

    @Test
    fun testWriteConfig() {
        val config = AdvConfig(TEST_WRITE_FILE)
        Assert.assertEquals(ScreenResolution.RES_1024_768, config.resolution)

        config.resolution = ScreenResolution.RES_1920_1080
        Assert.assertEquals(ScreenResolution.RES_1920_1080, config.resolution)

        config.writeToFile()

        val loadConfig = AdvConfig(TEST_WRITE_FILE)
        Assert.assertEquals(ScreenResolution.RES_1920_1080, loadConfig.resolution)
    }

    companion object {

        private val TEST_FILE = getTestResourceFileHandle(AdvConfigTest::class.java,
                "config.properties")
        private val BAD_TEST_FILE = getTestResourceFileHandle(AdvConfigTest::class.java,
                "bad-config.properties")
        private val TEST_WRITE_FILE = Gdx.files.local("test-config.properties")
    }

}
