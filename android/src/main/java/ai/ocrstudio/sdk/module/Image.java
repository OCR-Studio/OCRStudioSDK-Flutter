/*
  Copyright (c) 2024-2026, OCR Studio
  All rights reserved.
*/

package ai.ocrstudio.sdk.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

import ai.ocrstudio.sdk.OCRStudioSDKImage;
import ai.ocrstudio.sdk.OCRStudioSDKPixelFormat;
import ai.ocrstudio.sdk.OCRStudioSDKYUVFormat;

public class Image {

    // Image from base64 string
    public static OCRStudioSDKImage createFromBase64(String imageObject) {
        return OCRStudioSDKImage.CreateFromBase64FileBuffer(imageObject);
    }

    // Image from Android Bitmap
    public static OCRStudioSDKImage createImageFromBitmap(Bitmap imageObject) {
        // int stride = imageObject.getRowBytes();
        int width = imageObject.getWidth();
        int height =imageObject.getHeight();

        int bytesPerPixel = 4; // Assuming ARGB_8888 format, where each pixel occupies 4 bytes (8 bits per channel)

        int bytesPerLine = width * bytesPerPixel; // Stride
        int bytesPerChannel = bytesPerPixel / 4; // Each channel (A, R, G, B) has 8 bits

        int bufferSize = bytesPerLine * height;
        byte[] byteArray = new byte[bufferSize];

        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        imageObject.copyPixelsToBuffer(byteBuffer);

        return OCRStudioSDKImage.CreateFromPixelBuffer(byteArray, width, height, bytesPerLine, bytesPerChannel, OCRStudioSDKPixelFormat.OCRSTUDIOSDK_PIXEL_FORMAT_RGBA);
    }

    // Image from CameraX ImageProxy
    public static OCRStudioSDKImage createFromYUV_420_888(ImageProxy imageObject, int width, int height ) {

            // Example for YUV_FORMAT_420_888
            // According to our tests RGBA_8888 has ~45ms overhead per frame (tested on Helio G90T)
            // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
            ImageProxy.PlaneProxy planeY = imageObject.getPlanes()[0];
            ImageProxy.PlaneProxy planeU = imageObject.getPlanes()[1];
            ImageProxy.PlaneProxy planeV = imageObject.getPlanes()[2];

            return OCRStudioSDKImage.CreateFromYUV(
                    getByteArrayFromByteBuffer(planeY.getBuffer(), planeY.getRowStride(), height),
                    planeY.getRowStride(),
                    planeY.getPixelStride(),
                    getByteArrayFromByteBuffer(planeU.getBuffer(), planeU.getRowStride(), height),
                    planeU.getRowStride(), planeU.getPixelStride(),
                    getByteArrayFromByteBuffer(planeV.getBuffer(), planeV.getRowStride(), height),
                    planeV.getRowStride(), planeV.getPixelStride(),
                    width,
                    height,
                    OCRStudioSDKYUVFormat.OCRSTUDIOSDK_YUV_FORMAT_420_888);

    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer, int rowStride, int height) {
        // getBuffer() - The stride after the last row may not be mapped into the buffer.
        //  This is why we always calculate the byteBuffer offset.
        //  https://developer.android.com/reference/android/media/Image.Plane#getBuffer()

        int bufferSize = byteBuffer.remaining();
        // The byte array size is stride * height (the leftover spaces will be filled with 0 bytes)
        byte[] bytesArray = new byte[height * rowStride];
        byteBuffer.get(bytesArray, 0, bufferSize);
        return bytesArray;
    }

    public static OCRStudioSDKImage createFromJPG(@NonNull ImageProxy image) {

        // If image.getFormat() return JPG
        // Compressed data. Row and pixel strides are 0.
        // https://developer.android.com/reference/android/media/Image#getFormat()

        ByteBuffer buffer = image.getPlanes()[0].getBuffer(); // jpg
        byte[] frame_bytes = new byte[buffer.remaining()];
        buffer.get(frame_bytes);
        OCRStudioSDKImage frame = OCRStudioSDKImage.CreateFromFileBuffer(frame_bytes);
        //String base64_after = frame.GetBase64String().GetCStr();
        return frame;
    }

    // Flutter - ImagePicker
    public static OCRStudioSDKImage createFromByteArray(@NonNull byte[] bytes) {
        return OCRStudioSDKImage.CreateFromFileBuffer(bytes);
    }

    private static Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.rewind();
        return byteBuffer.array();
    }

}
