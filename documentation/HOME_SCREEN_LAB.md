# HomeScreen programming laboratory

Build a shared Compose screen by predicting, implementing, and observing one change at a time. Keep a brief lab notebook: record your prediction before each edit, then explain any difference you see in Preview. Do not jump to the final listing in phase 7.

The target is the supplied HomeScreen implementation, also recorded in `documentation/LAB_FIRST_SCREEN.md`. The current source file is only a preview stub calling an undefined `HomeScreen()`; it is not the completed screen. This laboratory reconstructs the supplied target rather than claiming that the stub already works. This document changes no application source files.

Work in `shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/feature/home/presentation/HomeScreen.kt`. Keep experiments in that file so previews can access private components. Snippets say whether to add, replace, or insert code; they are successive edits, not separate implementations. Use Android Studio to import symbols as they become necessary. Keep only one definition of each function. Small temporary scaffolds are explicitly removed later.

## 1. Project/context preparation

### Goal
Locate the shared presentation boundary and prepare a working preview without adding libraries or inventing navigation.

### Concepts
`commonMain` contains UI shared by Android and iOS. A composable emits UI; it is not an Activity. A theme provides Material defaults to descendants. Resources are generated Kotlin accessors over shared assets.

### Step 1 — Trace the actual entry points
Read these files and draw the call chain in your notebook:

| File | Existing role |
| --- | --- |
| `androidApp/src/main/kotlin/mx/edu/orizaba/nahuatlcorpus/MainActivity.kt` | Enables edge-to-edge and calls `App()` inside `setContent`. |
| `shared/src/iosMain/kotlin/mx/edu/orizaba/nahuatlcorpus/MainViewController.kt` | Creates `ComposeUIViewController { App() }`. |
| `shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/App.kt` | Currently displays the Compose starter demo. |
| `shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/feature/home/presentation/HomeScreen.kt` | Presentation file being rebuilt. |
| `shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/navigation/AppNavigation.kt` and `AppDestination.kt` | Package-only placeholders, not a navigation implementation. |

HomeScreen receives two callbacks. It owns local visual state only; no ViewModel, repository, authentication dependency, or navigation library is needed for this screen.

### Step 2 — Inventory what already exists
`settings.gradle.kts` includes `:shared` and `:androidApp`. `shared/build.gradle.kts` applies Kotlin Multiplatform, Android Multiplatform Library, Compose Multiplatform, and the Compose compiler plugins. Its targets are Android, `iosArm64`, and `iosSimulatorArm64`.

The checked-in `gradle/libs.versions.toml` declares Kotlin **2.4.20**, Compose Multiplatform **1.12.1**, Material 3 **1.12.0-alpha03**, and AGP **9.4.1**. These are project declarations, not instructions to upgrade. Android uses compile/target SDK 37, minimum SDK 24, and JVM target 11.

The shared module already declares runtime, foundation, Material 3, UI, Compose resources, and preview tooling in `commonMain`; Android tooling is also configured through `androidMain` and `androidRuntimeClasspath`. Lifecycle dependencies exist but this screen does not need them. Do not add dependencies for this lab.

| Existing asset | Accessor/import |
| --- | --- |
| `shared/src/commonMain/composeResources/drawable/corpus_header.png`, 1521 × 1034 pixels | `Res.drawable.corpus_header`, `painterResource` |
| `shared/src/commonMain/composeResources/values/strings.xml` | `Res.string.welcome_title`, `welcome_subtitle`, `welcome_sign_in`, `welcome_create_account`, `stringResource` |
| `shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/ui/theme/CorpusTheme.kt` | `mx.edu.orizaba.nahuatlcorpus.ui.theme.CorpusTheme` |

The strings are “Corpus Náhuatl–Español”, “Recopilación y validación de datos lingüísticos”, “Iniciar sesión”, and “Crear cuenta”. Import `nahuatlcorpus.shared.generated.resources.Res` and each referenced generated resource symbol. Use `org.jetbrains.compose.resources.painterResource` and `stringResource`, not Android `R` or Android-only resource helpers.

### Step 3 — Reconcile the theme and clear the stub
The current theme has `primaryContainer = Color(0xFF245C3D)` and `secondaryContainer = Color(0xFF52674D)`. Your supplied target theme instead uses the following two assignments. Update these roles in the existing theme when performing the lab to reproduce that reference palette:

