/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.MutableLiveData;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKInstance;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKPixelFormat;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKSession;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.HttpURLConnection;

/**
 * THE ENGINE WRAPPER SINGLETON
 * Implements engine loading
 * exposes the engine loading state as a LiveData
 */
public class Engine {
    private static final String TAG = "myapp.Engine";
    private static OCRStudioSDKInstance instance = null;
    private static final Character separator = ':';

    // Default data for UI
    @NonNull
    private static String documents = "";

    @NonNull
    private static final List<String> documents2 = new ArrayList<>();

    private static String sessionTypes;
    public enum LoadingState{
        Empty, Loading, Ready, Error
    }
    public static MutableLiveData<LoadingState> loadingState = new MutableLiveData<>(LoadingState.Empty);
    public static String error = null;
    //private

    // Get instance already initiated
    public static OCRStudioSDKInstance getInstance() {return instance;}

    /**
     * Load instance without blocking UI. Typically used for native platform
     */
    public static void loadAsync(Context context) {
        if (loadingState.getValue() == LoadingState.Empty) {
            new Thread(() -> initEngine(context)).start();
        }
    }

    /**
     * Load instance synchronously.
     * Typically used for Flutter/ React Native where synchronous call does not block the UI
     */
    public static void loadSync(Context context) {
        if (loadingState.getValue() == LoadingState.Empty) {
            initEngine(context);
        }
    }


    /**
     * Load required data from local file system
     * and create the OCRStudioSDKInstance object as result.
     * @param context - the application context for file reading operations
     */
    private static void initEngine(Context context) {
        try {
            loadingState.postValue(LoadingState.Loading);

            // Load config file
            byte[] config_data = loadConfigFile(context);
            // load library
            System.loadLibrary("jniocrstudiosdk");

            // Create Engine instance
            JSONObject p = new JSONObject();
            p.put("enable_lazy_initialization",    true);
            p.put("enable_delayed_initialization", false);
            instance = OCRStudioSDKInstance.CreateFromBuffer(config_data, p.toString());

            loadingState.postValue(LoadingState.Ready);

        } catch (Throwable e) {
            Log.e(TAG,"Load engine",e);
            error = e.getMessage();
            loadingState.postValue(LoadingState.Error);
        }
    }


    /**
     * Load the content of your config file (it should be in assets/config folder)
     * @param context - the application context
     * @return the file content as array of bytes
     * @throws IOException
     */
    private static byte[] loadConfigFile(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        String configFilename = "";
        String[] file_list;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        file_list = assetManager.list("config");

        for (String file : file_list) {
            if (file.endsWith(".ocr")) {
                configFilename = file;
                break;
            }
        }

        if (configFilename.isEmpty()) {
            throw new IOException("configuration file is not found");
        }

        final String configPath = "config" + File.separator + configFilename;

        InputStream is = assetManager.open(configPath);

        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        os.close();
        return os.toByteArray();
    }

