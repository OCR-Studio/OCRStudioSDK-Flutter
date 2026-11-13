//import 'events.dart';
import 'dart:convert';
import 'dart:math';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ocrstudiosdk/ocrstudiosdk.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';

import 'jsonClasses/engine_result.dart';
import 'jsonClasses/engine_description.dart';

part 'screen_example.dart';
part 'drop_down_button_mask_list.dart';

String signature = "INSERT_SIGNATURE_FROM_README.html\";
late String mask;
late String mode;

late final Future _initEngineFuture;

bool _showLoader = false;

final OCRSTUDIOSDK = OCRStudioSDKModule();

ValueNotifier<Items> resultData =
    ValueNotifier(Items(image: {}, string: {}, template: {}));

void main() {
  runApp(
    const MaterialApp(home: FirstScreen()),
  );
}

class FirstScreen extends StatefulWidget {
  const FirstScreen({super.key});

  @override
  State<FirstScreen> createState() => _FirstScreenState();
}

class _FirstScreenState extends State<FirstScreen> {
  String _info = '';

  // event from native
  final eventEmitter = const EventChannel('eventStream');

  final ImagePicker picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    initPlatformState();
    // Set listener for _onFrameUpdate event
    eventEmitter.receiveBroadcastStream().listen(_onFrameUpdate);
  }

  Future<void> initPlatformState() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.

    try {
      // 1. Init engine.
      // It is important to wait for a response before any engine call!
      debugPrint("1. Init engine");
      _initEngineFuture = OCRSTUDIOSDK.init(signature);
    } on PlatformException catch (e) {
      debugPrint(e.toString());
    } catch (err) {
      // other types of Exceptions
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  String _getCurrentDate() {
    // dd.mm.yyyy
    final now = DateTime.now();
    var year = now.year;
    var month = now.month.toString().padLeft(2, '0');
    var day = now.day.toString().padLeft(2, '0');

    return [day, month, year].join('.');
  }

  void _onFrameUpdate(e) async {
    // ignore empty requests
    if (e == null) {
      return;
    }

    if (e.containsKey("frameIndex")) {
      Random random = Random();
      int randomNumber = random.nextInt(8); // from 0 upto 99 included

      var a =
          ['😉', '👺', '😡', '😱', '👮🏻‍♂️', '🚀', '🚢', '🚩'][randomNumber];
      await OCRSTUDIOSDK.setLabelText('Data from DART: ${e['frameIndex']}  $a');
    }
  }

  Future _scan() async {
    try {

      // check permissions 
      const permission = Permission.camera;
      if (await permission.isDenied) {
        await permission.request();
      }

      // setSignature must be called first due iOS module implementation.
      Map<String, Object> settings = {
        'mode': mode,
        'mask': [mask],
        // 'fraudAttemtEnabled': 'true',
        'options': {'sessionTimeout': '5.0', 'extractTemplateImages': 'true'},
      };

      // 2. Set settings
      debugPrint("2. Set settings...");
      await OCRSTUDIOSDK.initSettings(settings);
      await OCRSTUDIOSDK.setLabelText("Please scan document");

      // reset data store
      resultData.value = (Items(image: {}, string: {}, template: {}));

      // 3. Waiting result
      debugPrint("3. Waiting result...");
      String? data = await OCRSTUDIOSDK.startRecognition();

      // is canceled?
      if (data == null) {
        setState(() {
          _info = "Canceled";
        });
        return;
      }

      final map = jsonDecode(data) as Map<String, dynamic>;
      print(map);
      final response = EngineResult.fromJson(map);

      setState(() {
        for (final target in response.targets) {
          final Map? el = target.items.template as Map?;

          if (el != null && el.isNotEmpty) {
            _info = el.keys.first;
            resultData.value = target.items;
          }
          else {
            _info = 'Not found';
          }
        }
      });
    } on Exception catch (e) {
      setState(() {
        _info = e.toString();
      });
      e.toString();
    }
  }

  Future _upload({bool photo = false}) async {
    try {
      final XFile? image = await picker.pickImage(
          source: photo ? ImageSource.gallery : ImageSource.camera);

      if (image == null) {
        setState(() {
          resultData.value = Items(image: {}, string: {}, template: {});
          _info = "Canceled";
        });
        return;
      }

      setState(() {
        _info = "";
        _showLoader = true;
      });

      debugPrint("Prepare image...");
      final Uint8List bytes = await image.readAsBytes();

      Map se = {
        'mode': mode,
        'mask': [mask],
        'options': {'sessionTimeout': '5.0', 'extractTemplateImages': 'true'},
      };

      // 2. Set settings
      debugPrint("2. Set settings...");
      await OCRSTUDIOSDK.initSettings(se);

      // reset data store
      resultData.value = Items(image: {}, string: {}, template: {});

      // 3. Waiting result...
      debugPrint("3. Waiting result...");
      String? data = await OCRSTUDIOSDK.fileRecognition(bytes);

      // is canceled?
      if (data == null) {
        setState(() {
          _info = "Canceled";
          _showLoader = false;
        });
        return;
      }

      final map = jsonDecode(data) as Map<String, dynamic>;

      final response = EngineResult.fromJson(map);

      setState(() {
        _showLoader = false;
        for (final target in response.targets) {
          final Map? el = target.items.template as Map?;

          if (el != null && el.isNotEmpty) {
            _info = el.keys.first;
            resultData.value = target.items;
          }
          else {
            _info = 'Not found';
          }
        }
      });

      //  resultData.value = data;
    } on Exception catch (e) {
      setState(() {
        _showLoader = false;
        debugPrint(e.toString());
        _info = e.toString();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<void>(
      future: _initEngineFuture,
      builder: (BuildContext context, AsyncSnapshot<void> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(body: Center(child: Text('Loading engine...')));
          //CircularProgressIndicator();
        } else {
          return Scaffold(
              appBar: AppBar(
                title: Row(children: [
                  Expanded(
                      child: Text(
                    _info,
                    textAlign: TextAlign.center,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 15.0,
                    ),
                    maxLines: 150,
                  )),
                ]),
              ),
              body: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  ValueListenableBuilder(
                    builder: (_, response, __) {
                      if (response.template.isNotEmpty) {
                        // result.containsKey("docType")
                        return EngineScreenExample(result: response);
                      } else {
                        return Column(
                          children: [
                            _showLoader
                                ? const Center(
                                    child: CircularProgressIndicator())
                                : const CircularProgressIndicator(value: 0.0),
                            const Center(
                                child: Text('Select mask and press "Scan"'))
                          ],
                        );
                      }
                    },
                    valueListenable: resultData,
                    // The child parameter is most helpful if the child is
                    // expensive to build and does not depend on the value from
                    // the notifier.
                    // child: goodJob,
                  ),
                ],
              ),
              bottomNavigationBar: Container(
                  //padding: const EdgeInsets.fromLTRB(5, 5, 5, 0),
                  height: 120,
                  color: const Color.fromARGB(255, 233, 233, 233),
                  child: Column(
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(
                              flex: 1,
                              child: Container(
                                padding:
                                    const EdgeInsets.fromLTRB(20, 5, 20, 8),
                                child: Column(children: [
                                  const Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Expanded(
                                        flex: 1,
                                        child: DropdownButtonMaskList(),
                                      ),
                                    ],
                                  ),
                                  Row(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Expanded(
                                        flex: 1,
                                        child: Container(
                                          padding: const EdgeInsets.symmetric(
                                              horizontal: 5),
                                          child: ElevatedButton(
                                            child: const Text('Scan'),
                                            onPressed: () => _scan(),
                                          ),
                                        ),
                                      ),
                                      Expanded(
                                        flex: 1,
                                        child: Container(
                                          padding: const EdgeInsets.symmetric(
                                              horizontal: 5),
                                          child: ElevatedButton(
                                            child: const Text('Gallery'),
                                            onPressed: () =>
                                                _upload(photo: true),
                                          ),
                                        ),
                                      ),
                                    ],
                                  ),
                                ]),
                              ))
                        ],
                      ),
                      Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            IntrinsicHeight(
                              child: Center(
                                child: FutureBuilder<String?>(
                                    future: OCRSTUDIOSDK.getVersion(),
                                    builder: (BuildContext context,
                                        AsyncSnapshot snapshot) {
                                      if (snapshot.connectionState ==
                                          ConnectionState.done) {
                                        return snapshot.hasError
                                            ? Text('${snapshot.error}')
                                            : Text("Version: ${snapshot.data}");
                                      } else {
                                        return const CircularProgressIndicator();
                                      }
                                    }),
                              ),
                            )
                          ]),
                    ],
                  )));
        }
      },
    );
  }
}