```kotlin
primaryContainer = Color(0xFFE4EEDC),
secondaryContainer = Color(0xFFE8EEDD),
```

Keep the other roles and `Typography()` unchanged. It uses default Material typography without a custom font asset. The header uses `primaryContainer`; the secondary container is part of the supplied palette but is not used by HomeScreen.

Replace the broken HomeScreen preview stub with the existing package declaration alone for now. Phase 2 adds the first working preview. Until phase 5, there is intentionally no public HomeScreen function.

### Preview
Use the actual project import `androidx.compose.ui.tooling.preview.Preview`. Each preview below wraps its component in `CorpusTheme`. Sync Gradle, select the Android preview target, and use Build & Refresh after edits. If generated accessors are unresolved, build/sync the shared module before changing resource names or adding dependencies.

### What should I observe?
The starter application still runs through `App()`. Your new component previews will be independent of it. After the theme adjustment, the header container will be pale green when introduced in phase 2.

### Why does this work?
The platform entry points host shared composables. Keeping this screen in its existing presentation package preserves that boundary. Callbacks let the host decide what sign-in means without coupling the UI to an unfinished navigation layer.

### Try it yourself
Find the Spanish subtitle in the XML and predict how a longer translation would wrap. Do not change the resource permanently. Trace where `CorpusTheme` must wrap the UI: it currently does not wrap the starter `App()`.

### Checkpoint
You can identify both platform entry points, the shared resource namespace, the theme mismatch, and the exact file you will edit. No dependency changes are necessary.

## 2. HeaderArtwork

### Goal
Build the decorative image card, discovering how parent constraints, modifier order, and image scaling determine its shape.

### Concepts
`Box` positions a child. `Surface` supplies a Material background and shape. `Image` draws a painter. `dp` expresses density-independent layout size; `aspectRatio` relates measured width to height.

### Step 1 — Create the smallest useful Preview first
Add these functions after the package and relevant imports. The temporary body lets the preview compile before any artwork exists:

```kotlin
@Preview(name = "Header scaffold", widthDp = 390, heightDp = 320)
@Composable
private fun HeaderArtworkPreview() {
    CorpusTheme {
        HeaderArtwork(Modifier.fillMaxWidth())
    }
}

@Composable
private fun HeaderArtwork(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text("Artwork area") // Temporary measurement marker.
    }
}
```

Refresh now: predict where the marker appears and why it occupies little height.

### Step 2 — Introduce a Surface
Replace the marker with this card scaffold and add `contentAlignment = Alignment.Center` to the surrounding Box:

```kotlin
Surface(
    modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
    shape = RoundedCornerShape(32.dp),
    color = MaterialTheme.colorScheme.primaryContainer,
) {
    Box(Modifier.height(200.dp)) // Temporary visible height.
}
```

Refresh: you should see a pale green card with rounded corners. Its width follows the preview up to a 480 dp limit. Temporarily change the preview width to 700 dp to see the Box center the capped card.

### Step 3 — Draw the resource without cropping
Replace the temporary inner Box with:

```kotlin
Image(
    painter = painterResource(Res.drawable.corpus_header),
    contentDescription = null,
    contentScale = ContentScale.Fit,
    modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .aspectRatio(1521f / 1034f),
)
```

Refresh: the artwork replaces the empty rectangle, its full image fits inside the padded rounded card, and height now follows width rather than a fixed 200 dp.

### Preview
Keep `HeaderArtworkPreview` and restore its width to 390 dp. Rename its preview to “Header artwork”. Inspect it again after each of steps 2 and 3. Keep the temporary component previews through phase 6.

### What should I observe?
Marker → rounded colored card → complete artwork. At a wider preview, the card stops growing at 480 dp. The image has a 16 dp inset; its measured image region follows the original 1521:1034 ratio.

### Why does this work?
The caller's `modifier` belongs on the outer Box so the future parent can control allocation. The inner Surface caps the visual card independently. `widthIn(max = 480.dp)` constrains the width before `fillMaxWidth()` fills that permitted width. Reversing them can make an outer fill impose an exact width that the inner cap cannot reduce.

