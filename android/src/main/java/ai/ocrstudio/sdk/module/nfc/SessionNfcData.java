/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

import ai.ocrstudio.sdk.OCRStudioSDKImage;

/**
 * DATA FOR NFC VERIFICATION BY THE SDK
 */
public class SessionNfcData {
    public final String         mrz;
    public final OCRStudioSDKImage image;

    public SessionNfcData(String mrz, OCRStudioSDKImage image){
        this.mrz   = mrz;
        this.image = image;
    }
}
