# 🚀 OCRStudioSDK Flutter plugin

## 1. How to run the example

1. In `example/lib/main.dart` fill value:

```dart
String signature = 'SET SIGNATURE HERE\'
```

2. In root folder run `flutter pub get` to install all dependencies. In example folder run `flutter pub get` too.
   For ios: run `pod install` in the `example/ios` folder.
3. Use `flutter run`  in example folder to test examples or
a) open on Android studio  `example/android/` folder and run app.
b) open `example/ios/Runner.xcworkspace` in xCode and run app.


---

## 2. Editing sources

**For Android:** open `example/android` folder in your IDE. Sources of module: `/android/src/main`
**For iOS:** open `example/ios/*.xcworkspace` project. Find in Pods -> Development pods -> find `Classes/*.m`
**Dart:**  example app `/example/lib/main.dart`. Module dart files  in `/lib/`.

---

## 3. Install the pub to your project

Set local path to module in your pubspec.yaml

```yaml
dependencies:
  ocrstudio:
    path: ./
```
---

## 4. JSON example

The structure of the returned recognition object from native platforms.

`items.image` - object containing the image data.
`items.string` - object containing all recognized fields.
`items.template` - object. If empty, a document is not found.

```json
{
    "isAllTargetsFinal": false,
    "description": {
        "target_type": "document",
        "specific_type": "are.passport.type1",
        "specific_type_has_rfid": 1,
        "specific_type_display_name": "United Arab Emirates Passport",
        "item_types": [
            "image",
            "string",
            "template"
        ],
        "attributes": {

        }
    },
    "isFinal": false,
    "items": {
        "image": {
            "photo": {
                "type": "image",
                "name": "photo",
                "value": "/UBFRDVP..//",
                "confidence": 0.993137,
                "accepted": true,
                "attributes": {
                    "best_photo_frame_idx": "0",
                    "processed_photos_number": "1"
                },
                "field_geometry": {

                }
            },
            ...
        },
        "string": {
            "type": {
                "type": "string",
                "name": "type",
                "value": "P",
                "confidence": 0.99983,
                "accepted": true,
                "attributes": {
                    "mono_score": "-1.5"
                },
                "field_geometry": {
                    "quad": [
                        [
                            315.0,
                            88.0
                        ],
                        [
                            357.0,
                            88.0
                        ],
                        [
                            356.0,
                            121.0
                        ],
                        [
                            314.0,
                            122.0
                        ]
                    ]
                }
            }
            ...
        },
        "template": {
            "are.passport.type1:main": {
                "type": "template",
                "name": "are.passport.type1:main",
                "value": [
                    [
                        -11.225542,
                        -0.38539
                    ],
                    [
                        1037.421509,
                        -2.85152
                    ],
                    [
                        1024.562866,
                        723.667908
                    ],
                    [
                        -22.655428,
                        741.494446
                    ]
                ],
                "confidence": 0.91016,
                "accepted": true,
                "attributes": {
                    "DPI": "209",
                    "fully_presented": "false",
                    "n90CCWrotations": "0"
                }
            }
            ...
        }
    }
}

```

---

## 5. How to update libraries to a new version

If you have received a new pub package, you can update the binaries this way:

### For android

1. Replace all folders (`arm64-v8a`,`armeabi-v7a` etc) in `/android/src/main/jniLibs/`
2. Replace interface `*.jar` in `/android/src/main/libs/`
3. Replace `*.ocr` bundle `/android/src/main/assets/config/`

### For iOS

1. Replace 2 folders `ios/OCRStudioSDK`, `ios/OCRStudioSDKCore`
2. Run `pod install` in `example/ios`
