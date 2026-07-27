# Fix Dependency Resolution Conflicts

The build is failing due to a version conflict between `androidx.test.espresso:espresso-core:3.5.1` (requested by the project) and version `3.5.0` (required by Compose UI testing libraries). Additionally, a hardcoded alpha version of Material 3 is causing inconsistencies.

## Proposed Changes

### [app](file:///C:/Users/zain4/GitProjects/Habitz/app)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/zain4/GitProjects/Habitz/app/build.gradle.kts)
- Remove the hardcoded `androidx.compose.material3:material3-android:1.5.0-alpha01` dependency. This dependency is redundant when using the Compose BOM and is likely the source of the transitive version conflicts.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/zain4/GitProjects/Habitz/gradle/libs.versions.toml)
- Downgrade `espressoCore` from `3.5.1` to `3.5.0` to match the version required by the Compose testing framework.
- Remove redundant version overrides for Compose libraries (`ui-tooling`, `ui-util`, etc.) to let the Compose BOM manage them consistently.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the APK can be built successfully.
- Run `./gradlew :app:connectedDebugAndroidTest` (optional, if a device is available) to ensure tests still pass.

### Manual Verification
- N/A (Build fix).
