package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGameGlobals
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.TileHighlightUpdateEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.*
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.tiles.*
import com.pipai.adv.utils.*
import net.mostlyoriginal.api.event.common.Subscribe

class BattleMapRenderingSystem(private val skin: Skin,
                               private val batch: BatchHelper,
                               private val globals: AdvGameGlobals,
                               private val mapTileset: MapTileset,
                               private val advConfig: AdvConfig,
                               private val pccManager: PccManager,
                               private val animatedTilesetManager: AnimatedTilesetManager,
                               private val textureManager: TextureManager) : IteratingSystem(allOf()) {

    private val mBackend by require<BattleBackendComponent>()

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mXy by mapper<XYComponent>()
    private val mDrawable by mapper<DrawableComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()
    private val mTileDescriptor by mapper<TileDescriptorComponent>()
    private val mText by mapper<TextComponent>()
    private val mPartialRender by mapper<PartialRenderComponent>()
    private val mUnitHealthbar by mapper<UnitHealthbarComponent>()

    private val sZoom by system<ZoomInputSystem>()
    private val sTags by system<TagManager>()

    private var tileHighlights: Map<Color, List<GridPosition>> = mapOf()
    private val drawableCache: MutableMap<Color, Drawable> = mutableMapOf()

    @Subscribe
    fun movementTileUpdateListener(event: TileHighlightUpdateEvent) {
        tileHighlights = event.tileHighlights
    }

    override fun process(entityId: Int) {
        val cBackend = mBackend.get(entityId)
        val mapState = cBackend.backend.getBattleMapState()

        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        System.out.println(camera.position)
        batch.spr.projectionMatrix = camera.combined
        batch.spr.begin()
        batch.spr.color = Color.WHITE
        renderBackgroundTiles(camera, mapState)
        renderTileHighlights()
        batch.spr.end()
        batch.shape.projectionMatrix = camera.combined
        batch.shape.begin(ShapeRenderer.ShapeType.Filled)
        renderHealthbars()
        batch.shape.end()
        batch.spr.begin()
        renderMapObjects()
        renderText()
        batch.spr.end()
    }

    private fun renderBackgroundTiles(camera: OrthographicCamera, mapState: BattleMap) {
        val tileSize = advConfig.resolution.tileSize.toFloat()
        val center = GridUtils.localToGridPosition(camera.position.x, camera.position.y, tileSize)
        val zoom = sZoom.currentZoom()
        val gridViewWidth = (advConfig.resolution.width / tileSize * zoom).toInt() + 4
        val gridViewHeight = (advConfig.resolution.height / tileSize * zoom).toInt() + 4
        val gridViewLeft = Math.max(center.x - gridViewWidth / 2, 0)
        val gridViewRight = Math.min(center.x + gridViewWidth / 2, mapState.width)
        val gridViewBottom = Math.max(center.y - gridViewHeight / 2, 0)
        val gridViewTop = Math.min(center.y + gridViewHeight / 2, mapState.height)
        for (y in gridViewBottom until gridViewTop) {
            for (x in gridViewLeft until gridViewRight) {
                val cell = mapState.cells[x][y]
                for (bgTileInfo in cell.backgroundTiles) {
                    val tile = mapTileset.tiles(bgTileInfo.tileType)[bgTileInfo.index]
                    batch.spr.draw(tile, x.toFloat() * tileSize, y.toFloat() * tileSize, tileSize, tileSize)
                }
            }
        }
    }

    private fun renderTileHighlights() {
        tileHighlights.forEach { color, tiles ->
            val drawable = getColorDrawable(color)
            renderTileHighlight(tiles, drawable)
        }
    }

    private fun getColorDrawable(color: Color): Drawable {
        return if (drawableCache.containsKey(color)) {
            drawableCache[color]!!
        } else {
            skin.newDrawable("white", color)!!
                    .also { drawableCache[color] = it }
        }
    }

    private fun renderTileHighlight(tiles: List<GridPosition>, drawable: Drawable) {
        val tileSize = advConfig.resolution.tileSize.toFloat()
        for (tilePosition in tiles) {
            drawable.draw(batch.spr, tileSize * tilePosition.x, tileSize * tilePosition.y, tileSize, tileSize)
        }
    }

    private fun renderMapObjects() {
        val envObjEntities = world.fetch(allOf(EnvObjTileComponent::class, XYComponent::class, AnimationFramesComponent::class))
                .map { Pair(it, RenderType.ENV_OBJ) }

        val tileDescriptorEntities = world.fetch(allOf(TileDescriptorComponent::class, XYComponent::class))
                .map { Pair(it, RenderType.TILE) }

        val drawableEntities = world.fetch(allOf(DrawableComponent::class, XYComponent::class))
                .map { Pair(it, RenderType.DRAWABLE) }

        val entities: MutableList<Pair<Int, RenderType>> = mutableListOf()
        entities.addAll(envObjEntities)
        entities.addAll(tileDescriptorEntities)
        entities.addAll(drawableEntities)

        val sortedEntities = entities.map { Pair(-mXy.get(it.first).y, it) }
                .sortedBy { it.first }
                .map { it.second }

        val tileSize = advConfig.resolution.tileSize.toFloat()
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
            is PccTilesetMetadata -> {
                for (pcc in tilesetMetadata.pccMetadata) {
                    val pccTexture = pccManager.getPccFrame(pcc, animationFrame)
                    val scaleFactor = tileSize / pccTexture.regionWidth

                    if (pcc.color1 != null) {
                        globals.shaderProgram.setAttributef("a_color_inter1", pcc.color1.r, pcc.color1.g, pcc.color1.b, pcc.color1.a)
                    }
                    if (pcc.color2 != null) {
                        globals.shaderProgram.setAttributef("a_color_inter2", pcc.color2.r, pcc.color2.g, pcc.color2.b, pcc.color2.a)
                    }

                    batch.spr.draw(pccTexture, cXy.x, cXy.y, tileSize, pccTexture.regionHeight * scaleFactor)
                    batch.spr.flush()
                    globals.shaderProgram.setAttributef("a_color_inter1", 0f, 0f, 0f, 0f)
                    globals.shaderProgram.setAttributef("a_color_inter2", 0f, 0f, 0f, 0f)
                }
            }
            is MapTilesetMetadata -> {
                batch.spr.draw(mapTileset.tiles(tilesetMetadata.mapTileType)[0], cXy.x, cXy.y, tileSize, tileSize)
            }
            is AnimatedUnitTilesetMetadata -> {
                val unitTexture = animatedTilesetManager.getTilesetFrame(tilesetMetadata.filename, animationFrame)
                val scaleFactor = tileSize / unitTexture.regionWidth
                val cPartialRender = mPartialRender.getSafe(id, null)
                if (cPartialRender == null) {
                    batch.spr.draw(unitTexture, cXy.x, cXy.y, tileSize, unitTexture.regionHeight * scaleFactor)
                } else {
                    val partialTexture = TextureRegion(unitTexture,
                            0, 0,
                            (unitTexture.regionWidth * cPartialRender.widthPercentage).toInt(),
                            (unitTexture.regionHeight * cPartialRender.heightPercentage).toInt())
                    batch.spr.draw(partialTexture, cXy.x, cXy.y, tileSize, partialTexture.regionHeight * scaleFactor)
                }
            }
        }
    }

    private fun renderTileDescriptor(id: Int) {
        val cTileDescriptor = mTileDescriptor.get(id)
        val cXy = mXy.get(id)
        batch.spr.draw(textureManager.getTile(cTileDescriptor.descriptor), cXy.x, cXy.y)
    }

    private fun renderDrawable(id: Int) {
        val cDrawable = mDrawable.get(id)
        val cXy = mXy.get(id)
        cDrawable.drawable.draw(batch.spr, cXy.x, cXy.y, cDrawable.width, cDrawable.height)
    }

    private fun renderHealthbars() {
        val tileSize = advConfig.resolution.tileSize.toFloat()
        val width = tileSize * 0.8f
        val xOffset = (tileSize - width) / 2f
        val height = 5f
        val healthbarEntities = world.fetch(allOf(UnitHealthbarComponent::class, XYComponent::class))
        healthbarEntities.forEach {
            val cHealthbar = mUnitHealthbar.get(it)
            val cXy = mXy.get(it)
            batch.shape.drawHealthbar(cXy.x + xOffset, cXy.y - height, width, height,
                    Color.DARK_GRAY, Color.RED, Color.YELLOW, Color.BLACK, cHealthbar.percentage)
        }
    }

    private fun renderText() {
        val textEntities = world.fetch(allOf(TextComponent::class, XYComponent::class))
        batch.font.color = Color.WHITE
        textEntities.forEach {
            val cText = mText.get(it)
            val cXy = mXy.get(it)
            batch.smallFont.draw(batch.spr, cText.text, cXy.x, cXy.y)
        }
    }

    private enum class RenderType {
        ENV_OBJ, TILE, DRAWABLE
    }
}