Modifier chains wrap the following measurement/drawing operations. Here `padding` reduces the space available to the image before it fills the remaining width and computes its ratio. The padding belongs inside the Surface, so the card background remains behind the inset. `ContentScale.Fit` preserves the painter's proportions without cropping. The explicit ratio reserves a predictable layout area; it is not a pixel-sized fixed height.

`contentDescription = null` deliberately treats this image as decoration. The title communicates the screen's identity; repeating it through the illustration would add redundant accessibility output. The Surface supplies the rounded clipping shape and themed container color.

### Try it yourself
Change `Fit` to `Crop` and the ratio to `1f`; observe missing image edges, then restore both. Swap `widthIn` and `fillMaxWidth` at a 700 dp preview; compare the cap, then restore their original order and the 390 dp preview.

### Checkpoint
HeaderArtwork accepts a Modifier, centers a capped rounded Surface, uses the existing resource, and preserves the image ratio. Remove both temporary markers.

## 3. WelcomeContent

### Goal
Build the readable welcome text and action positions before designing the reusable button.

### Concepts
`Column` lays out children vertically. Horizontal child alignment differs from `TextAlign`, which positions text inside each Text's bounds. Theme typography specifies text styles; resources keep UI labels out of layout code.

### Step 1 — Preview a minimal content scaffold
Add the preview first, then the smallest callable function:

```kotlin
@Preview(name = "Welcome content", widthDp = 390, heightDp = 440)
@Composable
private fun WelcomeContentPreview() {
    CorpusTheme {
        WelcomeContent(
            onSignInClick = {},
            onCreateAccountClick = {},
            centered = true,
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
        )
    }
}

@Composable
private fun WelcomeContent(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    centered: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(stringResource(Res.string.welcome_title))
    }
}
```

Refresh: one title appears in default text styling. Unused parameters are temporary while you build the body.

### Step 2 — Style and align the title
Before the Column, add:

```kotlin
val textAlignment = if (centered) TextAlign.Center else TextAlign.Start
```

Give the Column this `horizontalAlignment`:

```kotlin
horizontalAlignment = if (centered) {
    Alignment.CenterHorizontally
} else {
    Alignment.Start
},
```

Replace its title Text with:

```kotlin
Text(
    text = stringResource(Res.string.welcome_title),
    style = MaterialTheme.typography.displaySmall,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.onBackground,
    textAlign = textAlignment,
    modifier = Modifier.fillMaxWidth().semantics { heading() },
)
```

Refresh: the title becomes large, semibold, and centered within the available width. Line wrapping is expected on a phone. `heading()` changes accessibility semantics, not visual styling.

### Step 3 — Add subtitle, then spacing
Append the subtitle first and refresh before adding spacing:

```kotlin
Text(
    text = stringResource(Res.string.welcome_subtitle),
    style = MaterialTheme.typography.bodyLarge,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = textAlignment,
    modifier = Modifier.fillMaxWidth(),
)
```

You should see smaller supporting text immediately below the title. Now insert `Spacer(Modifier.height(16.dp))` between the two Text calls. Refresh: only their separation should increase.

### Step 4 — Reserve the actions with a temporary helper
Phase 4 owns button design. For now add this deliberately basic helper after WelcomeContent:

```kotlin
@Composable
private fun WelcomeButton(text: String, onClick: () -> Unit, primary: Boolean) {
    Button(onClick = onClick) { Text(text) }
}
```

Append to the Column after the subtitle:

```kotlin
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
```

Refresh: two basic filled buttons appear; `primary` is intentionally unused until phase 4. Do not duplicate the helper later.

### Preview
After each step refresh `WelcomeContentPreview`. Add a second preview by duplicating that preview function, naming it `WelcomeContentStartPreview`, and changing `centered` to `false`. Keep both at 390 dp so alignment is the only variable.

### What should I observe?
A plain title becomes a prominent heading; a subtitle and measured gaps follow. Centered and start-aligned versions use the same strings and widths. Buttons are still temporary basic buttons.

### Why does this work?
Full-width Text gives `TextAlign` space in which to operate. Column alignment controls children, including buttons that are not yet full-width. `Start` respects layout direction. The same `centered` argument will later adapt phone and wide layouts without duplicating content.

Avoid fixed text heights: wrapping and larger font settings need additional room. The parent will cap content width and allow vertical scrolling. Callback forwarding preserves the host's ownership of actions; text and button styling remain presentation concerns.

