/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

/**
 * Successful NFC-verification data
 */
public class SessionNfcResultSuccess implements SessionNfcResult{
    public final String dataCheck;
    public final String faceCheck;

    public SessionNfcResultSuccess(String dataCheck, String faceCheck){
        this.dataCheck = dataCheck;
        this.faceCheck = faceCheck;
    }
}
