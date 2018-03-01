package com.pipai.adv.index

import org.junit.Assert
import org.junit.Test

import com.badlogic.gdx.Gdx
import com.pipai.test.libgdx.GdxMockedTest

class WeaponSchemaIndexTest : GdxMockedTest() {

    @Test
    fun testReadFile() {
        val index = WeaponSchemaIndex(Gdx.files.internal("data/weapons.csv"))
        Assert.assertTrue(index.index.size > 0)
    }

}
