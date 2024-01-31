package com.tenthorange.getcamerachar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static class CamInfo {
        public String id;
        public float fovRatio;

        public CamInfo(String id, float fovRatio) {
            this.id = id;
            this.fovRatio = fovRatio;
        }
    }

    private static final String TAG = "MainActivity";
    public static int REQUEST_CODE = 1;
    public TextView cam0_info;
    public String[] cameraIds = {"0", "1", "2", "3"};
    public String all_cam_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cam0_info = findViewById(R.id.cam0_info);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(this);
        all_cam_info = "";
        int cameraCount = cameraIds.length;
        CamInfo[] camInfo = new CamInfo[cameraCount];
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        int i = 0;
        try {
            Log.d(TAG, "setUpCameraOutputs: manager.getCameraIdList() size: "
                    + manager.getCameraIdList().length);
            for (String cameraId : cameraIds) {
                // override camera id for debug
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Size pixelArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
                float[] focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                SizeF physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                float pixelSize = physicalSize.getWidth() / pixelArraySize.getWidth() * 1000;
                Log.d(TAG, "cameraId:" + cameraId + ", SENSOR_INFO_PIXEL_ARRAY_SIZE: " + pixelArraySize);
                Log.d(TAG, "cameraId:" + cameraId + ", LENS_INFO_AVAILABLE_FOCAL_LENGTHS: " + focalLength[0]);
                Log.d(TAG, "cameraId:" + cameraId + ", pixel size: " + pixelSize);
                Log.d(TAG, "cameraId:" + cameraId + ", pixel array size: " +
                        pixelArraySize.getWidth() + " " + pixelArraySize.getHeight());

                Rect activeArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                Log.d(TAG, "cameraId:" + cameraId + ", active_array_size l t w h: " +
                        activeArraySize.left + " " + activeArraySize.top + " " +
                        activeArraySize.width() + " " + activeArraySize.height());

                all_cam_info = all_cam_info + "cameraId:" + cameraId
                        + "\nfocal length: " + focalLength[0]
                        + "\npixel array size: " + pixelArraySize
                        + "\npixel size: " + pixelSize + "\n\n";
                float fov_ratio = activeArraySize.width() * pixelSize / focalLength[0];
                camInfo[i++] = new CamInfo(cameraId, fov_ratio);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException");
            e.printStackTrace();
        }
        Log.d(TAG, "camera id " + camInfo[0].id + " and " + camInfo[2].id + " ratio:" + camInfo[0].fovRatio / camInfo[2].fovRatio);
        Log.d(TAG, "camera id " + camInfo[0].id + " and " + camInfo[3].id + " ratio:" + camInfo[0].fovRatio / camInfo[3].fovRatio);

        all_cam_info = all_cam_info + "camera id " + camInfo[0].id + " and " + camInfo[2].id + " ratio:" + (camInfo[0].fovRatio / camInfo[2].fovRatio)
                + "\ncamera id " + camInfo[0].id + " and " + camInfo[3].id + " ratio:" + (camInfo[0].fovRatio / camInfo[3].fovRatio);
        cam0_info.setText(all_cam_info);

    }


    public static void checkPermission(Activity activity) {

        Log.d(TAG, "checkPermission: ");
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
//            performAction(...);
        } else if (shouldShowRequestPermissionRationale()) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
//            showInContextUI(...);
        } else {
            // You can directly ask for the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CODE
            );
        }
    }

    private static boolean shouldShowRequestPermissionRationale() {
        return false;
    }
}