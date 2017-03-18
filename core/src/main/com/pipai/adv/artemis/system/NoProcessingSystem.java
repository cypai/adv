package com.pipai.adv.artemis.system;

import com.artemis.BaseSystem;

public abstract class NoProcessingSystem extends BaseSystem {

    @Override
    protected final boolean checkProcessing() {
        return false;
    }

    @Override
    protected final void processSystem() {
        // Do nothing
    }

}
