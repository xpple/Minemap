
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.ARBFramebufferObject.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Showcases multisampled FBO rendering.
 * <p>
 * This demo first renders into a multisampled FBO and then blits/resolves into
 * a single-sampled FBO, which allows to obtain the final image as a texture.
 * <p>
 * Additionally, this demo then renders this texture on the screen using a
 * textured fullscreen quad.
 *
 * @author Kai Burjack
 */
public class TestGLFW {

    long window;
    int width = 1024;
    int height = 768;
    boolean resetFramebuffer;
    boolean destroyed;
    Object lock = new Object();
    /* Single-sampled FBO objects */
    int colorTexture;
    int fbo;

    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    Callback debugProc;

    void run() {
        try {
            init();
            winProcLoop();

            synchronized (lock) {
                destroyed = true;
                glfwDestroyWindow(window);
            }
            if (debugProc != null)
                debugProc.free();
            keyCallback.free();
            fbCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Multisampled FBO", NULL, NULL);
        if (window == NULL) {
            throw new AssertionError("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action != GLFW_RELEASE)
                    return;

                if (key == GLFW_KEY_ESCAPE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        });

        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (width > 0 && height > 0 && (TestGLFW.this.width != width || TestGLFW.this.height != height)) {
                    TestGLFW.this.width = width;
                    TestGLFW.this.height = height;
                    TestGLFW.this.resetFramebuffer = true;
                }
            }
        });

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
        glfwShowWindow(window);

        try (MemoryStack frame = MemoryStack.stackPush()) {
            IntBuffer framebufferSize = frame.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            width = framebufferSize.get(0);
            height = framebufferSize.get(1);
        }
    }

    void createFBOs() {

        /* Create single-sampled FBO */
        colorTexture = glGenTextures();

        Random random = new Random();
        ByteBuffer buffer = BufferUtils.createByteBuffer(width*height * 4);

        for (int idy = 0; idy < height; idy++) {
            for (int idx = 0; idx < width; idx++) {
                int pixel = (int) random.nextLong();
                buffer.put((byte) ((pixel >> 24) & 0xFF));
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
            }
        }
        buffer.rewind();
        glBindTexture(GL_TEXTURE_2D, colorTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); // we also want to sample this texture later
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); // we also want to sample this texture later
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,buffer);


        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0);

        int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw new AssertionError("Could not create FBO: " + fboStatus);
        }
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    void resizeFBOs() {
        /* Delete multisampled FBO objects */
//        glDeleteRenderbuffers(multisampledDepthRenderBuffer);
//        glDeleteRenderbuffers(multisampledColorRenderBuffer);
//        glDeleteFramebuffers(multisampledFbo);
        /* Delete single-sampled FBO objects */
        glDeleteTextures(colorTexture);
        glDeleteFramebuffers(fbo);
        /* Recreate everything */
        createFBOs();
    }

    void update() {
        if (resetFramebuffer) {
            resizeFBOs();
            resetFramebuffer = false;
        }
    }

    void renderLoop() {
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        debugProc = GLUtil.setupDebugMessageCallback();
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glColor3f(0.1f, 0.1f, 0.1f);



        /* Initially create the FBOs */
        createFBOs();

        long lastTime = System.nanoTime();
        while (!destroyed) {
            /* Update the FBO if the window changed in size */
            update();



            /* Now you can just read from the resolved colorTexture */

            /* But we will just draw it on the viewport using a fullscreen quad */
            glEnable(GL_TEXTURE_2D);
            glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
            glBindTexture(GL_TEXTURE_2D, colorTexture);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2f(-1, -1);
            glTexCoord2f(1, 0);
            glVertex2f(1, -1);
            glTexCoord2f(1, 1);
            glVertex2f(1, 1);
            glTexCoord2f(0, 1);
            glVertex2f(-1, 1);
            glEnd();
            glDisable(GL_TEXTURE_2D);

            synchronized (lock) {
                if (!destroyed) {
                    glfwSwapBuffers(window);
                }
            }
        }
    }

    void winProcLoop() {
        /*
         * Start new thread to have the OpenGL context current in and which does
         * the rendering.
         */
        new Thread(new Runnable() {
            public void run() {
                renderLoop();
            }
        }).start();

        while (!glfwWindowShouldClose(window)) {
            glfwWaitEvents();
        }
    }

    public static void main(String[] args) {
        new TestGLFW().run();
    }

}
