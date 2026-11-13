/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKPlugin;
import ai.ocrstudio.sdk.R;
import ai.ocrstudio.sdk.databinding.ActivityCameraBinding;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Main sample activity for documents recognition
 */
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "myapp.CameraActivity";

    // VIEW MODEL
    CameraViewModel model;

    private final int REQUEST_CAMERA_PERMISSION = 1;
    private boolean pauseAnalysis = true;
    private Button button;

    public static Draw draw;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView cameraView;
    ActivityCameraBinding binding;
    RelativeLayout drawing;

    private double startTime = -1;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        model = new ViewModelProvider(this).get(CameraViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);

        // Bind to UI DRAW class
        draw = new Draw(this.getBaseContext());
        drawing = binding.drawing;
        drawing.addView(draw);

        button = binding.start;
        button.setVisibility(View.INVISIBLE);
        button.setEnabled(false);

        cameraView = binding.cameraView;

        intent = new Intent();

        // Bind label object to xml
        binding.setLabel(Label.getInstance());

        button.setOnClickListener(v -> {
            if (pauseAnalysis) {
                startTime = System.nanoTime();
                pauseAnalysis = false;
                button.setText("CANCEL");
            } else {
                pauseAnalysis = true;
                onCanceled();
            }
        });

        /**
         ** Creating session in async way.
         */
        ExecutorService SessionExecutor = Executors.newSingleThreadExecutor();
        SessionExecutor.execute(() -> {
            long startCrTime = System.nanoTime();
            // Create session on another thread
            CameraSession.instance.createVideoSession();
            long endCrTime = System.nanoTime();
            long duration = (endCrTime - startCrTime) / 1000000;  // divide to get milliseconds.
            Log.d(TAG,"Create Video Session time: " + duration );
            // Show buttons on Camera UI screen
            runOnUiThread(this::onInitialized);
        });

        // Check camera permission before setting up layout listener
        if (hasCameraPermission()) {
            initCameraWithLayoutCheck();
        } else {
            requestCameraPermission(REQUEST_CAMERA_PERMISSION);
        }
    }

    private void initCameraWithLayoutCheck() {
        binding.main.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.main.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initCamera();
            }
        });
    }

    private void initCamera() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        // "cameraView.getDisplay().getRotation()" some times null object reference error
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();

        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        // Camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set up the image analysis
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setResolutionSelector(
                        new ResolutionSelector.Builder()
                                .setResolutionStrategy(
                                        new ResolutionStrategy(new Size(1200, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                                ).build())
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setTargetRotation(rotation)
                .build();

        ViewPort viewPort = ((PreviewView)findViewById(R.id.cameraView)).getViewPort();

        // Use case
        assert viewPort != null;
        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                .addUseCase(preview)
                .setViewPort(viewPort)
                .addUseCase(imageAnalysis)
                .build();

        imageAnalysis.setAnalyzer(model.executor, image -> {

            if (pauseAnalysis){
                image.close();
                return;
            }

            // Try recognition
            try {
                OCRStudioSDKResult result = model.onVideoFrame(image, cameraView.getHeight());

                // Draw Feedback on screen
                draw.showMatching(result);

                // Events. Sending intermediate results during the recognition session:
                runOnUiThread(() -> {
                  // for flutter
                  OCRStudioSDKPlugin.streamRecognized(result, (int) (Math.random() * 100));
                  // for react-native
                  // OCRStudioSDKModule.streamRecognized(result, (int) (Math.random() * 100));
                });

                if (result.AllTargetsFinal()) {
                    // The result is terminal when the engine decides that the recognition result
                    // has had enough information and ready to produce result, or when the session
                    // is timed out

                    // This will stop data from streaming
                    imageAnalysis.clearAnalyzer();
                    pauseAnalysis = true;
                    onRecognized();
                }
            } catch (Exception e) {
                onError(e.getMessage());
                finish();
            }

            // Free resources
            image.close();
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);
        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    void toast(final String message) {

        runOnUiThread(() -> {
            Toast t = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
            t.show();
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean hasCameraPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraPermission(int request_code) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, request_code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            boolean granted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) {
                // Automatically initialize camera once permission is granted by user
                initCameraWithLayoutCheck();
            } else {
                onError("Please allow Camera permission");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onInitialized() {
        // enable buttons
        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);

        long elapsedTime = System.nanoTime() - CameraSession.instance.startTime;
        long t = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        Label.getInstance().message.set("Engine Ready: " + t + "ms");
    }

    public void onRecognized() {
        double elapsedTime = System.nanoTime() - startTime;
        double t = TimeUnit.MILLISECONDS.convert((long) elapsedTime, TimeUnit.NANOSECONDS);

        toast("Time:" + t);
        OCRStudioSDKResult result = CameraSession.instance.getCurrentResult();
        ResultStore.instance.addResult(result);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCanceled() {
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void onError(String message) {
        toast(message);
    }



}
