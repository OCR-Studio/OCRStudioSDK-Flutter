/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Store for Engines settings
public class SettingsStore {

    public static Map<String, String> options = new HashMap<>();
    public static String currentMode = "default";
    public static ArrayList<String> currentMask = new ArrayList<>();
    public static String signature = null;

    // We use ArrayList due react-native supported data structure
    public static void SetMask(ArrayList<String> mask) {
        currentMask = mask;
    }

    public static void SetMode(String mode) {
        currentMode = mode;
    }

    public static void SetSignature(String sign) {
        signature = sign;
    }

    public static void SetOptions(Map<String, String> map) {
        options = map;
    }

    // For Flutter and react or any other cases we can return result in jsob
    public static Boolean isJsonResult = false;
    public static Boolean isRoi = false;
    public static Boolean isStoppersDisabled = false;

}
