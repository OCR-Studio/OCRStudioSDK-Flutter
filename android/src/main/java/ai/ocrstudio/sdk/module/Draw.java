/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import ai.ocrstudio.sdk.OCRStudioSDKDelegate;
import ai.ocrstudio.sdk.R;
import ai.ocrstudio.sdk.OCRStudioSDKItemIterator;
import ai.ocrstudio.sdk.OCRStudioSDKResult;
import ai.ocrstudio.sdk.OCRStudioSDKTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A custom view to show frames received from CameraSession
 */
public class Draw extends View  {
  private static final String TAG = "Draw";

  public static float scale = 1f;
  public static float translate_x = 0;
  public static float translate_y = 0;

  class QuadStorage {
    private final float[] points = new float[16];
    private final int color = R.color.detected;
    private final Paint paint = new Paint();

    QuadStorage(double[][] quad) {

      points[0] = (float) quad[0][0] * scale;
      points[1] = (float) quad[0][1] * scale;
      points[2] = (float) quad[1][0] * scale;
      points[3] = (float) quad[1][1] * scale;
      points[4] = (float) quad[1][0] * scale;
      points[5] = (float) quad[1][1] * scale;
      points[6] = (float) quad[2][0] * scale;
      points[7] = (float) quad[2][1] * scale;
      points[8] = (float) quad[2][0] * scale;
      points[9] = (float) quad[2][1] * scale;
      points[10] = (float) quad[3][0] * scale;
      points[11] = (float) quad[3][1] * scale;
      points[12] = (float) quad[3][0] * scale;
      points[13] = (float) quad[3][1] * scale;
      points[14] = (float) quad[0][0] * scale;
      points[15] = (float) quad[0][1] * scale;

      paint.setColor(getResources().getColor(color));
      paint.setStrokeWidth(4);
    }

    public float[] getPoints() {
      return points;
    }

    public Paint getPaint() {return paint;}
  }

  private final Paint paint = new Paint();

  private final int historyLength = 4;
  private final List<Set<QuadStorage>> quads = new LinkedList<>();

  public Draw(Context context) {
    super(context);
    initView();
  }

  public Draw(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  private void initView() {
    paint.setColor(Color.WHITE);
    paint.setStrokeWidth(3);
    paint.setAntiAlias(true);
  }

  public void showMatching(OCRStudioSDKResult result) {
    Set<QuadStorage> qs = new HashSet<>();

    for (int target_i = 0; target_i < result.TargetsCount(); ++target_i) {
      OCRStudioSDKTarget target = result.TargetByIndex(target_i);

      try {
        JSONObject targetJson = new JSONObject(target.Description());
        JSONArray itemTypesArray = targetJson.getJSONArray("item_types");
        for (int item_i = 0; item_i < itemTypesArray.length(); item_i++) {
          String itemType = itemTypesArray.getString(item_i);

          // Visualization of template
          if (itemType.equals("template")) {
            for (OCRStudioSDKItemIterator item_it = target.ItemsBegin(itemType); !item_it.IsEqualTo(target.ItemsEnd(itemType)); item_it.Step()) {

              String value = item_it.Item().Value();
              double[][] array = stringToArray(value);
              if (array != null) {
                qs.add(new QuadStorage(array));
              }
            }
          }

          // Visualization of fields
          if (itemType.equals("string")) {
            for (OCRStudioSDKItemIterator item_it = target.ItemsBegin(itemType); !item_it.IsEqualTo(target.ItemsEnd(itemType)); item_it.Step()) {

              String description = item_it.Item().Description();
              JSONObject fieldDescrJson;
              try {
                fieldDescrJson = new JSONObject(description);
                if (fieldDescrJson.has("field_geometry")) {
                  JSONObject fieldGeometry = fieldDescrJson.getJSONObject("field_geometry");
                  if (fieldGeometry.has("line_quads")) {
                    JSONArray line_quads = fieldGeometry.getJSONArray("line_quads");
                    for (int i = 0; i < line_quads.length(); i++) {
                      double[][] array = stringToArray(String.valueOf(line_quads.getJSONArray(i)));
                        assert array != null;
                        if (array.length != 0) {
                        qs.add(new QuadStorage(array));
                      }
                    }
                  }
                  if (fieldGeometry.has("quad")) {
                    JSONArray quad = fieldGeometry.getJSONArray("quad");
                    double[][] array = stringToArray(String.valueOf(quad));
                    if (array != null) {
                      qs.add(new QuadStorage(array));
                    }
                  }
                }
              } catch (JSONException e) {
                e.printStackTrace();
              }
            }
          }
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    if (quads.size() == historyLength) {
      quads.remove(0);
    }

    quads.add(qs);
    invalidate();
  }

  public void showMatching(JSONObject feedBackJson) {

    Set<QuadStorage> qs = new HashSet<>();

      try {

        if (feedBackJson.getString("type").equals("detection_result")) {
          JSONArray quad = feedBackJson.getJSONObject("data").getJSONArray("quad");
          double[][] array = stringToArray(String.valueOf(quad));
          if (array != null) {
            qs.add(new QuadStorage(array));
          }
        }
        if (feedBackJson.getString("type").equals("segmentation_result")) {
          JSONArray quads = feedBackJson.getJSONObject("data").getJSONArray("quads");
          for (int i = 0; i < quads.length(); i++) {
            double[][] array = stringToArray(String.valueOf(quads.getJSONArray(i)));
              assert array != null;
              if (array.length != 0) {
              qs.add(new QuadStorage(array));
            }
          }
        }
      } catch (JSONException e) {
        Log.e(TAG,"drawing quads",e);
      }


    if (quads.size() == historyLength) {
      quads.remove(0);
    }

    quads.add(qs);
    invalidate();
  }

  private double[][] stringToArray(String value) {
    try {
      JSONArray jsonValue = new JSONArray(value);
      double[][] array = new double[jsonValue.length()][];
      for (int i = 0; i < jsonValue.length(); i++) {
        JSONArray innerArray = jsonValue.getJSONArray(i);
        double[] innerValues = new double[innerArray.length()];
        for (int j = 0; j < innerArray.length(); j++) {
          innerValues[j] = innerArray.getDouble(j);
        }
        array[i] = innerValues;
      }
      return array;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void cleanUp() {
    quads.clear();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.translate(translate_x, translate_y);

    try {

      int nq = quads.size();
      for (int i = 0; i < nq; i++) {
        for (QuadStorage q : quads.get(i)) { // NPE sometimes
          int currentAlpha = q.getPaint().getAlpha();
          if (currentAlpha > 0) {
            int newAlpha = Math.max(currentAlpha - 20, 0); // Decrease alpha by 20
            q.getPaint().setAlpha(newAlpha);
            canvas.drawLines(q.getPoints(), q.getPaint());
          }
        }
      }

    // quads.get(i) - NPE sometimes
    } catch (Exception e) {
      Log.e(TAG, "onDraw", e);
    }

  }

}

