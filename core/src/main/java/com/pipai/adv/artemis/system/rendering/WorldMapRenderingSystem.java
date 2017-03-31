package com.pipai.adv.artemis.system.rendering;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.pipai.adv.artemis.components.BattleBackendComponent;

public class WorldMapRenderingSystem extends IteratingSystem {

    public WorldMapRenderingSystem() {
        super(Aspect.all(BattleBackendComponent.class));
    }

    // private ComponentMapper<BattleBackendComponent> mWorldMap;
    // private ComponentMapper<OrthographicCameraComponent> mOrthoCamera;

    // private TagManager tagManager;

    @Override
    protected void process(int entityId) {
        // OrthographicCameraComponent cOrtho = mOrthoCamera.get(tagManager.getEntity(Tag.ORTHO_CAMERA.toString()));
    }
}
