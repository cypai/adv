package com.pipai.adv.utils

import org.junit.Assert
import org.junit.Test

import com.badlogic.gdx.math.Vector2
import com.pipai.adv.artemis.components.CollisionBounds

class CollisionUtilsTest {

    @Test
    fun testOverlapRectRect() {
        Assert.assertTrue(CollisionUtils.overlaps(
                0f, 0f, BOUNDING_BOX,
                9f, 0f, BOUNDING_BOX))

        Assert.assertTrue(CollisionUtils.overlaps(
                9f, 0f, BOUNDING_BOX,
                0f, 0f, BOUNDING_BOX))

        Assert.assertTrue(CollisionUtils.overlaps(
                0f, 0f, BOUNDING_BOX,
                0f, 9f, BOUNDING_BOX))

        Assert.assertTrue(CollisionUtils.overlaps(
                0f, 9f, BOUNDING_BOX,
                0f, 0f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                0f, 0f, BOUNDING_BOX,
                10f, 0f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                10f, 0f, BOUNDING_BOX,
                0f, 0f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                0f, 0f, BOUNDING_BOX,
                0f, 10f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                0f, 10f, BOUNDING_BOX,
                0f, 0f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                0f, 0f, BOUNDING_BOX,
                11f, 0f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                11f, 0f, BOUNDING_BOX,
                0f, 0f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                0f, 0f, BOUNDING_BOX,
                0f, 11f, BOUNDING_BOX))

        Assert.assertFalse(CollisionUtils.overlaps(
                0f, 11f, BOUNDING_BOX,
                0f, 0f, BOUNDING_BOX))
    }

    @Test
    fun testMtvRectRect() {
        Assert.assertEquals(Vector2(-1f, 0f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        9f, 0f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(1f, 0f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        -9f, 0f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(0f, -1f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        0f, 9f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(0f, 1f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        0f, -9f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(-1f, 0f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        9f, 8f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(1f, 0f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        -9f, -8f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(0f, -1f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        8f, 9f, BOUNDING_BOX))

        Assert.assertEquals(Vector2(0f, 1f),
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        -8f, -9f, BOUNDING_BOX))

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        10f, 0f, BOUNDING_BOX))

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        -10f, 0f, BOUNDING_BOX))

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        0f, 10f, BOUNDING_BOX))

        Assert.assertEquals(Vector2.Zero,
                CollisionUtils.minimumTranslationVector(
                        0f, 0f, BOUNDING_BOX,
                        0f, -10f, BOUNDING_BOX))
    }

    companion object {

        private val BOUNDING_BOX = CollisionBounds.CollisionBoundingBox(
                0f, 0f, 10f, 10f)
    }

}
