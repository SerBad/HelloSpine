package cc.fotoplace;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import cc.fotoplace.spine.LibgdxSpineFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class AndroidLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks {
    private LibgdxSpineFragment m_libgdxFgm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spine_activity);
        AndroidLauncherPermissionsDispatcher.checkWithCheck(this);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        m_libgdxFgm = (LibgdxSpineFragment) getSupportFragmentManager().findFragmentById(R.id.libgdxFrag);
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
}
