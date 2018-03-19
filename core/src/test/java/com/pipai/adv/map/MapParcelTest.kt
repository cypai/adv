package com.pipai.adv.map

import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver
import com.pipai.test.libgdx.GdxMockedTest
import com.pipai.test.libgdx.getTestResourceFilePath
import org.junit.Test

class MapParcelTest : GdxMockedTest() {

    companion object {
        private val PARCEL_FILE = getTestResourceFilePath(MapParcelTest::class.java,
                "parcel1.tmx")
    }

    @Test
    fun testMapParcel() {
        val mapParcel = MapParcel.Factory.readMapParcel(ExternalFileHandleResolver(), PARCEL_FILE)
    }

}
