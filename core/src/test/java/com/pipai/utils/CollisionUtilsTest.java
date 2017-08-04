package com.pipai.utils;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;
import com.pipai.adv.artemis.components.CollisionBounds;

public class CollisionUtilsTest {

    private static final CollisionBounds.CollisionBoundingBox BOUNDING_BOX = new CollisionBounds.CollisionBoundingBox(
            0, 0, 10, 10);

    @Test
    public void testOverlapRectRect() {
        Assert.assertTrue(CollisionUtils.INSTANCE.overlaps(
                0, 0, BOUNDING_BOX,
                9, 0, BOUNDING_BOX));

        Assert.assertTrue(CollisionUtils.INSTANCE.overlaps(
                9, 0, BOUNDING_BOX,
                0, 0, BOUNDING_BOX));

        Assert.assertTrue(CollisionUtils.INSTANCE.overlaps(
                0, 0, BOUNDING_BOX,
                0, 9, BOUNDING_BOX));

        Assert.assertTrue(CollisionUtils.INSTANCE.overlaps(
                0, 9, BOUNDING_BOX,
                0, 0, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                0, 0, BOUNDING_BOX,
                10, 0, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                10, 0, BOUNDING_BOX,
                0, 0, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                0, 0, BOUNDING_BOX,
                0, 10, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                0, 10, BOUNDING_BOX,
                0, 0, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                0, 0, BOUNDING_BOX,
                11, 0, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                11, 0, BOUNDING_BOX,
                0, 0, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                0, 0, BOUNDING_BOX,
                0, 11, BOUNDING_BOX));

        Assert.assertFalse(CollisionUtils.INSTANCE.overlaps(
                0, 11, BOUNDING_BOX,
                0, 0, BOUNDING_BOX));
    }

    @Test
    public void testMtvRectRect() {
        Assert.assertEquals(new Vector2(-1, 0),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        9, 0, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(1, 0),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        -9, 0, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(0, -1),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        0, 9, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(0, 1),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        0, -9, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(-1, 0),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        9, 8, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(1, 0),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        -9, -8, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(0, -1),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        8, 9, BOUNDING_BOX));

        Assert.assertEquals(new Vector2(0, 1),
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        -8, -9, BOUNDING_BOX));

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        10, 0, BOUNDING_BOX));

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        -10, 0, BOUNDING_BOX));

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        0, 10, BOUNDING_BOX));

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.INSTANCE.minimumTranslationVector(
                        0, 0, BOUNDING_BOX,
                        0, -10, BOUNDING_BOX));
    }

}
