/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

public class FaceImageError implements FaceImage{
    public final String error;

    public FaceImageError(String error){
        this.error = error;
    }
}
