package cc.fotoplace;

import android.Manifest;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Calendar;

import javax.microedition.khronos.opengles.GL10;

import cc.fotoplace.base.FileUtils;
import cc.fotoplace.offscreen.GLSurface;
import cc.fotoplace.offscreen.TestRenderer;
import cc.fotoplace.spine.LibgdxSpineFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class AndroidLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    private LibgdxSpineFragment m_libgdxFgm;
    private TextView click_view;

    /**
     * 将数据转换成bitmap(OpenGL和Android的Bitmap色彩空间不一致，这里需要做转换)
     *
     * @param width  图像宽度
     * @param height 图像高度
     * @param ib     图像数据
     * @return bitmap
     */
    private static Bitmap frameToBitmap(int width, int height, IntBuffer ib) {
        int pixs[] = ib.array();
        // 扫描转置(OpenGl:左上->右下 Bitmap:左下->右上)
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++) {
                int pos1 = y * width + x;
                int pos2 = (height - 1 - y) * width + x;

                int tmp = pixs[pos1];
                pixs[pos1] = (pixs[pos2] & 0xFF00FF00) | ((pixs[pos2] >> 16) & 0xff) | ((pixs[pos2] << 16) & 0x00ff0000); // ABGR->ARGB
                pixs[pos2] = (tmp & 0xFF00FF00) | ((tmp >> 16) & 0xff) | ((tmp << 16) & 0x00ff0000);
            }
        }
        if (height % 2 == 1) { // 中间一行
            for (int x = 0; x < width; x++) {
                int pos = (height / 2 + 1) * width + x;
                pixs[pos] = (pixs[pos] & 0xFF00FF00) | ((pixs[pos] >> 16) & 0xff) | ((pixs[pos] << 16) & 0x00ff0000);
            }
        }

        return Bitmap.createBitmap(pixs, width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spine_activity);
        AndroidLauncherPermissionsDispatcher.checkWithCheck(this);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        m_libgdxFgm = (LibgdxSpineFragment) getSupportFragmentManager().findFragmentById(R.id.libgdxFrag);

        click_view = findViewById(R.id.click_view);
        click_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "这里开始执行", Toast.LENGTH_SHORT).show();
                startOffScreen();
            }
        });
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void check() {

    }

    @Override
    public void finish() {
        m_libgdxFgm.preDestory();
        super.finish();
    }

    @Override
    public void exit() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void startOffScreen() {
        GLSurface glPufferSurface = new GLSurface(512, 512);
        TestRenderer glRenderer = new TestRenderer();
        glRenderer.addSurface(glPufferSurface);
        glRenderer.startRender();
        glRenderer.requestRender();

        glRenderer.postRunnable(new Runnable() {
            @Override
            public void run() {
                IntBuffer ib = IntBuffer.allocate(512 * 512);
                GLES20.glReadPixels(0, 0, 512, 512, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
                Bitmap bitmap = frameToBitmap(512, 512, ib);
                String name = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "离屏数据" + Calendar.getInstance().getTimeInMillis() + ".jpeg";

                try {
                    FileUtils.saveToFile(name, bitmap);
                    Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


}
