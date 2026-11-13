/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import android.graphics.Rect;
import android.util.Log;

import androidx.camera.core.ImageProxy;
import androidx.lifecycle.ViewModel;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A view mode for CameraActivity
 */
public class CameraViewModel extends ViewModel {
    private final String TAG = "CameraViewModel";

    public final ExecutorService executor;// video analyse thread
    private int height;
    private int width;
    private List<Integer> crop_rect;
    private int rotationTimes = 0;
    private boolean init_once = true;

    // CONSTRUCTOR
    public CameraViewModel(){
        //Log.d(TAG, "constructor");
        executor = Executors.newSingleThreadExecutor();
    }
    // DESTRUCTOR
    @Override
    protected void onCleared() {
        super.onCleared();
        //Log.d(TAG, "destructor - onCleared");
        executor.shutdown();
    }

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public OCRStudioSDKResult onVideoFrame(ImageProxy image, int heightPreview) {
        Log.d(TAG, "onVideoFrame");

        //  initialized only once:
        if (init_once) {
            // Get sensor orientation
            int imageRotationDegrees = image.getImageInfo().getRotationDegrees();
            // Calculate rotation image counts
            rotationTimes = imageRotationDegrees / 90;
            // Cropping rectangle
            Rect crop = image.getCropRect();

            // Rotate crop rectangle if needed
            if (imageRotationDegrees == 0 || imageRotationDegrees == 180) {
                // If smartphone in landscape - NOT TESTED

                // width = image.getWidth(); // ~1280
                // height = image.getHeight(); // ~960
                // // Set scale for canvas drawing
                // int heightPreview = binding.cameraView.getWidth();
                // Draw.scale = (float) heightPreview / (float) height;
                // // Calculate crop rectangle
                // crop_rect = new Rectangle(crop.left, crop.top, crop.right - crop.left, crop.bottom);
            } else {
                width = image.getWidth(); // ~1280
                height = image.getHeight(); // ~960

                // Set scale for canvas drawing
                Draw.scale = (float) heightPreview / (float) width;
                // Calculate crop rectangle
                /**
                 * Rectangle:
                 * int x, X-coordinate of the top-left corner
                 * int y, Y-coordinate of the top-left corner
                 * int width, Width of the rectangle
                 * int height, Height of the rectangle
                 */

                crop_rect = Arrays.asList(crop.top, crop.left, crop.bottom - crop.top,  crop.right);
            }
            init_once = false;
        }

        OCRStudioSDKImage frame = Image.createFromYUV_420_888(image, width, height);

        frame.RotateByNinety(rotationTimes);

        // CROP THE IMAGE
        // According to our tests without cropping frame (W=546 H=1088) for image (W=1088 H=1088)
        // the recognition speed decreases by ~125ms per frame (tested on Helio G90T)
        frame.CropByRect(crop_rect.get(0),
                crop_rect.get(1),
                crop_rect.get(2),
                crop_rect.get(3));

        // PROCESS THE IMAGE
        CameraSession.instance.processImage(frame);

        // RETURN THE RESULT
        return CameraSession.instance.getCurrentResult();
    }
}
