package cc.fotoplace.spine;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

/**
 * Created by QJoy on 2017.12.25.
 */
@SuppressWarnings("All")
public class LibgdxSpineEffectView implements ApplicationListener {

    private static final String TAG = LibgdxSpineEffectView.class.getSimpleName();
    public static boolean openDEBUGLog = false;
    //基础绘制资源
    SpriteBatch mBatch;

    //	Asset资源加载管理
    AssetManager mAssetManager = new AssetManager();

    int mWidth = 0;
    OrthographicCamera camera;
    SpriteBatch batch;
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;
    TextureAtlas atlas;
    Skeleton skeleton;
    SkeletonBounds bounds;
    AnimationState state;
    int width = 0;
    int height = 0;
    private boolean forceOver = false;
    private boolean mIsLand = false;
    private boolean m_candraw = true;
    private volatile boolean loadingAnimate = true;
    private int time = 0;

    public LibgdxSpineEffectView(int width, int height) {
        this.width = width;
        this.height = height;

    }

    public void forceOver() {

        if (openDEBUGLog)
            Log.d(TAG, "forceOver");

        forceOver = true;

//		缓冲50ms，解决退出时绘制闪动的问题
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeforceOver() {

        if (openDEBUGLog)
            Log.d(TAG, "closeforceOver");

        forceOver = false;

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setAction(String actionName) {
        try {
            switch (actionName) {
                case "run":
                    state.setAnimation(0, "run", true);
                    break;
                case "walk":
                    state.setAnimation(0, "walk", true);
                    break;
                case "jump":
                    state.setAnimation(0, "jump", false);
                    state.addAnimation(0, "run", true, 0);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存图片文件
    public String saveToFile(String fileFolderStr, Bitmap croppedImage) throws FileNotFoundException, IOException {
        File jpgFile = new File(fileFolderStr);
        if (!jpgFile.getParentFile().exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
            mkdir(jpgFile.getParentFile());
        }
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
        croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
        return jpgFile.getPath();
    }

    public boolean mkdir(File file) {
        while (!file.getParentFile().exists()) {
            mkdir(file.getParentFile());
        }
        return file.mkdir();
    }

    @Override
    public void create() {

        if (openDEBUGLog)
            Log.d(TAG, "create");

        mBatch = new SpriteBatch();

        mWidth = Gdx.graphics.getWidth() > Gdx.graphics.getHeight() ? Gdx.graphics.getHeight() : Gdx.graphics.getWidth();

        //放置 init 方法

        camera = new OrthographicCamera();
        batch = new SpriteBatch();
        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(true); // PMA results in correct blending without outlines.
        debugRenderer = new SkeletonRendererDebug();
        debugRenderer.setBoundingBoxes(false);
        debugRenderer.setRegionAttachments(false);

        /*
         *		libgdx资源加载必须在create所在线程执行，如果此步骤耗时过程可以考虑使用AssetManager的方式加载
         * */
        atlas = new TextureAtlas(Gdx.files.internal("demo.atlas"));

        /*
         *		spine动画加载数据过大，耗时过长，可以移动到子线程中执行
         * */
        new Thread(new Runnable() {
            @Override
            public void run() {

                SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
                json.setScale(0.6f); // Load the skeleton at 60% the size it was in Spine.

                SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("demo.json"));

                skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
                skeleton.setPosition(mWidth / 2, 20);

                AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.
                stateData.setMix("baipose", "baipose", 0.2f);
//				stateData.setMix("jump", "run", 0.2f);
//
//				stateData.setMix("run","walk",1f);
//				stateData.setMix("walk","run",1f);

                state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
                state.setTimeScale(1.0f); // Slow all animations down to 50% speed.

                // Queue animations on track 0.
                state.setAnimation(0, "baipose", true);

                loadingAnimate = false;
            }
        }).start();

    }

    @Override
    public void resize(int i, int i2) {
        camera.setToOrtho(false); // Update camera with new size.
    }

    public void setCanDraw(boolean candraw) {

        if (openDEBUGLog)
            Log.d(TAG, "setCanDraw:" + candraw);

        m_candraw = candraw;

        if (!m_candraw) {
//			放置不可见时手动销毁的内容
            return;
        }
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (forceOver)
            return;

        if (!m_candraw) {
            //			放置不可见时手动销毁的内容
            return;
        }

        if (loadingAnimate)
            return;

//		放置动画逻辑
        state.update(Gdx.graphics.getDeltaTime()); // Update the animation time.

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        state.apply(skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.

        // Configure the camera, SpriteBatch, and SkeletonRendererDebug.
        camera.update();
        batch.getProjectionMatrix().set(camera.combined);
        debugRenderer.getShapeRenderer().setProjectionMatrix(camera.combined);

        batch.begin();
        renderer.draw(batch, skeleton); // Draw the skeleton images.
        batch.end();

        if (time <= 2) {
            time++;
            try {
                Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                ByteBuffer pixels1 = pixmap.getPixels();

                Gdx.gl.glReadPixels(0, 0, width, height, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels1);

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                bitmap.copyPixelsFromBuffer(pixels1);

                Matrix matrix = new Matrix();
                matrix.postScale(1, -1);

                String name = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Calendar.getInstance().getTimeInMillis() + ".jpeg";
                saveToFile(name, Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//		debugRenderer.draw(skeleton); // Draw debug lines.

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

        if (openDEBUGLog)
            Log.d(TAG, "dispose");

        mBatch.dispose();

        //遍历释放所有资源
        atlas.dispose();
    }
}
