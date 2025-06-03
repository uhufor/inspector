# UI Inspector

**UI Inspector** is a debugging overlay library for Android that allows developers to visually
inspect layout elements during development.

This app is inspired by the UI inspection (measurement) function of the now defunct **Window VQA** app.

It is intended for **debug builds only**, and a corresponding **no-op module** ensures no overhead
or code inclusion in release builds.

| Screenshot 1                            | Screenshot 2                            | Screenshot 3                            |
|:----------------------------------------| :-------------------------------------- |:----------------------------------------|
| <img src="./art/art_1.png" width="300"> | <img src="./art/art_2.png" width="300"> | <img src="./art/art_3.png" width="300"> |

---

## Features

### Standard Inspection Mode

- When enabled, all selectable UI elements are highlighted with outlines.
- Tapping an element displays:
    - The element’s **width and height**
    - Its **distance from the immediate parent view** (top, bottom, left, right)
    - All data is shown as an overlay on the screen.

### Relative Inspection Mode

- Long-pressing a selectable element activates relative inspection mode.
- The first selected element is tinted **red**, the second **blue**.
- The distance between the two elements is shown for each side (top, bottom, left, right).
    - Sides with **zero distance are omitted** from the display.

### View System Compatibility

- Supports both:
    - Traditional `XML`-based UI trees
    - `Jetpack Compose` UI trees

---

## Compose Constraints

Since **SemanticsNode** is used for UI element extraction in **Compose**, Nodes that do not generate Semantics during composable declaration are not displayed as selectable elements. In such cases, you need to ensure that SemanticsNode is generated as shown in the code below.

```kotlin
@Composable
fun Profile(profile: Profile, modifier: Modifier) {
  Box(
    modifier = Modifier
      .clearAndSetSemantics {} // or .semantics {}
      .size(100.dp)
      ..
  ) {
      Image(
        painter = painterResource(id = R.drawable.ic_profile_placeholder),
        contentDescription = "profile holder", // Option 1
        modifier = Modifier
          // or .clearAndSetSemantics {}       // Option 2
          // or .semantics {}                  // Option 3
          .fillMaxSize(),
        contentScale = ContentScale.Crop
      )
      ..
  }
}
```

---

## Permissions

This library requires the **"Draw over other apps"** permission in order to display the overlay UI.
Make sure to request and handle this permission appropriately in your app.

---

## Download

Add the following dependencies via Maven Central:

```kotlin
dependencies {
  debugImplementation("io.github.uhufor:inspector-runtime:latest.release")
  releaseImplementation("io.github.uhufor:inspector-noop:latest.release")
}
```

## Example

```kotlin
class InspectorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Installation required before use
        Inspector.install(this)
        // If you want to use the control floating button provided by default
        Inspector.showFloatingTrigger()
    }
}

// If you want to manipulate inspection manually
class InspectorActivity : Activity() {
    fun enableInspection() {
        Inspector.enableInspection()
    }

    fun disableInspection() {
        Inspector.disableInspection()
    }
}
```

## License

```
Copyright [2025] [uhufor (Hae Jung, Kim)]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
