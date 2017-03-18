package com.pipai.adv;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.pipai.adv.gui.BatchHelper;

public class AdvGame extends Game {

    private BatchHelper batchHelper;

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    @Override
    public void create() {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        batchHelper = new BatchHelper(spriteBatch, shapeRenderer, font);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }

    public BatchHelper getBatchHelper() {
        return batchHelper;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public BitmapFont getFont() {
        return font;
    }

}