### Try it yourself
Change the title/subtitle gap from 16 to 4 dp and describe the grouping effect, then restore 16 dp. Narrow a preview to 320 dp and predict wrapping before refreshing; restore 390 dp.

### Checkpoint
The final text styles, heading semantics, resource labels, 16/32/12 dp gaps, and both forwarded callbacks exist. The button scaffold is the only intentionally unfinished component.

## 4. WelcomeButton

### Goal
Turn the existing helper into the primary and outlined Material actions. Keep it static until phase 6.

### Concepts
Material 3 buttons supply click semantics and standard interaction handling. A shared modifier expresses layout; shape, padding, colors, and border express visual roles. `primary` is an input selecting a variant, not locally remembered state.

### Step 1 — Preview both variants before editing the helper
Add:

```kotlin
@Preview(name = "Welcome button variants", widthDp = 390, heightDp = 220)
@Composable
private fun WelcomeButtonPreview() {
    CorpusTheme {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WelcomeButton(stringResource(Res.string.welcome_sign_in), {}, true)
            WelcomeButton(stringResource(Res.string.welcome_create_account), {}, false)
        }
    }
}
```

Refresh: both still look filled, proving that the helper has not implemented its variant yet.

### Step 2 — Establish the basic button measurements
Inside the existing WelcomeButton, before its Button, add:

```kotlin
val buttonModifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
val shape = RoundedCornerShape(16.dp)
val contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
```

Pass `modifier = buttonModifier`, `shape = shape`, and `contentPadding = contentPadding` to Button. Replace its Text with:

```kotlin
Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    textAlign = TextAlign.Center,
)
```

Refresh: both buttons fill the preview's padded width and have at least 56 dp height with rounded corners. A minimum height permits taller content instead of forcing clipping.

### Step 3 — Implement the primary variant
Wrap the existing Button in `if (primary) { ... } else { ... }`. Initially duplicate the measured Button into the else branch so the function stays usable. On the primary Button add:

```kotlin
colors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
),
```

Refresh: the sign-in button uses the theme's green fill and white content. The other branch is still a filled placeholder.

### Step 4 — Implement the outlined variant
Change the else branch's Button to `OutlinedButton`. Keep the same `onClick`, modifier, shape, content padding, and Text. Add:

```kotlin
border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
colors = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.primary,
),
```

If you copied primary colors into the else branch, replace that colors argument rather than adding a second one. Refresh: the secondary action has an outline and green text, with the same dimensions as the primary action.

### Preview
Inspect both the button preview and the two WelcomeContent previews after each measurement or variant edit. This catches how reusable styling changes the composed text section.

### What should I observe?
Two identical small filled buttons become equally wide rounded actions, then separate into filled sign-in and outlined create-account treatments. Neither scales on press yet; Material's default press feedback remains.

### Why does this work?
The shared modifier keeps variant geometry consistent. Material buttons already handle semantic roles, enabled click actions, and default visual feedback; a separate clickable modifier would be redundant. Theme role pairs specify readable foreground/background combinations without duplicating palette literals.

A `MutableInteractionSource` will let us observe button presses in phase 6. Material handles interactions internally already; we need to remember an explicit source only when connecting that interaction state to custom scale animation. Do not add it early merely to create unused state.

### Try it yourself
Change the minimum height to 72 dp, observe both variants, then restore 56 dp. Change the corner radius to 0 dp, compare the shape, then restore 16 dp.

### Checkpoint
The helper has a complete static `if (primary)` implementation, consistent measurements and typography, and both variants invoke their supplied onClick. There is no custom animation yet.

## 5. HomeScreen composition

### Goal
Assemble the components and progressively adapt them to available space, preserving the target screen's actual breakpoint.

### Concepts
A parent measures children under constraints. `BoxWithConstraints` exposes those constraints as dp values. `Row` supports weights; a scroll container allows content taller than the viewport. Window insets reserve drawing space around system UI.

### Step 1 — Start with a phone Preview and simple composition
Add the preview first, followed by the static HomeScreen scaffold:

