/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

import android.app.PendingIntent;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;



// android.nfc.NfcAdapter extension
// Handles NFC data receiving
// independent on the passport data format
public class NfcAdapterExt {
    private static final String TAG = "myapp.NfcAdapterExt";

    // ENABLE/DISABLE NFC INTENT RECEIVING
    // instead of intent filter in manifest
    // returns false if NFC is not supported by device!
    public static boolean enableNfcReceiving(Activity activity, boolean enable){
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if(adapter==null) return false; // nfc not supported
        if(enable){
            // ENABLE
            Log.w(TAG, "enableNfcReceiving");
            Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE);
            String[][] filter = {{"android.nfc.tech.IsoDep"}};
            adapter.enableForegroundDispatch(activity, pendingIntent, null, filter);
        }else{
            // DISABLE
            Log.w(TAG, "disableNfcReceiving");
            adapter.disableForegroundDispatch(activity);
        }
        return true; // nfc supported
    }

    // Read passport tag from intent
    public static Tag getPassportTag(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            try {
                Tag tag = intent.getExtras().getParcelable(NfcAdapter.EXTRA_TAG);
                String[] techList = tag.getTechList();
                for (String str: techList ) {
                    if(str.equals("android.nfc.tech.IsoDep")) return tag;
                }
            }catch(Exception ex){
                return null;
            }
        }
        return null;
    }


}
