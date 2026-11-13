/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module.nfc;

import ai.ocrstudio.sdk.OCRStudioSDKImage;

import org.jmrtd.lds.icao.MRZInfo;
import java.util.List;

public class PassportData {
    public final MRZInfo           mrzInfo;
    public final List<FaceImage>   faceImages;

    public PassportData(MRZInfo mrzInfo, List<FaceImage> faceImages){
        this.mrzInfo    = mrzInfo;
        this.faceImages = faceImages;
    }

    public OCRStudioSDKImage firstImage(){
        if(faceImages.isEmpty()) return null;
        FaceImage faceImage = faceImages.get(0);
        if(faceImage instanceof FaceImageSuccess)
            return ((FaceImageSuccess) faceImage).image;
        return null;
    }
}
