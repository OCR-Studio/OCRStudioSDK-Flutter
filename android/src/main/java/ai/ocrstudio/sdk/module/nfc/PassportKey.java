/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

/**
 * AN EXTERNAL DATA REQUIRED FOR PASSPORT NFC-DATA PARSING
 */
public class PassportKey {
    public final String passportNumber;
    public final String expirationDate;
    public final String birthDate;

    public PassportKey(
        String passportNumber,
        String expirationDate,
        String birthDate
    ){
        this.passportNumber = passportNumber;
        this.expirationDate = expirationDate;
        this.birthDate      = birthDate;
    }

    /**
     * Convert date to YYMMDD from DD.MM.YYYY
     * @param date
     * @return
     */
    public static String dateFrom_DD_MM_YYYY(String date){
        String[] parts = date.split("\\.");
        return parts[2].substring(2) + parts[1] + parts[0];
    }
}
