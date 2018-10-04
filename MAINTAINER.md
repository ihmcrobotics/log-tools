# Maintainer Guide

### Making a release

1. Increment version numbers in `build.gradle.kts` and `buildSrc/build.gradle.kts`
1. In `$ log-tools`, run:
   ```
   > gradle publish -PpublishUrl=ihmcRelease
   ```
1. In `$ log-tools/buildSrc`, run:
   ```
   > gradle publishPlugins
   ```
1. Publish Bintray artifacts: [https://bintray.com/ihmcrobotics/maven-release/log-tools](https://bintray.com/ihmcrobotics/maven-release/log-tools)
1. Tag commit with `:bookmark: X.X.X`
1. Document release notes at [https://github.com/ihmcrobotics/log-tools/releases](https://github.com/ihmcrobotics/log-tools/releases)