    public static String engineDescription() {
        if (documents.isEmpty()) {
            try {
                documents = instance.Description();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            JSONObject engineJson;
            try {
                engineJson = new JSONObject(instance.Description());

                JSONArray targetGroupsArray = engineJson.getJSONArray("target_groups");
                for (int tg_index = 0; tg_index < targetGroupsArray.length(); tg_index++) {
                    JSONObject targetGroup = targetGroupsArray.getJSONObject(tg_index);
                    String mode = targetGroup.getString("target_group_type");
                    JSONArray masksArray = targetGroup.getJSONArray("target_masks");
                    for (int mask_index = 0; mask_index < masksArray.length(); mask_index++) {
                        String mask = masksArray.getString(mask_index);
                        documents2.add(mode + separator + mask);
                    }

                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return documents;

    }

    public static String getSessionTypes() {
        if (sessionTypes.isEmpty()) {
//            OCRStudioSDKInstance engine = instance;
//            JSONObject engineJson;
//            try {
//                engineJson = new JSONObject(engine.Description());
//                JSONArray sessionTypesArray = engineJson.getJSONArray("session_types");
//                for (int st_index = 0; st_index < sessionTypesArray.length(); st_index++) {
//                    String sessionType = sessionTypesArray.getString(st_index);
//                    sessionTypes.add(sessionType);
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
        }
        //return sessionTypes;
        return "";
    }

    public static String getFaceMatching(OCRStudioSDKImage imageA, OCRStudioSDKImage imageB) throws JSONException {
        Log.d(TAG,"getFaceMatching "+imageA+" "+imageB);
        JSONObject sessionParamsJson = new JSONObject();
        // 2.1 Set session_type
        // session_type: "face_matching" for face comparative
        sessionParamsJson.put("session_type", "face_matching");
        // 2.2 Set target_group_type
        sessionParamsJson.put("target_group_type", "default");
        String sessionParams = sessionParamsJson.toString();
        String status = "";
        String similarity_estimation = null;
        try {
            OCRStudioSDKInstance engine = instance;
            OCRStudioSDKSession session = engine.CreateSession(SettingsStore.signature, sessionParams);

            // PROCESS images
            session.ProcessImage(imageA);
            session.ProcessImage(imageB);

            // Read the result
            OCRStudioSDKResult result = session.CurrentResult();
            OCRStudioSDKTarget target = result.TargetByIndex(0);
            for (OCRStudioSDKItemIterator item_it = target.ItemsBegin("string"); !item_it.IsEqualTo(target.ItemsEnd("string")); item_it.Step()) {
                if (item_it.Item().Name().equals("status")) {
                    status = item_it.Item().Value();
                }
                if (item_it.Item().Name().equals("similarity_estimation")) {
                    similarity_estimation = item_it.Item().Value();
                }
            }
            // No similarity => error => return status (which describes the error)
            if(similarity_estimation==null) throw new Exception(status);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "Similarity: " + similarity_estimation;
    }

    public static void getResultFromFile(OCRStudioSDKImage imageData) {
        try {
            // 1. Get engine instance
            OCRStudioSDKInstance engine = Engine.getInstance();
            // 2. Create new session params
            JSONObject sessionParamsJson = new JSONObject();
            // 2.1 Set session_type
            // session_type: "document_recognition" for recognition by one frame
            sessionParamsJson.put("session_type", "document_recognition");
            // 2.2 Set target_group_type
            sessionParamsJson.put("target_group_type", SettingsStore.currentMode);
            // 2.3 Set target_masks
            sessionParamsJson.put("target_masks", new JSONArray(SettingsStore.currentMask));
            // 2.4 Set field_geometry to draw quads during recognition
            sessionParamsJson.put("output_modes", "field_geometry");

            // 2.5 Set custom options
            JSONObject optionsJson = new JSONObject();
            Map<String, String> map = SettingsStore.options;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                optionsJson.put(key, value);
            }

            String timeStamp = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
            optionsJson.put("currentDate", timeStamp);

            // Adding session options
            sessionParamsJson.put("options", optionsJson);
            String sessionParams = sessionParamsJson.toString();

            // 3. Spawn recognition session
            long startTime = System.nanoTime();

            OCRStudioSDKSession session = engine.CreateSession(SettingsStore.signature, sessionParams);

            // Dynamic activation
            // String dynKey = session.UnlockRequest().CStr();
            // String actKey = getActivationKey(dynKey);
            // session.Unlock(actKey);

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;  //divide by  to get milliseconds.
            Log.i(TAG,"create image session time: " + duration );

            session.ProcessImage(imageData);
            OCRStudioSDKResult result = session.CurrentResult();
            ResultStore.instance.addResult(result);

            // 4. Reset session
            session.Reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // Session activation
    public static String getActivationKey(String dynKey) {
        try {
            String apiKey = "";
            // http or https
            String actUrl = "";

            String data = "{\"action\": \"activate_id_session\",  \"message\": \"" + dynKey + "\" }";
            System.out.printf("data is " + data + "\n");
            URL requestUrl = new URL(actUrl);

            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setConnectTimeout(4 * 1000);
            connection.setReadTimeout(4 * 1000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("X-API-Key", apiKey);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", System.getProperty("http.agent"));
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            byte[] message = data.getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Length", "" + message.length);

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            out.write(data);
            out.flush();
            out.close();

            if (connection.getResponseCode() != 200) {

                String error;
                try (Scanner scanner = new Scanner(connection.getErrorStream())) {
                    String json = scanner.useDelimiter("\\A").next();
                    System.out.printf("json0 " + json + "\n");
                    JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                    error = object.getString("detail");
                }
                throw new RuntimeException(error);
            }

            InputStream response = connection.getInputStream();
            try (Scanner scanner = new Scanner(response)) {
                String json = scanner.useDelimiter("\\A").next();
                JSONObject object = (JSONObject) new JSONTokener(json).nextValue();
                return object.getString("message");
            }

        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Network activation: " + e.getMessage());
        }
    }


}