```kotlin
@Preview(name = "Welcome phone", widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPhonePreview() {
    CorpusTheme {
        HomeScreen(onSignInClick = {}, onCreateAccountClick = {}, animateEntrance = false)
    }
}

@Composable
fun HomeScreen(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    animateEntrance: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            HeaderArtwork(Modifier.widthIn(max = 480.dp).fillMaxWidth())
            Spacer(Modifier.height(32.dp))
            WelcomeContent(
                onSignInClick = onSignInClick,
                onCreateAccountClick = onCreateAccountClick,
                centered = true,
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
            )
        }
    }
}
```

`animateEntrance` deliberately has no effect yet. Refresh: the existing parts now form one centered vertical screen. On a short viewport this scaffold can overflow; solve that in step 3.

### Step 2 — Add a tablet Preview before responsiveness
Duplicate the phone preview, rename the function `HomeScreenTabletPreview`, and set `name = "Welcome tablet"`, `widthDp = 1024`, `heightDp = 768`. Keep entrance disabled. Refresh both: the tablet still stacks components vertically. Record why this wastes horizontal space before introducing the wide branch.

### Step 3 — Establish the viewport and scrollable content
Replace the Surface's single Column with this scaffold, moving its three children to the marked position:

```kotlin
BoxWithConstraints(
    modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
        .padding(horizontal = 24.dp, vertical = 24.dp),
) {
    val minimumContentHeight = maxHeight
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 1120.dp)
                .fillMaxWidth()
                .heightIn(min = minimumContentHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Move HeaderArtwork, Spacer, and WelcomeContent here unchanged.
        }
    }
}
```

Refresh: a tall viewport keeps content centered. Temporarily use a 390 × 500 preview and Interactive Preview to scroll to both actions. Restore the phone dimensions afterward.

### Step 4 — Add the actual wide-layout decision
Inside BoxWithConstraints, before `minimumContentHeight`, add:

```kotlin
val useWideLayout = maxWidth >= 840.dp
```

Wrap the three inner children in an `else` branch and insert the wide branch before it:

```kotlin
if (useWideLayout) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderArtwork(modifier = Modifier.weight(1f))
        WelcomeContent(
            onSignInClick = onSignInClick,
            onCreateAccountClick = onCreateAccountClick,
            centered = false,
            modifier = Modifier.weight(1f),
        )
    }
} else {
    // Keep the three narrow-layout children from step 1 here.
}
```

Refresh both previews: the tablet becomes two equal allocated columns separated by 48 dp; text aligns to the start. The phone remains stacked and centered.

### Preview
Retain the two named screen previews with entrance disabled. For a temporary breakpoint pair, duplicate the tablet preview at widths 887 and 888 dp. With no horizontal safe insets and 24 dp padding on each side, their inner widths are 839 and 840 dp. Remove this pair after inspection.

### What should I observe?
Phone: capped artwork, 32 dp separation, capped centered content. Tablet: artwork beside start-aligned content. Short windows scroll; spacious windows center the content vertically. On very wide windows the entire group stops expanding at 1120 dp.

### Why does this work?
`maxWidth` here is the width available **inside** safe drawing insets and padding. The breakpoint is 840 dp of usable content, not an unconditional physical device width or “tablet” flag. Rotation and window resizing can change the branch.

`maxHeight` is captured before entering the scroll container. Vertical scrolling measures content with an unbounded vertical maximum; using that finite viewport height as `heightIn(min = ...)` preserves centering when content is short while allowing it to grow when tall. `rememberScrollState()` retains scroll position during recomposition while that composition remains present.

`safeDrawingPadding()` accommodates system bars/cutouts; the following 24 dp padding adds design spacing. Static previews often have no realistic system insets, so verify this on a device. `widthIn` before `fillMaxWidth` caps growth, while equal Row weights split the width left after the gap. The artwork's own cap still applies inside its allocation.

### Try it yourself
Change the temporary breakpoint previews by 1 dp and explain the branch switch, then remove them. Change the wide gap to 16 dp and describe the added width available to each weighted child; restore 48 dp.

### Checkpoint
The static HomeScreen matches target layout and forwards callbacks in both branches. Its signature already contains the eventual entrance flag. Both screen previews compile, and short-height content can scroll.

## 6. Interactions and animations

### Goal
Add transient visual behavior only after the layout and action contracts are stable.

