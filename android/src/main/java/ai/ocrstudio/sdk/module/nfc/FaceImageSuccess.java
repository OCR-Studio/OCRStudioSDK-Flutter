/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

import android.graphics.Bitmap;

import ai.ocrstudio.sdk.OCRStudioSDKImage;

public class FaceImageSuccess implements FaceImage{
    public final OCRStudioSDKImage image;
    public final Bitmap         bitmap;

    public FaceImageSuccess(OCRStudioSDKImage image, Bitmap bitmap){
        this.image  = image;
        this.bitmap = bitmap;
    }
}
