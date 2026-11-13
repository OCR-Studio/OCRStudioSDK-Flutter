#import "OCRStudioSDKPlugin.h"

#import "OCRStudioSDKInstance.h"
#import "OCRStudioSDKViewController.h"

@interface OCRStudioSDKPlugin () <OCRStudioSDKViewControllerDelegate, OCRStudioSDKInitializationDelegate, FlutterStreamHandler>
@property(strong, nonatomic) OCRStudioSDKInstance *engineInstance;
@property(strong, nonatomic) OCRStudioSDKViewController *ocrViewController;
@property(atomic) FlutterEventSink eventSink;
@property(atomic) FlutterResult response_resolver;
@end

@implementation OCRStudioSDKPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  OCRStudioSDKPlugin* instance = [[OCRStudioSDKPlugin alloc] init];
  
  // Method channel setup
  FlutterMethodChannel* channel = [FlutterMethodChannel methodChannelWithName:@"ocrstudiosdk"
                                                              binaryMessenger:[registrar messenger]];
  [registrar addMethodCallDelegate:instance channel:channel];
  
  // Create event channel
  FlutterEventChannel* eventChannel = [FlutterEventChannel eventChannelWithName:@"eventStream" binaryMessenger:[registrar messenger]];
  // Register event channel
  [eventChannel setStreamHandler:instance];
}

// for event stream
- (FlutterError*)onListenWithArguments:(id)arguments
                             eventSink:(FlutterEventSink)eventSink {
    _eventSink = eventSink;
    return nil;
}

// for event stream
- (FlutterError*)onCancelWithArguments:(id)arguments {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    _eventSink = nil;
    return nil;
}



// ----------------------- MethodCall -----------------------

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  //  __weak typeof(self) weakSelf = self;
  NSString *method = call.method;
  
  if ([@"init" isEqualToString:method]) {
    [self init:call.arguments[@"signature"] result:result];
    return;
  }
  
  if ([@"initSettings" isEqualToString:method]) {
    [self initSettings:call result:result];
    return;
  }
  
  if ([@"startRecognition" isEqualToString:method]) {
    [self startRecognition:result];
    return;
  }
  
  if ([@"fileRecognition" isEqualToString:method]) {
    [self fileRecognition:call.arguments[@"file"] withResult:result];
    return;
  }
  
  if ([@"engineDescription" isEqualToString:method]) {
    NSLog(@">>>>>>>>>>>> getDocTypes <<<<<<<<<<<<");
    [self engineDescription:result];
    return;
  }
  
  if ([@"getVersion" isEqualToString:method]) {
    [self getVersion:result];
    return;
  }
  
  if ([@"setLabelText" isEqualToString:method]) {
    [self setLabelText:call result:result];
    return;
  }
  
  result(FlutterMethodNotImplemented);
}

/*
 * ========================================================================
 * ========================== Core methods ================================
 * ========================================================================
 */

- (void)startRecognition:(FlutterResult)result {
  if (!self.engineInstance.engineInitialized) {
    result([FlutterError errorWithCode:@"NOT_INITIALIZED" message:@"Engine is not initialized" details:nil]);
    return;
  }
  
  self.response_resolver = result;
  
  dispatch_async(dispatch_get_main_queue(),^{
    
    UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:self.ocrViewController];
    navigationController.modalPresentationStyle = UIModalPresentationFullScreen;
    navigationController.navigationBar.hidden = YES;
    
    // Get active scene
    UIWindowScene *windowScene = (UIWindowScene *)[UIApplication sharedApplication].connectedScenes.anyObject;
    UIWindow *window = windowScene.keyWindow;
    
    // Find root controller
    UIViewController *topVC = window.rootViewController;
    while (topVC.presentedViewController) {
      topVC = topVC.presentedViewController;
    }
    
    if (topVC) {
      [topVC presentViewController:navigationController animated:YES completion:nil];
    } else {
      self.response_resolver([FlutterError errorWithCode:@"VC_NOT_FOUND"
                                                 message:@"Active view controller not found"
                                                 details:nil]);
      self.response_resolver = nil;
    }
  });
}


#pragma mark - file recogntion
- (void)fileRecognition:(FlutterStandardTypedData *) file withResult:(FlutterResult)result {
  
  if (!self.engineInstance.engineInitialized) {
      result([FlutterError errorWithCode:@"NOT_INITIALIZED" message:@"Engine is not initialized" details:nil]);
      return;
  }
  
  self.response_resolver = result;
  
  // Set session type
  [self.ocrViewController.sessionParams setSessionType:(@"document_recognition")];
  
  NSData *fileData = [file data];
  UIImage* image = [UIImage imageWithData:fileData];
  
  // NSString* debugimg = [[image getRef] exportBase64JPEG];
  
  int rotationRules[] = {0, 2, 3, 1, 0, 2, 1, 3};
  OBJCOCRStudioSDKResult* imageResult = [self.engineInstance processSingleImageFromUIImage:image rotations:rotationRules[image.imageOrientation]];
  NSString* json_result = [imageResult.getRef serialize];

  result(json_result);
}

