/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

/**
 * Unsuccessful NFC-verification data
 */
public class SessionNfcResultError implements SessionNfcResult{
    public final String error;

    public SessionNfcResultError(String error){
        this.error = error;
    }

}
