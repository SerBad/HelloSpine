package cc.fotoplace.spine;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.badoo.mobile.util.WeakHandler;

import cc.fotoplace.R;
import cc.fotoplace.base.InterceptableViewGroup;
import permissions.dispatcher.NeedsPermission;


/**
 * Created by QJoy on 2017.12.25.
 */


public class LibgdxSpineFragment extends AndroidFragmentApplication implements InputProcessor {

    private static final String TAG = LibgdxSpineFragment.class.getSimpleName();
    public static boolean openDEBUGLog = false;
    private View m_viewRooter = null;
    //粒子效果UI容器层
    private InterceptableViewGroup mContainer;
    //粒子效果绘制层
    private LibgdxSpineEffectView spineEffectView;
    //Fragment 处于销毁过程中标志位
    private boolean m_isDestorying = false;
    //Fragment 处于OnStop标志位
    private boolean m_isStoping = false;
    //Screen 是否需要重建播放
    private boolean m_isNeedBuild = true;

    private boolean m_hasBuilt = false;

    private WeakHandler m_WeakHandler = new WeakHandler();


    public void setAction(String actionName) {
        if (spineEffectView != null)
            spineEffectView.setAction(actionName);
    }

    public void preDestory() {

        if (openDEBUGLog)
            Log.d(TAG, "preDestory");

        if (!m_hasBuilt)
            return;

        spineEffectView.forceOver();
        spineEffectView.setCanDraw(false);

        m_isDestorying = true;
        m_isStoping = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (openDEBUGLog)
            Log.d(TAG, "onCreateView");

        m_viewRooter = inflater.inflate(R.layout.lf_layout_giftparticle, null);
        return m_viewRooter;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        if (openDEBUGLog)
            Log.d(TAG, "onViewCreated");

        super.onViewCreated(view, savedInstanceState);
        buildGDX();
    }

    public void buildGDX() {

        if (openDEBUGLog)
            Log.d(TAG, "buildGDX");
        mContainer = (InterceptableViewGroup) m_viewRooter.findViewById(R.id.container);
        spineEffectView = new LibgdxSpineEffectView(mContainer.getMeasuredWidth(), mContainer.getMeasuredHeight());
        View effectView = CreateGLAlpha(spineEffectView);

//        effectView.setVisibility(View.INVISIBLE);
//        ((GLSurfaceView) effectView).onResume();

        mContainer.addView(effectView);
        View placeView = new View(getContext());
        placeView.setBackgroundColor(Color.BLUE);

        mContainer.addView(placeView);


        Gdx.input.setInputProcessor(LibgdxSpineFragment.this);
        Gdx.input.setCatchBackKey(true);
        mContainer.setIntercept(true);
        m_hasBuilt = true;

        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                spineEffectView.setHeight(mContainer.getMeasuredHeight());
                spineEffectView.setWidth(mContainer.getMeasuredWidth());
            }
        });


    }

    @Override
    public void onStart() {

        if (openDEBUGLog)
            Log.d(TAG, "onStart");

        m_isStoping = false;
        super.onStart();

        if (spineEffectView != null)
            spineEffectView.setCanDraw(true);
    }

    @Override
    public void onStop() {

        if (openDEBUGLog)
            Log.d(TAG, "onStop");

        m_isStoping = true;
        spineEffectView.setCanDraw(false);
        super.onStop();
    }

    @Override
    public void onResume() {

        if (openDEBUGLog)
            Log.d(TAG, "onResume");

        super.onResume();

        if (spineEffectView != null) {
            spineEffectView.closeforceOver();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .create();
                    View view = new View(getContext());
                    view.setBackgroundColor(Color.RED);
                    view.setLayoutParams(new ViewGroup.LayoutParams(mContainer.getMeasuredWidth(), mContainer.getMeasuredHeight()));
                    dialog.setView(view);

                    dialog.setMessage("假装挡一下");
                    Window window = dialog.getWindow();
                    window.setLayout(mContainer.getMeasuredWidth(), mContainer.getMeasuredHeight());
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onPause() {

        if (openDEBUGLog)
            Log.d(TAG, "onPause");

        if (spineEffectView != null) {
            spineEffectView.forceOver();
        }

        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {

        if (openDEBUGLog)
            Log.d(TAG, "onConfigurationChanged");

        super.onConfigurationChanged(config);

        mContainer.removeAllViews();
        buildGDX();
    }

    private View CreateGLAlpha(ApplicationListener application) {

        if (openDEBUGLog)
            Log.d(TAG, "CreateGLAlpha");

        //	    GLSurfaceView透明相关
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.r = cfg.g = cfg.b = cfg.a = 8;

        View view = initializeForView(application, cfg);

        if (view instanceof SurfaceView) {
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setZOrderMediaOverlay(true);
            glView.setZOrderOnTop(true);

        }


        return view;
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void check() {

    }

    @Override
    public boolean keyDown(int i) {

        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

    private boolean isScreenLock() {
        try {
            PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
            return !isScreenOn;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
