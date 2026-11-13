package ai.ocrstudio.sdk;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.ocrstudio.sdk.module.CameraActivity;
import ai.ocrstudio.sdk.module.Engine;
import ai.ocrstudio.sdk.module.Image;
import ai.ocrstudio.sdk.module.ResultStore;
import ai.ocrstudio.sdk.module.SettingsStore;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * OCRStudioSDKPlugin
 */
public class OCRStudioSDKPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    String TAG = "flutter_plugin";
    private static final int OCR_REQUEST = 1;
    private static final String ERROR_CODE = "errorCode";
    private static FlutterPluginBinding pluginBinding;
    private Activity activity;

    // Saved result callback for activity
    private static Result resultResponse = null;

    // Event stream
    private static EventChannel eventStream;
    private static EventChannel.EventSink mEventSink;

    private Result r;

    // ----------------------- Engine Binding -----------------------
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        // TODO: your plugin is now attached to a Flutter experience.

        pluginBinding = flutterPluginBinding;
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "ocrstudiosdk");
        channel.setMethodCallHandler(this);
        // Engine loading state

        eventStream = null;
        mEventSink = null;
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        // TODO: your plugin is no longer attached to a Flutter experience.
        channel.setMethodCallHandler(null);

        pluginBinding = null;
        eventStream = null;
        mEventSink = null;
    }

    // ----------------------- Activity Binding -----------------------
    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        // TODO: your plugin is now attached to an Activity
        activity = activityPluginBinding.getActivity();
        activityPluginBinding.addActivityResultListener(this);

        eventStream = new EventChannel(pluginBinding.getBinaryMessenger(), "eventStream");
        eventStream.setStreamHandler(
                new EventChannel.StreamHandler() {

                    @Override
                    public void onListen(Object args, EventChannel.EventSink events) {
                        mEventSink = events;
                    }

                    @Override
                    public void onCancel(Object args) {
                        // w("TAG", "cancelling listener");
                    }
                }
        );
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        // TODO: the Activity your plugin was attached to was
        // destroyed to change configuration.
        // This call will be followed by onReattachedToActivityForConfigChanges().
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        // TODO: your plugin is now attached to a new Activity
        // after a configuration change.
    }

    @Override
    public void onDetachedFromActivity() {
        // TODO: your plugin is no longer associated with an Activity.
        // Clean up references.
        pluginBinding = null;
        activity = null;
        mEventSink = null;
    }

    // ----------------------- MethodCall -----------------------



    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        switch (call.method) {

            // Core
            case "startRecognition":
                // Save link for onActivityResult
                resultResponse = result;
                startRecognition();
                break;
            case "fileRecognition":
                final byte[] file = call.argument("file");
                fileRecognition(file, result);
                break;
            case "init":
                final String signature = call.argument("signature");
                init(signature, result);
                break;
            // Fill settings store
            case "initSettings":
                final Map settings = call.argument("settings");
                initSettings(settings, result);
                break;
            // Custom
            case "getVersion":
                getVersion(result);
                break;
            // Get engine description
            case "engineDescription":
                engineDescription(result);
                break;
            case "setLabelText":
                final String text = call.argument("param");
                setLabelText(text, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /*
     * ========================================================================
     * ========================== Core methods ================================
     * ========================================================================
     */

    private String prepareResult() {
        // Get data from store
       return ResultStore.instance.getResult();
    }

    private void startRecognition() {

        try {
            // Why it's not being used registerForActivityResult
            // https://github.com/flutter/flutter/issues/133667
            activity.startActivityForResult(new Intent(activity, CameraActivity.class), OCR_REQUEST);

        } catch (Exception e) {
            System.out.println(e);
            resultResponse.error(ERROR_CODE, e.getMessage(), null);
        }
    }

    private void fileRecognition(byte[] file, Result result) {

        // Another thread used in case of http request inside methods.
        // Http request blocked on main thread
        ExecutorService SessionExecutor = Executors.newSingleThreadExecutor();
        SessionExecutor.execute(() -> {

            try {
                OCRStudioSDKImage image = Image.createFromByteArray(file);
                ai.ocrstudio.sdk.module.Engine.getResultFromFile(image);
                result.success(prepareResult());
            } catch (Exception e) {
                System.out.println(e);
                result.error("UNAVAILABLE", e.getMessage(), null);
            }
        });
    }

    private void init(String signature, Result result){
        try {
            // signature
            SettingsStore.SetSignature(signature);
            // Return result from store in json
            SettingsStore.isJsonResult = true;

            if (Engine.getInstance() == null) {
                // Init engine
                Engine.loadSync(pluginBinding.getApplicationContext());
                // If something went wrong with initialization
                if (Engine.loadingState.getValue() == Engine.LoadingState.Error) {
                    result.error("ERROR", Engine.error, null);
                }
            }
            result.success(null);
        } catch (Exception e) {
            result.error(ERROR_CODE, e.getMessage(), "init() Error");
        }
    }

    private void initSettings(Map<String, Object> settings, Result result) {
        try {

            // mode
            String mode = (String) settings.get("mode");
            SettingsStore.SetMode(mode);

            // mask
            ArrayList mask = (ArrayList) settings.get("mask");
            SettingsStore.SetMask(mask);

            // options
            Map options = (Map) settings.get("options");
            Map<String, String> newMap = new HashMap(options);
            SettingsStore.SetOptions(newMap);

            result.success(null);
        } catch (Exception e) {
            result.error(ERROR_CODE, e.getMessage(), "initSettings() Error");
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == OCR_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // Log.i("JSON", result);
                resultResponse.success(prepareResult());
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                resultResponse.success(null);
            }
        }
        return false;
    }

    /*
     * ========================================================================
     * ======================== Custom methods ================================
     * ========================================================================
     */

    private void getVersion(Result result) {
        try {
            String version = OCRStudioSDKInstance.LibraryVersion();
            result.success(version);
        } catch (Exception e) {
            result.error("ERROR", e.getMessage(), null);
        }
    }

    private void engineDescription(Result result) {
        try {
            // Init if not exist
            if (Engine.getInstance() == null) {
                result.error("ERROR", "getMessage()", null);
            }
            String desc = Engine.engineDescription();
            result.success(desc);
        } catch (Exception e) {
            result.error("ERROR", e.getMessage(), null);
        }
    }

    public void setLabelText(String txt, Result result) {
        ai.ocrstudio.sdk.module.Label.getInstance().message.set(txt);
        result.success(null);
    }
    /*
     * ========================================================================
     * ======================== Events to DART ================================
     * ========================================================================
     */

    public static void streamRecognized(OCRStudioSDKResult result, int frameCounter) {

        // ==== DEFAULT MODE SECTION ====
        HashMap<String, String> params = new HashMap<>();
        params.put("frameIndex", String.valueOf(frameCounter));
        mEventSink.success(params);
    }
}

