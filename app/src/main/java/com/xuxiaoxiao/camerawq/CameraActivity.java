package com.xuxiaoxiao.camerawq;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by mac on 2016/12/30.
 */
public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    Camera camera;
    @InjectView(R.id.surfaceView)
    SurfaceView surfaceView;
    @InjectView(R.id.btn_take_photo)
    FloatingActionButton btn_take_photo;
    SurfaceHolder surfaceHolder;
    Camera.PictureCallback jpegCallback;
    Camera.ShutterCallback shutterCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        ButterKnife.inject(this);
        surfaceHolder = surfaceView.getHolder();
        // Install a surfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);
        // 3.0 之前用
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btn_take_photo.setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraImage();
            }
        });
        jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                FileOutputStream outputStream = null;
                File file_image = getDirc();
                if (!file_image.exists() && !file_image.mkdirs()) {
                    Toast.makeText(getApplicationContext(), "Can't create directory to save image", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                String date = simpleDateFormat.format(new Date());
                String photofile = "Cam_Demo" + date + ".jpg";
//                String photofile = "Cam_Demo" + date + ".jpg";
                String file_name = file_image.getAbsolutePath() + "/" + photofile;
                File picfile = new File(file_name);
                try {
                    outputStream = new FileOutputStream(picfile);
                    outputStream.write(bytes);
                    outputStream.close();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                } finally {

                }
                Toast.makeText(getApplicationContext(), "Picture saved", Toast.LENGTH_SHORT).show();
                refreshCamera();
                refreshGallery(picfile);
            }
        };
    }

    private void refreshGallery(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
//        intent.setDate(Uri.fromFile(file));
//        startActivity(intent);
        sendBroadcast(intent);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {

        }
        //set preview size and make any resize ,rotate or
        // reformatting changes here start preview with new setting
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (Exception e){}
    }


    private File getDirc() {
        File dics = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dics, "Camera_Demo");
    }

    public void cameraImage() {
        // take the picture
        camera.takePicture(null, null, jpegCallback);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // Open the camera
        try {
            camera = Camera.open();
        } catch (RuntimeException ex) {

        }
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        // modify parameter
        parameters.setPreviewFrameRate(20);
        parameters.setPreviewSize(352, 288);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);

        try {
            // The surface has been created ,now tell the camera where to draw
            // the preview
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception ex) {
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        refreshCamera();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;

    }
}