### Concepts
`remember` keeps a value across recompositions within a composition. `mutableStateOf` makes changes observable. `LaunchedEffect` runs a coroutine tied to composition. `animateFloatAsState` animates changing targets. `graphicsLayer` changes rendering without changing measured layout.

### Step 1 — Prepare animation observation before modifying behavior
Keep static phone/tablet previews with `animateEntrance = false`. Add a temporary duplicate phone preview named “Welcome entrance interactive”, with a distinct function name and `animateEntrance = true`. Refresh: before implementing animation, it looks identical. This provides an explicit comparison target.

### Step 2 — Add entrance state and progress
At the beginning of HomeScreen, before Surface, insert:

```kotlin
var entered by remember { mutableStateOf(false) }
LaunchedEffect(Unit) {
    entered = true
}
val entranceProgress = animateFloatAsState(
    targetValue = if (entered || !animateEntrance) 1f else 0f,
    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
    label = "Welcome entrance",
)
```

Refresh: no visual change yet because nothing reads progress for drawing. `by` uses the runtime `getValue`/`setValue` imports. `LaunchedEffect(Unit)` runs on entering composition; ordinary recompositions do not restart it.

### Step 3 — Apply progress to the content layer
Append this modifier after the inner content Column's `heightIn`:

```kotlin
.graphicsLayer {
    alpha = entranceProgress.value
    translationY = (1f - entranceProgress.value) * 16.dp.toPx()
}
```

Refresh static previews: they stay fully visible because their initial target is 1f. Start or restart the interactive entrance preview: content should fade in and rise from 16 dp below its final position over 420 ms. If the tooling completes the effect before displaying the interactive session, verify the transition by opening the screen on an emulator/device.

### Step 4 — Observe button interaction state
At the beginning of WelcomeButton insert:

```kotlin
val interactionSource = remember { MutableInteractionSource() }
val pressed by interactionSource.collectIsPressedAsState()
```

Pass `interactionSource = interactionSource` to **both** Button and OutlinedButton. Refresh: their static appearance is unchanged. The remembered source is stable so collection follows the same interaction stream across recompositions. Press state is collected from Material's interactions; do not toggle a separate Boolean in onClick.

### Step 5 — Animate press scaling
After collecting `pressed`, add:

```kotlin
val pressScale = animateFloatAsState(
    targetValue = if (pressed) 0.985f else 1f,
    animationSpec = tween(durationMillis = 100),
    label = "Welcome button press",
)
```

Append to `buttonModifier`, after `heightIn`:

```kotlin
.graphicsLayer {
    scaleX = pressScale.value
    scaleY = pressScale.value
}
```

Use Interactive Preview to press and hold each variant, then release. Both should contract slightly and return to their normal size. The measured layout and spacing remain stable because scaling affects the layer, not layout constraints.

### Preview
Static previews verify resting appearance, not elapsed animation. Interactive Preview can demonstrate press/release and callbacks; entrance observation depends on composition timing in the tooling. Emulator/device checks establish real touch behavior, launch timing, safe insets, and accessibility.

For observable callback verification, temporarily replace the content of the phone preview with this themed body:

```kotlin
var lastAction by remember { mutableStateOf("No action") }
Box {
    HomeScreen(
        onSignInClick = { lastAction = "Sign-in callback" },
        onCreateAccountClick = { lastAction = "Create-account callback" },
        animateEntrance = false,
    )
    Text(lastAction, modifier = Modifier.align(Alignment.TopCenter))
}
```

This diagnostic overlay is preview-only. Activate Interactive Preview and verify that each action updates its label. Restore the original phone preview afterward; do not add status text to HomeScreen.

### What should I observe?
Entrance affects the whole inner content group, leaving the background stationary. Each press affects only that button. Release restores scale to 1f and invokes the corresponding callback for a completed click. Cancelling a press should restore its scale without completing a click.

### Why does this work?
Setting `entered` schedules recomposition and changes the animated target from 0f to 1f. The effect has no explicit delay. The state is remembered, not saved across screen destruction: a new composition can animate again. `animateEntrance = false` starts with the final target; it does not disable button animations.

Translation uses pixels, so `16.dp.toPx()` runs in the layer's density-aware scope. `FastOutSlowInEasing` controls entrance acceleration; the button tween uses its default easing. Animation labels identify values in tooling. Reading animation state inside the layer block lets drawing follow changing values without using animation to recalculate component sizes.

