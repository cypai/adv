package com.pipai.adv.index;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.Gdx;
import com.pipai.libgdx.test.GdxMockedTest;

public class WeaponSchemaIndexTest extends GdxMockedTest {

    @Test
    public void testReadFile() {
        WeaponSchemaIndex index = new WeaponSchemaIndex(Gdx.files.internal("data/weapons.csv"));
        Assert.assertTrue(index.getIndex().size() > 0);
    }

}
