# Lab: build the Corpus Náhuatl–Español welcome screen

This lab walks you through implementing a shared Compose Multiplatform screen for Android phones, Android tablets, iPhones, and iPads. You will add the code yourself. Creating this document does not change your application, Gradle configuration, or image assets.

## Learning goals

By the end, you will understand how to:

- Share a UI and its resources through `commonMain`.
- Separate application composition, theme, and feature presentation.
- Adapt a layout to the available window width.
- Expose user actions through callbacks without implementing authentication.
- Add subtle animations while keeping Material button behavior.
- Check the screen on phones, tablets, and smaller multitasking windows.

## 1. Understand the project before editing

Project root:

```text
/home/isra/Projects/master/nahuatl-corpus-mobile
```

All file paths below are exact paths relative to that root.

The inspected project has two Gradle modules:

| Module/location | Responsibility |
| --- | --- |
| `androidApp` | Android application and `MainActivity` |
| `shared` | Shared Kotlin code, Compose UI, and platform implementations |
| `iosApp` | Xcode project and SwiftUI host for the shared UI |

`MainActivity.kt` calls `App()`. On iOS, `ContentView.swift` hosts `MainViewController()`, which also calls `App()`. Changing the shared `App()` therefore changes both applications.

The source sets configured or present are:

- `commonMain`: shared application code and resources.
- `androidMain`: Android-specific implementations.
- `iosMain`: shared Apple-specific implementations, used by `iosArm64` and `iosSimulatorArm64`.
- `commonTest`, `androidHostTest`, and `iosTest`: existing sample tests. The shared Gradle configuration also enables Android device tests.

The project already has feature packages such as `feature/home/presentation`. Their screen files and the navigation files are placeholders. We will implement the existing `HomeScreen.kt` rather than introduce another module or navigation library.

### Existing versions and dependencies

These values come from `gradle/libs.versions.toml`; they are not proposed upgrades:

| Tool/library | Declared version |
| --- | --- |
| Kotlin | `2.4.20` |
| Compose Multiplatform | `1.12.1` |
| Material 3 | `1.12.0-alpha03` |
| Android Gradle Plugin | `9.4.1` |
| AndroidX Activity | `1.13.0` |
| Multiplatform Lifecycle | `2.11.0` |
| Gradle wrapper | `9.6.0` |
| Android SDK | compile/target `37`, minimum `24` |

`shared/build.gradle.kts` already declares Compose runtime, foundation, UI, Material 3, resources, previews, and lifecycle in `commonMain`. Its existing `App.kt` already uses Compose animation APIs. `androidApp` declares Activity Compose and tooling.

**No new dependency is needed. Leave all Gradle files and the version catalog unchanged.** We will use the APIs already available through this configuration. There is no need for navigation, image-loading, authentication, or database libraries.

The Xcode project already targets iPhone and iPad (`TARGETED_DEVICE_FAMILY = "1,2"`) with deployment target `18.2`.

### Checkpoint

Open the existing `App.kt`, `HomeScreen.kt`, and both platform entry points. Trace how Android and iOS reach the same composable. This is the central reason the screen belongs in `commonMain`.

## 2. Plan the screen

Your reference image contains green mountains and an emblem, rather than a complete screen layout. We will preserve that artwork and surround it with a cream background, green typography, a filled primary button, and an outlined secondary button.

- Narrow windows: artwork above title, subtitle, and buttons.
- Available width of at least `840.dp`: artwork and welcome content side by side.
- Artwork width capped at `480.dp`; narrow text/button column capped at `400.dp`; overall content capped at `1120.dp`.
- Content scrolls if the window is short or accessibility text makes it taller.
- Safe-area padding keeps content away from system bars and cutouts.

The breakpoint is a design choice for this composition. We measure the window, not a device name, so tablet split-screen and iPad multitasking adapt naturally.

## 3. Add the original artwork

**New binary file:**

```text
shared/src/commonMain/composeResources/drawable/corpus_header.png
```

Source file:

