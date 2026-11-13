import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'method_channel.dart';

abstract class OCRStudioSDKModulePlatform extends PlatformInterface {
  /// Constructs a OCRStudioSDKModulePlatform.
  OCRStudioSDKModulePlatform() : super(token: _token);

  static final Object _token = Object();

  static OCRStudioSDKModulePlatform _instance = MethodChannelOCRStudioSDKModule();

  /// The default instance of [OCRStudioSDKModulePlatform] to use.
  ///
  /// Defaults to [MethodChannelOCRStudioSDKModule].
  static OCRStudioSDKModulePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [OCRStudioSDKModulePlatform] when
  /// they register themselves.
  static set instance(OCRStudioSDKModulePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  // https://itnext.io/flutter-methodchannel-dart-generic-and-type-casting-54ca48e6d3ad
  // Why  generic type info are not supported

  Future<String?> startRecognition() {
    throw UnimplementedError('startRecognition() has not been implemented.');
  }

  Future<String?> fileRecognition(file) {
    throw UnimplementedError('fileRecognition() has not been implemented.');
  }

  Future init(signature) {
    throw UnimplementedError('init() has not been implemented.');
  }

  Future initSettings(settings) {
    throw UnimplementedError('initSettings() has not been implemented.');
  }

  Future<List<String>?> getDocumentTypes() {
    throw UnimplementedError('getDocumentTypes() has not been implemented.');
  }

  Future<String?> engineDescription() {
    throw UnimplementedError('engineDescription() has not been implemented.');
  }

  Future<String?> getVersion() {
    throw UnimplementedError('getVersion() has not been implemented.');
  }

  Future<double?> getFaceComparison(facea, faceb) async {
    throw UnimplementedError('getFaceComparison() has not been implemented.');
  }

  Future myCustomMethodGetCroppedPhoto(Map<String, String> coords) {
    throw UnimplementedError(
        'myCustomMethodGetCroppedPhoto() has not been implemented.');
  }

  Future setLabelText(String text) {
    throw UnimplementedError('setLabelText() has not been implemented.');
  }

}