- (void)init:(NSString*)signature result:(FlutterResult)result {
  if (!self.engineInstance.engineInitialized) {
    self.engineInstance = [[OCRStudioSDKInstance alloc] initWithSignature:signature];
    [self.engineInstance initializeEngine:[self pathForSingleDataArchive]];
    
    self.ocrViewController = [[OCRStudioSDKViewController alloc] init];
    self.ocrViewController.ocrDelegate = self;
    self.engineInstance.initializationDelegate = self;
    
    [self.ocrViewController attachEngineInstance:self.engineInstance];
    [self.ocrViewController setCaptureButtonDelegate:self.ocrViewController];
    
    self.ocrViewController.displayDocumentQuadrangle = YES;
    self.ocrViewController.displayZonesQuadrangles = YES;
    self.ocrViewController.displayProcessingFeedback = YES;
  }
  result(nil);
}

- (void)initSettings:(FlutterMethodCall*)call result:(FlutterResult)result {
  
  NSLog(@">>>>>>>>>>>> initSettings <<<<<<<<<<<<");
  if (!self.engineInstance.engineInitialized) {
    result([FlutterError errorWithCode:@"NOT_INITIALIZED" message:@"Engine is not initialized" details:nil]);
    return;
  }
  
  NSDictionary *settings = call.arguments[@"settings"];
  NSString *mode = settings[@"mode"];
  NSArray *mask = settings[@"mask"];
  NSDictionary *options = settings[@"options"];
  
  [self.ocrViewController.sessionParams setTargetGroupType:(mode)];
  [self.ocrViewController.sessionParams clearTargetMasks];
  [self.ocrViewController.sessionParams setTargetMasks:(mask)];
  [self.ocrViewController.sessionParams clearOptions];
  
  for (id key in options) {
    [self.ocrViewController.sessionParams setOptionWithName:key to:[options objectForKey:key]];
  }
  result(nil);
}

- (void)setLabelText:(FlutterMethodCall*)call result:(FlutterResult)result {
  NSString *text = call.arguments[@"param"];
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.ocrViewController configureDocumentTypeLabel:text];
    result(nil);
  });
}

- (void) getVersion:(FlutterResult)result{
  result([OBJCOCRStudioSDKInstance libraryVersion]);
}

#pragma mark - engine description

- (void) engineDescription:(FlutterResult)result {
  
  if (![self.engineInstance engineInitialized]) {
    NSException* exc = [NSException
                        exceptionWithName:@"EngineInstanceError"
                        reason:@"engineDescription cannot call while engine is not yet initialized"
                        userInfo:nil];
    @throw exc;
  }
  
  @try {
    NSString *desc = [[self.engineInstance engine] description];
    
    result(desc);
  } @catch (NSException *e) {
    result([FlutterError errorWithCode:@"UNAVAILABLE" message:@"getVersion" details:nil]);
  }
}

#pragma mark - result callback
NSInteger frameCounter = 0;
- (void)ocrViewControllerDidRecognize:(OBJCOCRStudioSDKResult *)result
                           fromBuffer:(CMSampleBufferRef)buffer {
  
  NSLog(@">>>>>>>>>>>> RESULT <<<<<<<<<<<<");
  frameCounter++;
  
  /* Send to UI preliminary results. We send number of frames, for example. */
  NSMutableDictionary *dictField = [[NSMutableDictionary alloc] init];
  [dictField setValue:@(frameCounter) forKey:@"frameIndex"];
  
  // Send event to dart on each frame
  _eventSink(dictField);
  // NSLog(@">>>>>>>>>>>> dictField <<<<<<<<<<<<");
  
  // If is not in terminal condition, engines wait for more frames for recognition
  if (![[result getRef] allTargetsFinal]) {
    return;
  }
  
  // dismiss view controller
  [self.ocrViewController dismissViewControllerAnimated:YES completion:nil];
  
  frameCounter = 0;
  
  NSString* json_result = [[result getRef] serialize];
  _response_resolver(json_result);
}


// Cancel button event
- (void) ocrViewControllerDidCancel {
  // dismiss view controller
  [self.ocrViewController dismissViewControllerAnimated:YES completion:nil];
  if (self.response_resolver) {
    self.response_resolver(nil);
  }
}

- (NSString *)pathForSingleDataArchive {
  
  NSBundle *config = [NSBundle bundleForClass:[self classForCoder]];
  NSString *dataPath = [config pathForResource:@"ocrresources.bundle" ofType:nil];
  NSArray *listdir =
  [[NSFileManager defaultManager] contentsOfDirectoryAtPath:dataPath
                                                      error:nil];
  NSPredicate *zipFilter =
  [NSPredicate predicateWithFormat:@"self ENDSWITH '.ocr'"];
  NSArray *zipArchives = [listdir filteredArrayUsingPredicate:zipFilter];
  NSAssert(zipArchives.count == 1,
           @"data folder must contain single .ocr archive");
  NSString *zipName = [zipArchives objectAtIndex:0];
  NSString *zipPath = [dataPath stringByAppendingPathComponent:zipName];
  return zipPath;
}

@end
