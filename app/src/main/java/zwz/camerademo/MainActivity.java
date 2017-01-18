package zwz.camerademo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.File;

/**
 * http://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed
 */
public class MainActivity extends AppCompatActivity implements PermissionListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private String cameraOutputPath;//拍照自定义图片保存路径
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image= (ImageView) findViewById(R.id.image);

        findViewById(R.id.btnCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermission();
            }
        });
    }

    @Override
    public void onSucceed(int requestCode) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                takePhoto();
                break;
        }
    }

    @Override
    public void onFailed(int requestCode) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (AndPermission.getShouldShowRationalePermissions(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "获取相机权限失败", Toast.LENGTH_SHORT).show();
                } else {
                    //拒绝权限，并选择了记住选择
                    Toast.makeText(MainActivity.this, "您已拒绝了相机权限，并且下次不再提示，如果你要继续使用此功能，请在设置中为我们授权相机权限。", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private void takePhoto() {
        Uri uri = getOutputUri();
        if (uri != null) {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, 1);
        }
    }

    private Uri getOutputUri() {
        cameraOutputPath = getTempPath();
        File f = new File(cameraOutputPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri photoURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName()
                    + ".provider", f);

                 /* 第二种方式
                 ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
            L.d("contentValues="+uri.getPath());*/

            return photoURI;
        } else {
            return Uri.fromFile(f);
        }


    }
    private  String getTempPath() {
        String path;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File dir=new File(Environment.getExternalStorageDirectory()+"/zwz");
            //mkdirs()可以建立多级文件夹， mkdir()只会建立一级的文件夹
            dir.mkdirs();
            path=dir.getAbsolutePath()+"/"+System.currentTimeMillis()+".jpg";
        }else {
            path=getCacheDir().getAbsolutePath()+"/"+System.currentTimeMillis()+".jpg";
        }
        return path;
    }

    private void requestCameraPermission() {
        AndPermission.with(this)
                .requestCode(REQUEST_CAMERA_PERMISSION)
                .permission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .send();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    if (cameraOutputPath != null) {
                        if (cameraOutputPath.endsWith("jpg") || cameraOutputPath.endsWith("png")
                                || cameraOutputPath.endsWith("gif")
                                || cameraOutputPath.endsWith("jpeg")) {
                            //TODO
                            Glide.with(MainActivity.this).load(cameraOutputPath).into(image);
                        }
                    } else {


                    }
                    break;

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 这个Activity中没有Fragment，这句话可以注释。
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 没有Listener，最后的PermissionListener参数不写。
        //        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        // 有Listener，最后需要写PermissionListener参数。
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }
}
