/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.tech.IsoDep;
import android.util.Base64;
import android.util.Log;

//  JMRTD lib
import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.iso19794.FaceImageInfo;

// SCUBA lib
import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import ai.ocrstudio.sdk.OCRStudioSDKImage;


public class PassportReader {
    private static final String TAG = "myapp.PassportReader";


    /**
     * READ PASSPORT NFC-DATA
     * The process can take a long time
     * @param isoDep
     * @param passportKey
     * @return
     */
    public PassportData readPassportData(
        IsoDep isoDep,
        PassportKey passportKey
    ) throws CardServiceException, IOException {
        BACKeySpec bacKey = new BACKey(passportKey.passportNumber, passportKey.birthDate, passportKey.expirationDate);
        isoDep.setTimeout(10000); // timeout in ms
        CardService cardService = CardService.getInstance(isoDep);
        cardService.open();
        PassportService service = new PassportService(
                cardService,
                PassportService.NORMAL_MAX_TRANCEIVE_LENGTH,
                PassportService.DEFAULT_MAX_BLOCKSIZE,
                false,
                false
        );
        service.open();

        boolean paseSucceed = false; // PASE doesn't work
        service.sendSelectApplet(paseSucceed);
        if(!paseSucceed){
            service.doBAC(bacKey);
            Log.w(TAG,"doBAC "+bacKey);
        }

        // READ DATA
        CardFileInputStream dg1In = service.getInputStream(PassportService.EF_DG1);
        DG1File dg1File = new DG1File(dg1In);
        Log.w(TAG,"dg1File "+dg1File);

        CardFileInputStream dg2In = service.getInputStream(PassportService.EF_DG2);
        DG2File dg2File = new DG2File(dg2In);
        Log.w(TAG,"dg2File "+dg2File);

        // DECODE FACE IMAGES
        ArrayList<FaceImageInfo> faceImageInfos = new ArrayList<>();
        dg2File.getFaceInfos().forEach(faceInfo -> {
            faceImageInfos.addAll(
                faceInfo.getFaceImageInfos()
            );
        });
        ArrayList<FaceImage> faceImages = new ArrayList<>();
        faceImageInfos.forEach(faceImageInfo -> {
            faceImages.add(
                decodeFaceImage(faceImageInfo)
            );
        });

        return new PassportData(
            dg1File.getMRZInfo(),
            faceImages
        );
    }

    private FaceImage decodeFaceImage(FaceImageInfo info){
        try{
            // Read data to buffer
            DataInputStream dataInputStream = new DataInputStream(info.getImageInputStream());
            int length = info.getImageLength();
            byte[] buffer = new byte[length];
            dataInputStream.readFully(buffer,0,length);

            // DECODE image by SDK
            Log.d(TAG, "decodeImage, mime: "+info.getMimeType());
            OCRStudioSDKImage sdkImage = OCRStudioSDKImage.CreateFromFileBuffer(buffer);

            // GET bitmap from the sdk image
            String base64String = sdkImage.ExportBase64JPEG().CStr();
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            // Return successful result
            FaceImageSuccess image = new FaceImageSuccess( sdkImage, bitmap );
            Log.w(TAG, "FACE IMAGE DECODED, bitmap: "+image.bitmap.getWidth()+"x"+image.bitmap.getHeight());
            return image;
        }catch (Exception e){
            Log.e(TAG, "FACE IMAGE DECODE ERROR",e);
            return new FaceImageError(e.getMessage());
        }
    }
}
