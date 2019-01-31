package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class WorldMapRenderingSystem(private val game: AdvGame) : IteratingSystem(allOf()) {

    private val batch = game.batchHelper
    private val worldMapTmx = game.globals.worldMapTmx

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mXy by mapper<XYComponent>()
    private val mDrawable by mapper<DrawableComponent>()
    private val mText by mapper<TextComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()
    private val mTileDescriptor by mapper<TileDescriptorComponent>()

    private val sTags by system<TagManager>()

    private val glayout = GlyphLayout()

    private val renderer = OrthogonalTiledMapRenderer(worldMapTmx)

    override fun process(entityId: Int) {
        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        renderer.setView(camera)
        renderer.render()
        batch.spr.projectionMatrix = camera.combined
        batch.spr.begin()
        batch.spr.color = Color.WHITE
        renderEverything()
        renderText()
        batch.spr.end()
    }

    private fun renderEverything() {
        val envObjEntities = world.fetch(allOf(EnvObjTileComponent::class, XYComponent::class, AnimationFramesComponent::class))
                .map { Pair(it, RenderType.ENV_OBJ) }

        val tileDescriptorEntities = world.fetch(allOf(TileDescriptorComponent::class, XYComponent::class))
                .map { Pair(it, RenderType.TILE) }

        val drawableEntities = world.fetch(allOf(DrawableComponent::class, XYComponent::class))
                .map { Pair(it, RenderType.DRAWABLE) }

        val entities: MutableList<Pair<Int, RenderType>> = mutableListOf()
        entities.addAll(envObjEntities)
        entities.addAll(tileDescriptorEntities)
        entities.addAll(drawableEntities.filter { mDrawable.get(it.first).depth == 0 })

        val sortedEntities = entities.map { Pair(-mXy.get(it.first).y, it) }
                .sortedBy { it.first }
                .map { it.second }

        val drawableIds = world.fetch(allOf(DrawableComponent::class, XYComponent::class))
                .filter { mDrawable.get(it).depth < 0 }
                .sortedWith(compareBy({ mDrawable.get(it).depth }, { -mXy.get(it).y }))
        drawableIds.forEach { renderDrawable(it) }

        val tileSize = game.advConfig.resolution.tileSize.toFloat()
        for (entityPair in sortedEntities) {
            when (entityPair.second) {
                RenderType.ENV_OBJ -> renderEnvObjTile(entityPair.first, tileSize)
                RenderType.TILE -> renderTileDescriptor(entityPair.first)
                RenderType.DRAWABLE -> renderDrawable(entityPair.first)
            }
        }
    }

    private fun renderEnvObjTile(id: Int, tileSize: Float) {
        val cEnvObjTile = mEnvObjTile.get(id)
        val cXy = mXy.get(id)
        val cAnimationFrames = mAnimationFrames.get(id)
        val animationFrame = UnitAnimationFrame(cEnvObjTile.direction, cAnimationFrames.frame)

        val tilesetMetadata = cEnvObjTile.tilesetMetadata
        when (tilesetMetadata) {
            is EnvObjTilesetMetadata.PccTilesetMetadata -> {
                for (pcc in tilesetMetadata.pccMetadata) {
                    val pccTexture = game.globals.pccManager.getPccFrame(pcc, animationFrame)
                    val scaleFactor = tileSize / pccTexture.regionWidth

                    if (pcc.color1 != null) {
                        game.globals.shaderProgram.setAttributef("a_color_inter1", pcc.color1.r, pcc.color1.g, pcc.color1.b, pcc.color1.a)
                    }
                    if (pcc.color2 != null) {
                        game.globals.shaderProgram.setAttributef("a_color_inter2", pcc.color2.r, pcc.color2.g, pcc.color2.b, pcc.color2.a)
                    }

                    batch.spr.draw(pccTexture, cXy.x - tileSize / 2, cXy.y, tileSize, pccTexture.regionHeight * scaleFactor)
                    batch.spr.flush()
                    game.globals.shaderProgram.setAttributef("a_color_inter1", 0f, 0f, 0f, 0f)
                    game.globals.shaderProgram.setAttributef("a_color_inter2", 0f, 0f, 0f, 0f)
                }
            }
        }
    }

    private fun renderTileDescriptor(id: Int) {
        val cTileDescriptor = mTileDescriptor.get(id)
        val cXy = mXy.get(id)
        batch.spr.draw(game.globals.textureManager.getTile(cTileDescriptor.descriptor), cXy.x, cXy.y)
    }

    private fun renderDrawable(id: Int) {
        val cDrawable = mDrawable.get(id)
        val cXy = mXy.get(id)
        if (cDrawable.centered) {
            cDrawable.drawable.draw(batch.spr, cXy.x - cDrawable.width / 2, cXy.y - cDrawable.height / 2, cDrawable.width, cDrawable.height)
        } else {
            cDrawable.drawable.draw(batch.spr, cXy.x, cXy.y, cDrawable.width, cDrawable.height)
        }
    }

    private fun renderText() {
        val entities = world.fetch(allOf(TextComponent::class, XYComponent::class))
        entities.forEach { id ->
            val cXy = mXy.get(id)
            val cText = mText.get(id)
            val drawable = game.skin.newDrawable("white", Color.DARK_GRAY)
            glayout.setText(batch.smallFont, cText.text)
            drawable.draw(batch.spr, cXy.x, cXy.y, glayout.width + 12, glayout.height + 12)
            batch.smallFont.draw(batch.spr, cText.text, cXy.x + 6, cXy.y + 6 + glayout.height)
        }
    }

    private enum class RenderType {
        ENV_OBJ, TILE, DRAWABLE
    }
}
