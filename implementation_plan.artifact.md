# App Rename Implementation Plan: Habitz to Forge

Rename the application from "Habitz" to "Forge" throughout the project, including display name, package name, class names, and themes.

## User Review Required

> [!WARNING]
> This is a major refactor that changes the `applicationId`. If you have already deployed the "Habitz" app to a device or Play Store, the "Forge" app will be treated as a **different app**, and data from the old app will not be automatically accessible.

## Proposed Changes

### 1. Visual Branding & Metadata

#### [MODIFY] [strings.xml](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/res/values/strings.xml)
- Change `app_name` from "Habitz" to "Forge".

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/zain4/GitProjects/Habitz/app/build.gradle.kts)
- Change `namespace` to `com.example.forge`.
- Change `applicationId` to `com.example.forge`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/AndroidManifest.xml)
- Update `android:name` and theme references.

---

### 2. Code Refactoring (Package & Classes)

#### [MODIFY] All Source Files
- Update `package com.example.habitz...` to `package com.example.forge...`.
- Update all imports referencing `com.example.habitz`.

#### [RENAME] Classes
- `HabitzApplication` -> `ForgeApplication`
- `HabitzTheme` -> `ForgeTheme`
- `HabitzDatabase` -> `ForgeDatabase`
- `HabitzApp` -> `ForgeApp` (in MainActivity)

#### [MOVE] Directory Structure
- Move files from `app/src/main/java/com/example/habitz` to `app/src/main/java/com/example/forge`.
- Move files from `app/src/androidTest/java/com/example/habitz` to `app/src/androidTest/java/com/example/forge`.
- Move files from `app/src/test/java/com/example/habitz` to `app/src/test/java/com/example/forge`.

---

### 3. Database & Resources

#### [MODIFY] [HabitzDatabase.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/HabitzDatabase.kt) (to be renamed)
- Change `DATABASE_NAME` from "habitz_db" to "forge_db".

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure the project compiles with the new package name.
- Run existing unit tests to ensure no logic was broken.

### Manual Verification
- Deploy to a device/emulator and verify:
    - Launcher icon label is "Forge".
    - App runs without crashing.
    - Data persistence works with the new database name.
