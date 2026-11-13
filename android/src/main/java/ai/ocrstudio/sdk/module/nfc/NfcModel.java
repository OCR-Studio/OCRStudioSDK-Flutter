/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.module.CameraSession;

/**
 * NFC-LOGIC IMPLEMENTATION
 * TO USE AS A PART OF A VIEWMODEL
 */
public class NfcModel {
    private final String TAG = "myapp.NfcModel";

    //----------------------------------------------------------------------------------------------
    // DATA
    public MutableLiveData<NfcState> nfcState = new MutableLiveData<>(NfcState.Disabled);
    public boolean isNfcEnabled(){return nfcState.getValue()==NfcState.Waiting || nfcState.getValue()==NfcState.Reading;};
    public String nfcError=null;
    // Output data
    public PassportData     passportData = null;
    public SessionNfcResult verificationResult = null;
    Thread nfcThread=null;

    //----------------------------------------------------------------------------------------------
    // INITIALIZATION
    /** CONSTRUCTOR  */
    public NfcModel(){
        Log.d(TAG, "constructor");

    }
    /** DESTRUCTOR
     * must be called in ViewModel.onCleared() */
    public void onCleared() {
        Log.d(TAG, "destructor onCleared");
        if(nfcThread!=null)
            nfcThread.interrupt();
    }

    //----------------------------------------------------------------------------------------------
    // ACTIONS
    public void onResumeActivity(Activity activity){
        // ENABLE/DISABLE NFC INTENT RECEIVING
        if(!NfcAdapterExt.enableNfcReceiving(activity, isNfcEnabled()))
            nfcState.setValue(NfcState.NotSupported);
    }

    public void activate(Activity activity){
        if(nfcState.getValue()==NfcState.Disabled) {
            nfcState.setValue(NfcState.Waiting);
            NfcAdapterExt.enableNfcReceiving(activity, true);
        }
    }
    public void close(Activity activity){
        if(nfcThread!=null){
            nfcState.setValue(NfcState.Stopping);
            nfcThread.interrupt();
        }else{
            nfcState.setValue(NfcState.Disabled);
            NfcAdapterExt.enableNfcReceiving(activity, true);
        }
    }

    public void onNewIntent(Intent intent, PassportKey passportKey){
        if(isNfcEnabled()){
            Tag passportTag = NfcAdapterExt.getPassportTag(intent);
            if(passportTag!=null)
                onPassportTagDetected(passportTag, passportKey);
        }
    }

    private void onPassportTagDetected(Tag tag, PassportKey passportKey){
        Log.d(TAG, "onPassportTagDetected");
        if(isNfcEnabled() && nfcThread==null){
            nfcThread = new Thread(new NfcProcessingThread(
                    IsoDep.get(tag),
                    passportKey
            ));
            nfcThread.start();
        }
    }

    //----------------------------------------------------------------------------------------------
    private class NfcProcessingThread implements Runnable{
        final IsoDep isoDep;
        final PassportKey passportKey;

        public NfcProcessingThread(IsoDep isoDep, PassportKey passportKey){
            this.isoDep = isoDep;
            this.passportKey = passportKey;
        }

        @Override
        public void run() {
            Log.e(TAG,"--- NFC thread started");
            try {
                // READ DATA
                nfcState.postValue(NfcState.Reading);
                if(passportKey==null) throw new Exception("PassportKey is null");
                PassportReader passportReader = new PassportReader();
                passportData = passportReader.readPassportData(
                        isoDep,
                        passportKey);
                Log.w(TAG,"NFC READING SUCCESS");

                // VERIFY DATA by the engine
                verificationResult = null;
                OCRStudioSDKImage photo = passportData.firstImage();
                if(photo!=null) {
                    nfcState.postValue(NfcState.Checking);
                    //Thread.sleep(1500);
                    verificationResult = CameraSession.instance.processNfc(
                        new SessionNfcData(
                            passportData.mrzInfo.toString().replace("\n", ""),
                            photo
                        )
                    );
                }

                // Update state
                nfcState.postValue(NfcState.Success);
            } catch (Exception e) {
                Log.e(TAG,"NfcProcessingThread", e);
                // Update state
                if(nfcState.getValue()==NfcState.Stopping){
                    nfcState.postValue(NfcState.Disabled);
                }else {
                    nfcError = e.getMessage();
                    nfcState.postValue(NfcState.Error);
                }
            }
            Log.e(TAG,"--- NFC thread finished");
            nfcThread = null;
        }
    }

}
