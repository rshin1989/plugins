#import "KakaoMapsFlutterPlugin.h"
#if __has_include(<kakao_maps_flutter/kakao_maps_flutter-Swift.h>)
#import <kakao_maps_flutter/kakao_maps_flutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "kakao_maps_flutter-Swift.h"
#endif

@implementation KakaoMapsFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftKakaoMapsFlutterPlugin registerWithRegistrar:registrar];
}
@end
