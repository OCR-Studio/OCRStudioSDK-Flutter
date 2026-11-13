/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import androidx.annotation.NonNull;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKItem;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKString;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton which keeps the result data when switching between activities
 */
public class ResultStore {
    private static final String TAG = "myapp.ResultStore";

    public static final ResultStore instance = new ResultStore();
    @NonNull
    public static String jsonResponse = "";
    public static JSONArray itemsCollection;

    public String getResult() {
        return jsonResponse;
    }

    public void addResult(OCRStudioSDKResult result) {

        if (SettingsStore.isJsonResult) {
            jsonResponse = "";
            jsonResponse = result.Serialize().CStr();
        }
//            List<String> itemsType = new ArrayList<>();
//            itemsCollection = new JSONArray();
//
//
//            for (int i = 0; i < result.TargetsCount(); ++i) {
//                OCRStudioSDKTarget target = result.TargetByIndex(i);
//
//                JSONObject collection = new JSONObject();
//
//                try {
//                    JSONObject targetJson = new JSONObject(target.Description());
//
//                    // Put meta to new item for collection
//                    collection.put("meta", targetJson);
//                    //String specific_type = targetJson.getString("specific_type");
//                    JSONArray itemTypesArray = targetJson.getJSONArray("item_types");
//                    for (int item_i = 0; item_i < itemTypesArray.length(); item_i++) {
//                        itemsType.add(itemTypesArray.getString(item_i));
//                    }
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//
//                JSONArray items = new JSONArray();
//
//                for (String itemType : itemsType) {
//
//                    for (OCRStudioSDKItemIterator item_it = target.ItemsBegin(itemType); !item_it.IsEqualTo(target.ItemsEnd(itemType)); item_it.Step()) {
//
//                        OCRStudioSDKItem item = item_it.Item();
//
//                        try {
//                            if (itemType.equals("image")) {
//                                JSONObject el = new JSONObject(item.Description());
//                                OCRStudioSDKImage image = item.Image();
//                                // Put image string or save to local system and put uri
//                                String base64 = image.ExportBase64JPEG().CStr();
//                                el.put("value", base64);
//                                // JSONObject tempItem = new JSONObject();
//                                // tempItem.put("name", item.Name());
//                                items.put(el);
//                            }
//
//                            if (itemType.equals("string")) {
//                                JSONObject desc = new JSONObject(item.Description());
//                                items.put(desc);
//                            }
//
//                            if (itemType.equals("template")) {
//                            }
//
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//
//                    try {
//                        // Put new item to collection
//                        collection.put("items", items);
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                // Put item to group
//                itemsCollection.put(collection);
//
//            }
        }



    //public Map<String, FieldInfo>   getFields() {     return fields;  }
    //public Map<String, FieldInfo>   getImages() { return images; }
    //public String                   getType()   { return docType; }

    // Decode base64 and save to local folder. JSON file will become much lighter.
    // React Native passes the json file from the native part with the images encoded
    // in base64 for quite a long time. Flutter doesn't have this problem.
    // Visually it works faster with base64 images. Tested on Mediatek Helio G95
}