### Try it yourself
Temporarily use a 1500 ms entrance tween to observe timing, then restore 420 ms. Try a press scale of 0.9f, compare how exaggerated it feels, then restore 0.985f and 100 ms duration.

### Checkpoint
Static previews remain stable; interactive presses shrink both variants; callbacks remain separate; runtime entrance fades and rises once per composition entry. Restore all experimental values and remove the temporary entrance preview.

## 7. Final verification

### Goal
Audit your assembled work against the target and verify the actual platform integration.

### Concepts
A preview is a UI harness, not the runtime entry point. Compiling establishes API compatibility; interactive and device checks establish behavior. Accessibility semantics and safe drawing require runtime inspection as well as visual comparison.

### Step 1 — Compare your reconstruction
Explain these decisions from memory before reading the listing: why the image is decorative; why widths are capped before filling; why the viewport height is captured outside scrolling; why the breakpoint uses available width; why button state comes from interactions; and why the host owns callbacks.

Remove the temporary HeaderArtwork, WelcomeContent, and WelcomeButton previews if you want the exact final file below. Keep only the two final screen previews. The complete target appears here for the first time:

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

### Step 2 — Connect the runtime host for device verification
The checked-in `App()` still displays the starter demo. After rebuilding HomeScreen, replace that demo in `shared/src/commonMain/kotlin/mx/edu/orizaba/nahuatlcorpus/App.kt` with this small host, already used by the earlier lab:

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

Existing Android and iOS entry points can continue calling `App()`. Empty defaults do not implement authentication or navigation. For runtime callback checks, set breakpoints in temporary nonempty callbacks or pass temporary counters from the host, then remove that instrumentation. Do not add authentication destinations as part of this lab.

Build Android from the repository root with:

```bash
./gradlew :androidApp:assembleDebug
```

This also compiles the shared Android UI and generates resources. Run the app from Android Studio on a phone and tablet emulator/device. Building/running the iOS app requires macOS and Xcode; inspect the same shared screen through the existing iOS host there. If tooling or SDK prerequisites fail, resolve the environment rather than guessing new dependency versions.

### Preview
Refresh “Welcome phone” (390 × 844) and “Welcome tablet” (1024 × 768). Both use `animateEntrance = false` intentionally. Compare these with your earlier observations, then use Interactive Preview and device runs for behavior.

### What should I observe?
The finished UI reproduces the supplied implementation: the pale green illustration card, resource-backed heading and subtitle, full-width filled and outlined actions, stacked narrow layout, side-by-side wide layout, entrance motion, and subtle press scaling. Runtime appearance should now agree with previews because App uses CorpusTheme and HomeScreen.

### Why does this work?
The final assembly retains the public callback API and private presentation helpers. Platform hosting remains outside the feature; shared resource access and theme roles remain inside shared UI. The only local state controls visual behavior. The complete listing matches the supplied screen rather than introducing new architecture.

### Try it yourself
Rotate a running device or resize its window and record the usable-width branch change; restore the normal orientation. Increase system font size and confirm that text can wrap and actions remain reachable through scrolling; restore the device setting.

### Checkpoint
Complete this short verification record and note the device/window size used:

- [ ] Phone preview: stacked, centered content with 480 dp artwork and 400 dp text-section caps.
- [ ] Tablet preview: equal weighted columns, 48 dp gap, start-aligned text, and 1120 dp overall cap.
- [ ] Responsiveness: switch at 840 dp **inner** width; short windows and larger fonts allow access to both buttons by scrolling.
- [ ] Resources/theme: correct 1521 × 1034 image, four existing strings, supplied container colors, and CorpusTheme in both preview and runtime host.
- [ ] Interactions: each completed click reaches its own callback; cancelling a press restores scale without completing the action.
- [ ] Animations: runtime entrance lasts 420 ms and rises 16 dp; both button variants scale to 0.985f over 100 ms and recover on release.
- [ ] Accessibility/device: heading is announced as a heading, decorative artwork is skipped, Material buttons expose their labels/actions, and system bars/cutouts do not cover content. Check Android with TalkBack and iOS with VoiceOver where available.

You have finished when you can explain your observations and reproduce each component from its contract, not merely when your code resembles the final listing.
