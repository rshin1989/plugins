#import "NaverMapsFlutterPlugin.h"
#if __has_include(<naver_maps_flutter/naver_maps_flutter-Swift.h>)
#import <naver_maps_flutter/naver_maps_flutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "naver_maps_flutter-Swift.h"
#endif

@implementation NaverMapsFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNaverMapsFlutterPlugin registerWithRegistrar:registrar];
}
@end
