import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'platform_interface.dart';

/// An implementation of [OCRStudioSDKModulePlatform] that uses method channels.
class MethodChannelOCRStudioSDKModule extends OCRStudioSDKModulePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('ocrstudiosdk');

  // https://itnext.io/flutter-methodchannel-dart-generic-and-type-casting-54ca48e6d3ad
  // Why  generic type info are not supported

  // Before calling any method, we must initialize the engine.
  // This is a heavy operation and it takes time.

  @override
  Future<String?> startRecognition() async {
    var result = await methodChannel.invokeMethod('startRecognition');
    // Generic type info are not supported
    return result;
  }

  @override
  Future<String?> fileRecognition(file) async {
    var result = await methodChannel.invokeMethod('fileRecognition', {'file': file});
    // Generic type info are not supported
    return result;
  }

  @override
  Future init(signature) async {
    await methodChannel.invokeMethod<String>('init', {'signature': signature});
  }

  @override
  Future initSettings(settings) async {
    await methodChannel
        .invokeMethod<String>('initSettings', {'settings': settings});
  }

  @override
  Future<String?> engineDescription() async {
    final docTypes = await methodChannel.invokeMethod('engineDescription');
    return docTypes;
  }

  @override
  Future<String> getVersion() async {
    final version = await methodChannel.invokeMethod('getVersion');
    return version;
  }

  @override
  Future<List<String>?> getDocumentTypes() async {
    final docTypes = await methodChannel.invokeMethod('getDocTypes');
    return List<String>.from(docTypes);
  }

  @override
  Future<double?> getFaceComparison(facea, faceb) async {
    final fc = await methodChannel
        .invokeMethod('getFaceComparison', {'a': facea, 'b': faceb});
    return fc;
  }

  @override
  Future myCustomMethodGetCroppedPhoto(coords) async {
    await methodChannel
        .invokeMethod<Map>('myCustomMethodGetCroppedPhoto', {'param': coords});
  }

  @override
  Future setLabelText(text) async {
    await methodChannel.invokeMethod<String>('setLabelText', {'param': text});
  }

}
