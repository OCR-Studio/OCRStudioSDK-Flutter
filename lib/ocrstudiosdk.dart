import 'dart:typed_data';
import 'platform_interface.dart';

class OCRStudioSDKModule {

  Future<String?> startRecognition() {
    return OCRStudioSDKModulePlatform.instance.startRecognition();
  }

  Future<String?> fileRecognition(Uint8List file) {
    return OCRStudioSDKModulePlatform.instance.fileRecognition(file);
  }

  Future init(String signature) {
    return OCRStudioSDKModulePlatform.instance.init(signature);
  }

  Future initSettings(settings) {
    return OCRStudioSDKModulePlatform.instance.initSettings(settings);
  }

  Future<String?> getVersion() {
    return OCRStudioSDKModulePlatform.instance.getVersion();
  }

  Future<List<String>?> getDocumentTypes() {
    return OCRStudioSDKModulePlatform.instance.getDocumentTypes();
  }

  Future myCustomMethodGetCroppedPhoto(Map<String, String> coords) {
    return OCRStudioSDKModulePlatform.instance
        .myCustomMethodGetCroppedPhoto(coords);
  }
  Future<String?> engineDescription() {
    return OCRStudioSDKModulePlatform.instance.engineDescription();
  }

  Future getFaceComparison(String facea, Uint8List faceb) {
    return OCRStudioSDKModulePlatform.instance.getFaceComparison(facea, faceb);
  }

  Future setLabelText(text) {
    return OCRStudioSDKModulePlatform.instance.setLabelText(text);
  }

}