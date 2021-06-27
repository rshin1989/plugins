#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'naver_maps_flutter'
  s.version          = '0.0.1'
  s.summary          = 'Naver Maps for Flutter'
  s.description      = <<-DESC
A Flutter plugin that provides a Naver Maps widget.
                       DESC
  s.homepage         = 'https://github.com/clearmaps/plugins'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'ClearMaps Dev Team' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'NMapsMap'
  s.static_framework = true
  s.platform = :ios, '9.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
  s.swift_version = '5.0'
end