```text
/home/isra/Downloads/Stylized Green Mountains and Emblem.png
```

The complete contents of the new file are the original PNG, copied unchanged. There is no text code to paste for a binary image.

Run these commands manually from the project root, or copy the image with your file manager:

```bash
mkdir -p shared/src/commonMain/composeResources/drawable
cp "/home/isra/Downloads/Stylized Green Mountains and Emblem.png" \
  shared/src/commonMain/composeResources/drawable/corpus_header.png  
```

The source is a `1521 × 1034` RGBA PNG. The layout will use that aspect ratio and `ContentScale.Fit` to preserve the entire image.

### What you are learning

`composeResources` packages assets for both platforms. Compose generates a drawable accessor from the filename:

```kotlin
Res.drawable.corpus_header
```

Your existing resource package is `nahuatlcorpus.shared.generated.resources`. Do not edit generated files under `shared/build`.

Reference: [Compose Multiplatform resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html).

## 4. Add shared strings

**New file:**

```text
shared/src/commonMain/composeResources/values/strings.xml
```

Complete contents:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="welcome_title">Corpus Náhuatl–Español</string>
    <string name="welcome_subtitle">Recopilación y validación de datos lingüísticos</string>
    <string name="welcome_sign_in">Iniciar sesión</string>
    <string name="welcome_create_account">Crear cuenta</string>
</resources>
```

Create the `values` folder if it does not exist.

### What you are learning

Shared string resources keep visible text out of layout code. Both platforms use the same strings, and you can add localized resource directories later. This file is separate from Android's existing `androidApp/src/main/res/values/strings.xml`.

`stringResource(Res.string.welcome_title)` reads a generated shared string accessor inside a composable.

### Checkpoint

Check the spelling and accents. The title uses an en dash between Náhuatl and Español. A misspelled resource name produces an unresolved accessor import later.

## 5. Create a shared theme

**New file:**

```text
shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/ui/theme/CorpusTheme.kt
```

Complete contents:

```kotlin
package mx.edu.orizaba.nahuatlcorpus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CorpusColors = lightColorScheme(
    primary = Color(0xFF245C3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4EEDC),
    onPrimaryContainer = Color(0xFF183D29),
    secondary = Color(0xFF52674D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EEDD),
    onSecondaryContainer = Color(0xFF263B24),
    background = Color(0xFFFAF9F3),
    onBackground = Color(0xFF20362A),
    surface = Color(0xFFFAF9F3),
    onSurface = Color(0xFF20362A),
    surfaceVariant = Color(0xFFE8EBDD),
    onSurfaceVariant = Color(0xFF526052),
    outline = Color(0xFF71836C),
    outlineVariant = Color(0xFFD2DCCB),
)

