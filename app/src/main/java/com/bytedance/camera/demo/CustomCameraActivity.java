package com.bytedance.camera.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;
    private Button button_record;
    private Button button_face;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);
        mSurfaceView = findViewById(R.id.img);
        SurfaceHolder mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startPreview(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            mCamera.takePicture(null, null, mPicture);
            Toast.makeText(this,"拍照成功",Toast.LENGTH_SHORT).show();
        });
        button_record = findViewById(R.id.btn_record);
        button_record.setOnClickListener(v -> {
            if (isRecording) {
                releaseMediaRecorder();
                mCamera.lock();
                button_record.setText("开始录制");
                isRecording = false;
                Toast.makeText(this,"录制已经完成",Toast.LENGTH_SHORT).show();
            } else {
                if(prepareVideoRecorder()){
                    isRecording = true;
                    button_record.setText("结束录制");
                    Toast.makeText(this,"录制已经开始",Toast.LENGTH_SHORT).show();
                } else
                    releaseMediaRecorder();
            }
        });
        button_face = findViewById(R.id.btn_facing);
        button_face.setOnClickListener(v -> {
            if(CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK){
                CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_FRONT;
                button_face.setText("后置");
            }
            else{
                CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
                button_face.setText("前置");
            }
            startPreview(mHolder);
        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            Camera.Parameters parameters = mCamera.getParameters();
            if(parameters.isZoomSupported()) {
                int zoom = parameters.getZoom() + 5;
                if (zoom < parameters.getMaxZoom()) {
                    parameters.setZoom(zoom);
                    mCamera.setParameters(parameters);
                }
            }
        });
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        //设置旋转方向
        rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);
        //自动对焦
        Camera.Parameters p = cam.getParameters();
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        cam.setParameters(p);
        cam.startPreview();

        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        mCamera = getCamera(CAMERA_TYPE);
        Camera.Parameters parameters = mCamera.getParameters();
        size = getOptimalPreviewSize(parameters.getSupportedPictureSizes(),mSurfaceView.getWidth(),mSurfaceView.getHeight());
        parameters.setPictureSize(size.width,size.height);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void releaseMediaRecorder() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            RotateImage(bmp, pictureFile.getAbsolutePath()).compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    //旋转照片角度
    private Bitmap RotateImage(Bitmap bitmap, String path) {
        ExifInterface srcExif = null;
        try {
            srcExif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
