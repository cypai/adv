package com.pipai.test.libgdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.File;

public abstract class GdxMockedTest {

    private static Application application;

    @BeforeClass
    public static void mockGdxFiles() {
        // @cs.suppress [AnonInnerLength] we really don't want to create this as its own thing
        application = new HeadlessApplication(new ApplicationListener() {
            @Override
            public void create() {
            }

            @Override
            public void resize(int width, int height) {
            }

            @Override
            public void render() {
            }

            @Override
            public void pause() {
            }

            @Override
            public void resume() {
            }

            @Override
            public void dispose() {
            }
        });

        Gdx.files = Mockito.mock(Files.class);
        Mockito.when(Gdx.files.internal(Matchers.anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return new FileHandle("assets/" + args[0]);
        });
        Mockito.when(Gdx.files.local(Matchers.anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return new FileHandle((String) args[0]);
        });
        Mockito.when(Gdx.files.external(Matchers.anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            File file = new File("" + args[0]);
            return new FileHandle(file);
        });

        Gdx.graphics = new MockGraphics();
        Gdx.gl20 = Mockito.mock(GL20.class);
        Gdx.gl = Gdx.gl20;
    }

    @AfterClass
    public static void unmock() {
        Gdx.files = null;
    }
}