@Composable
fun CorpusTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CorpusColors,
        typography = Typography(),
        content = content,
    )
}
```

### What you are learning

- `@Composable` marks a function that participates in Compose UI composition.
- `content: @Composable () -> Unit` lets the theme wrap another UI.
- Material color roles separate meaning from literal colors. Buttons read `primary`; text reads `onBackground` or `onSurfaceVariant`.
- `Typography()` uses the existing Material typography and system fonts, so no font dependency or asset is needed.
- The palette is intentionally light to match this design. A dark palette can be added as a separate design task later.

Keep reusable visual decisions in the theme rather than repeating color values in each screen.

## 6. Implement the welcome screen

**Modify existing file:**

```text
shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/feature/home/presentation/HomeScreen.kt
```

Replace its contents with this complete code:

```kotlin
package mx.edu.orizaba.nahuatlcorpus.feature.home.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.orizaba.nahuatlcorpus.ui.theme.CorpusTheme
import nahuatlcorpus.shared.generated.resources.Res
import nahuatlcorpus.shared.generated.resources.corpus_header
import nahuatlcorpus.shared.generated.resources.welcome_create_account
import nahuatlcorpus.shared.generated.resources.welcome_sign_in
import nahuatlcorpus.shared.generated.resources.welcome_subtitle
import nahuatlcorpus.shared.generated.resources.welcome_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    animateEntrance: Boolean = true,
) {
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        entered = true
    }

    val entranceProgress = animateFloatAsState(
        targetValue = if (entered || !animateEntrance) 1f else 0f,
        animationSpec = tween(
            durationMillis = 420,
            easing = FastOutSlowInEasing,
        ),
        label = "Welcome entrance",
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            val useWideLayout = maxWidth >= 840.dp
            val minimumContentHeight = maxHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 1120.dp)
                        .fillMaxWidth()
                        .heightIn(min = minimumContentHeight)
                        .graphicsLayer {
                            alpha = entranceProgress.value
                            translationY =
                                (1f - entranceProgress.value) * 16.dp.toPx()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (useWideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(48.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HeaderArtwork(
                                modifier = Modifier.weight(1f),
                            )

                            WelcomeContent(
                                onSignInClick = onSignInClick,
                                onCreateAccountClick = onCreateAccountClick,
                                centered = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        HeaderArtwork(
                            modifier = Modifier
                                .widthIn(max = 480.dp)
                                .fillMaxWidth(),
                        )

                        Spacer(Modifier.height(32.dp))

                        WelcomeContent(
                            onSignInClick = onSignInClick,
                            onCreateAccountClick = onCreateAccountClick,
                            centered = true,
                            modifier = Modifier
                                .widthIn(max = 400.dp)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderArtwork(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Image(
                painter = painterResource(Res.drawable.corpus_header),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .aspectRatio(1521f / 1034f),
            )
        }
    }
}

@Composable
private fun WelcomeContent(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    centered: Boolean,
    modifier: Modifier = Modifier,
) {
    val textAlignment = if (centered) TextAlign.Center else TextAlign.Start

    Column(
        modifier = modifier,
        horizontalAlignment = if (centered) {
            Alignment.CenterHorizontally
        } else {
            Alignment.Start
        },
    ) {
        Text(
            text = stringResource(Res.string.welcome_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = textAlignment,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlignment,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))

        WelcomeButton(
            text = stringResource(Res.string.welcome_sign_in),
            onClick = onSignInClick,
            primary = true,
        )

        Spacer(Modifier.height(12.dp))

        WelcomeButton(
            text = stringResource(Res.string.welcome_create_account),
            onClick = onCreateAccountClick,
            primary = false,
        )
    }
}

@Composable
private fun WelcomeButton(
    text: String,
    onClick: () -> Unit,
    primary: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val pressScale = animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "Welcome button press",
    )

    val buttonModifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .graphicsLayer {
            scaleX = pressScale.value
            scaleY = pressScale.value
        }

    val shape = RoundedCornerShape(16.dp)
    val contentPadding = PaddingValues(
        horizontal = 24.dp,
        vertical = 16.dp,
    )

    if (primary) {
        Button(
            onClick = onClick,
            modifier = buttonModifier,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            interactionSource = interactionSource,
            shape = shape,
            contentPadding = contentPadding,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(
    name = "Welcome phone",
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun HomeScreenPhonePreview() {
    CorpusTheme {
        HomeScreen(
            onSignInClick = {},
            onCreateAccountClick = {},
            animateEntrance = false,
        )
    }
}

@Preview(
    name = "Welcome tablet",
    widthDp = 1024,
    heightDp = 768,
)
@Composable
private fun HomeScreenTabletPreview() {
    CorpusTheme {
        HomeScreen(
            onSignInClick = {},
            onCreateAccountClick = {},
            animateEntrance = false,
        )
    }
}
```

### Learn the screen in smaller pieces

#### A. Callbacks separate appearance from behavior

`onSignInClick: () -> Unit` means “a function with no arguments and no result.” The screen invokes it when the user taps the sign-in button. Its caller decides what should happen.

The callbacks are required on `HomeScreen`, making its actions explicit. No authentication or navigation object enters the UI. A ViewModel is unnecessary because this screen has no business state or asynchronous data to manage.

#### B. Remembered state survives recomposition

Compose may call a composable again when state changes. `remember` retains the entrance flag and interaction source across those calls.

`LaunchedEffect(Unit)` sets the entrance target after the screen enters composition. It does not rerun on every recomposition. If the screen is removed and later recreated, its entrance can run again.

`animateFloatAsState` moves the progress from `0f` to `1f`. The graphics layer uses it for opacity and a small upward translation. Reading the animation value inside the layer block lets Compose update the layer without rebuilding the entire screen for each animation frame.

#### C. Constraints determine the layout

`BoxWithConstraints` exposes `maxWidth` and `maxHeight` after safe-area and outer padding. We capture the viewport height before entering the scroll container, whose content can be taller than that viewport.

The inner column has a minimum height equal to the viewport. Short content is centered; taller content expands and can scroll. There are no vertical weights inside this scrolling column.

In the wide `Row`, two `weight(1f)` children share the available horizontal space. These weights are valid because the row has bounded width.

#### D. Modifier order affects measurement

`widthIn(max = 480.dp).fillMaxWidth()` first limits the maximum width, then fills that limited space. On a phone, the available width remains the tighter constraint.

`heightIn(min = 56.dp)` gives buttons a minimum height while allowing them to grow with larger text. A fixed height could clip accessibility text.

#### E. Material buttons retain interaction behavior

`Button` and `OutlinedButton` provide click semantics and Material interaction feedback. Their interaction source reports whether they are pressed, which also drives the small `0.985f` scale.

We do not add a second `clickable` modifier or custom pointer gesture handler. The actual click still belongs to the Material button, and the action runs only through its `onClick` callback.

References: [Compose animation guide](https://developer.android.com/develop/ui/compose/animation/quick-guide), [interaction handling](https://developer.android.com/develop/ui/compose/touch-input/user-interactions/handling-interactions).

#### F. Accessibility is part of the layout

- The title is marked as a heading for accessibility services.
- The image is decorative, so its description is `null`; the nearby text already identifies the app.
- Text can wrap, and buttons can grow vertically.
- Safe drawing insets protect content from system UI.
- Material buttons expose button semantics to TalkBack and VoiceOver.

#### G. Previews are separate from runtime animation

The preview functions supply empty actions and disable the entrance. That makes static previews immediately visible. The real app uses the default entrance behavior.

### Checkpoint

Explain why this screen does not import `android.*`, UIKit, or a navigation controller. Then locate the single expression that decides between phone-style and wide layouts.

## 7. Connect the screen to the app

**Modify existing file:**

```text
shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/App.kt
```

Replace the template contents with:

```kotlin
package mx.edu.orizaba.nahuatlcorpus

import androidx.compose.runtime.Composable
import mx.edu.orizaba.nahuatlcorpus.feature.home.presentation.HomeScreen
import mx.edu.orizaba.nahuatlcorpus.ui.theme.CorpusTheme

@Composable
fun App(
    onSignInClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {},
) {
    CorpusTheme {
        HomeScreen(
            onSignInClick = onSignInClick,
            onCreateAccountClick = onCreateAccountClick,
        )
    }
}
```

### What you are learning

`App` composes the theme and the feature screen. Its default empty callbacks let the existing Android and iOS hosts keep calling `App()`.

Both buttons intentionally do nothing beyond interaction feedback for now. Later, a shared navigation layer can supply the real callbacks here. Leave `AppNavigation.kt` and `AppDestination.kt` unchanged during this lab.

No edits are needed in `MainActivity.kt`, `MainViewController.kt`, or `ContentView.swift`. Android already enables edge-to-edge display, and the iOS host already uses `.ignoresSafeArea()`; the shared screen applies its own safe drawing padding.

## 8. Review the resulting structure

Only the marked application files should be added or modified when you complete this lab:

```text
nahuatl-corpus-mobile/
├── LAB_FIRST_SCREEN.md                         this lab
├── androidApp/
│   ├── build.gradle.kts                        unchanged
│   └── src/main/kotlin/mx/edu/orizaba/nahuatlcorpus/
│       └── MainActivity.kt                    unchanged
├── gradle/
│   └── libs.versions.toml                     unchanged
├── iosApp/
│   └── iosApp/
│       └── ContentView.swift                  unchanged
└── shared/
    ├── build.gradle.kts                       unchanged
    └── src/
        ├── commonMain/
        │   ├── composeResources/
        │   │   ├── drawable/
        │   │   │   ├── compose-multiplatform.xml existing, unused
        │   │   │   └── corpus_header.png       NEW
        │   │   └── values/
        │   │       └── strings.xml             NEW
        │   └── kotlin/mx/edu/orizaba/nahuatlcorpus/
        │       ├── App.kt                      MODIFIED
        │       ├── feature/home/presentation/
        │       │   └── HomeScreen.kt           MODIFIED
        │       ├── navigation/
        │       │   ├── AppDestination.kt       unchanged
        │       │   └── AppNavigation.kt        unchanged
        │       └── ui/theme/
        │           └── CorpusTheme.kt          NEW
        └── iosMain/kotlin/mx/edu/orizaba/nahuatlcorpus/
            └── MainViewController.kt          unchanged
```

The existing other feature files and sample greeting/test files can remain. They are outside this lab's scope.

## 9. Build and run

The proposed application code has not been applied or compiled while preparing this document. Run these steps after making the manual edits.

### Android

Sync Gradle in Android Studio, then run from the project root:

```bash
./gradlew :androidApp:assembleDebug
```

Use the existing Android app run configuration to launch it on a phone and a tablet emulator. Building also generates the image and string resource accessors.

If an accessor import is unresolved, check the file location, resource name, and the exact generated package above. Sync/build again. Do not create `Res` classes manually or change dependency versions to fix a filename problem.

Open `HomeScreenPhonePreview` and `HomeScreenTabletPreview` for static inspection. The existing `AppAndroidPreview` also calls the updated `App()`.

### iPhone and iPad

On a Mac with Xcode, open:

```text
iosApp/iosApp.xcodeproj
```

Run the existing scheme on an iPhone and an iPad simulator compatible with the project's deployment target, iOS `18.2` or later. The existing Xcode build phase invokes `:shared:embedAndSignAppleFrameworkForXcode`.

Physical devices require the normal signing configuration. Linux can be used for Android work, but it cannot run Xcode or an iOS simulator.

### Existing tests

The project's README lists these commands:

```bash
./gradlew :shared:testAndroidHostTest
```

On a Mac:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

The existing tests only check sample arithmetic. They do not verify this screen. No Compose UI test dependency is currently declared, so this lab uses compilation, previews, and device checks rather than adding a testing library.

## 10. Verify behavior and practice

| Check | Expected result |
| --- | --- |
| Phone portrait | Artwork above text and buttons |
| Wide tablet window | Artwork beside welcome content |
| Tablet split-screen | Layout adapts to available width |
| Phone landscape | Content remains reachable by scrolling |
| Large accessibility text | Text wraps and buttons can grow |
| Artwork | Mountains and emblem fully visible, no stretching |
| Press and release | Ripple and subtle scale, then normal size |
| Tap either action | No authentication or navigation yet |
| TalkBack/VoiceOver | Title heading and both button labels accessible |
| Rotate the device | No clipped or unreachable controls |

### Optional learning exercises

1. Temporarily change a preview width to `600` and then `1000`. Predict which branch runs before refreshing the preview.
2. Temporarily set `animateEntrance = false` at the app call site. Observe how the first render changes, then restore the default.
3. Put a debugger breakpoint inside a temporary `onSignInClick` lambda passed by `App`. Tap sign-in and confirm the callback is reached without changing `HomeScreen`.
4. Increase the system font size and inspect why `heightIn(min = 56.dp)` and scrolling are useful.

These exercises do not require a backend, authentication implementation, database, or another screen.
