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
    public static final int REQUEST_CODE = 1;
    public TextView cam0_info;
    public String[] cameraIds = {"0", "1", "2", "3", "4"};
    private static final String DEFAULT_CAMERA_ID = "0";
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
//        camera permission seems not need
//        checkPermission(this);
        GetCameraInfo();

    }

    private void GetCameraInfo() {
        Log.d(TAG, "GetCameraInfo: ");
        if (cameraIds == null || cameraIds.length == 0) {
            Log.e(TAG, "cameraIds is empty");
            return;
        }

        all_cam_info = "";
        int cameraCount = cameraIds.length;
        CamInfo[] camInfo = new CamInfo[cameraCount];
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

        try {
            Log.d(TAG, "setUpCameraOutputs: manager.getCameraIdList() size: "
                    + manager.getCameraIdList().length);
            for (int i = 0; i < cameraCount; i++) {
                String cameraId = cameraIds[i];

                // 为每个camera单独处理，避免一个失败影响其他
                // 直接调用getCameraCharacteristics，如果cameraId不存在会抛出IllegalArgumentException
                try {
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

                    float fov_ratio = calculateFOVratio(activeArraySize, pixelSize, focalLength);
                    camInfo[i] = new CamInfo(cameraId, fov_ratio);

                    // 安全地访问camInfo[0]
                    if (!cameraId.equals(DEFAULT_CAMERA_ID) && camInfo[0] != null && camInfo[i] != null) {
                        if (camInfo[i].fovRatio != 0) {
                            Log.d(TAG, "camera id " + camInfo[0].id + " and " + camInfo[i].id + " ratio:" + (camInfo[0].fovRatio / camInfo[i].fovRatio));
                        } else {
                            Log.w(TAG, "fovRatio is 0 for cameraId: " + cameraId);
                        }
                    }

                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException for cameraId: " + cameraId, e);
                    all_cam_info = all_cam_info + "cameraId:" + cameraId + " (can not get camera info)\n\n";
                } catch (CameraAccessException e) {
                    Log.e(TAG, "CameraAccessException for cameraId: " + cameraId, e);
                    all_cam_info = all_cam_info + "cameraId:" + cameraId + " (access denied)\n\n";
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected exception for cameraId: " + cameraId, e);
                    all_cam_info = all_cam_info + "cameraId:" + cameraId + " (error: " + e.getMessage() + ")\n\n";
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException when getting camera list", e);
            all_cam_info = "Error: Cannot access camera service";
        } catch (Exception e) {
            Log.e(TAG, "Unexpected exception in GetCameraInfo", e);
            all_cam_info = "Error: " + e.getMessage();
        }

        // 安全地处理camInfo数组
        if (camInfo[0] != null) {
            for (CamInfo info : camInfo) {
                if (info != null && !info.id.equals(DEFAULT_CAMERA_ID) && camInfo[0] != null) {
                    if (info.fovRatio != 0) {
                        all_cam_info = all_cam_info + "\ncamera id " + camInfo[0].id + " and " + info.id + " ratio:" + (camInfo[0].fovRatio / info.fovRatio);
                    }
                }
            }
        }

        if (cam0_info != null) {
            cam0_info.setText(all_cam_info.isEmpty() ? "No camera information available" : all_cam_info);
        }
    }

    private float calculateFOVratio(Rect activeArraySize, float pixelSize, float[] focalLength) {
        return activeArraySize.width() * pixelSize / focalLength[0];
    }


    public void checkPermission(Activity activity) {

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

    private boolean shouldShowRequestPermissionRationale() {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the feature requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }
}