# Zero-Crash Hardening, Ethiopian Campaign Missions & Adaptive HUD

A comprehensive production-grade upgrade for **Arbegna Strike** (*የአርበኛ ፍልሚያ*) that permanently eliminates startup and runtime rendering crashes, introduces historical Campaign Story Missions with progressive battle chapters, and implements an adaptive combat layout tailored for both vertical and horizontal displays.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed through user clarifications and guide this implementation:

- **Confirmed Decision 1 (Stability & Performance)**: Prioritize zero-crash Skia hardware-accelerated rendering and safe background audio pool. Eliminate all potential native `SIGABRT` crashes from unhandled float NaN/Inf values, SkPath churn, and AudioTrack/SoundPool races.
- **Confirmed Decision 2 (Production Feature)**: Add full **Campaign Story Missions** (*የታሪክ ዘመቻ*) featuring 4 playable historic Ethiopian battles (Gundet 1875, Dogali 1887, Adwa 1896, and Patriots' Resistance 1936-1941) with objective tracking, star ratings, and Amharic mission briefings.
- **Confirmed Decision 3 (Layout Ergonomics)**: Adaptive layout dynamically supporting both vertical (portrait handheld) and horizontal (landscape dual-stick) orientations with tailored HUD density.

---

### 1. Overview & Core Concept

- **What It Does**:
  1. **Crash Prevention Engine**: Hardens the rendering and physics engine against native Skia crashes (`SIGABRT`) by sanitizing all float coordinates, replacing crash-prone `LinearProgressIndicator` lambda calculations with fail-safe Compose Box bars, bounding all canvas loop iterations, and wrapping rendering/lifecycle transitions in defensive exception barriers.
  2. **Historical Campaign Mode**: Introduces an authentic single-player campaign recreating four monumental Ethiopian military triumphs with historical context, custom objectives (e.g. eliminate enemy general, hold fortress for 90 seconds, sniper tower defense), and unlockable medals.
  3. **Universal Adaptive HUD**: Dynamically adjusts touch controls, radar, and vital meters based on screen dimensions so the game plays comfortably in both portrait (compact vertical) and landscape (full panoramic) modes.
- **Target Audience**: Mobile action shooter fans who want an exciting, stable, and culturally rich 2D platform combat game inspired by Mini Militia, running flawlessly on devices and browser streaming emulators.
- **Key Value**: 100% crash-free stability, deep historical single-player progression, and responsive touch controls that never overflow or overlap.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Main Menu & Campaign Hub**:
     - Players can instantly jump into **Quick Play**, configure **Bot Deathmatch**, enter **Fortress Survival**, connect via **LAN Multiplayer**, or embark on the new **Campaign Missions (የታሪክ ዘመቻ)**.
  2. **Campaign Chapter Selection & Briefing**:
     - Visual mission selector with authentic historical artwork, battle maps, chapter lore in Amharic and English, objective descriptions, and 3-star victory rating tracking.
     - *Chapter 1: Battle of Gundet (1875)* – Repel coastal invaders using traditional shotel sabres, spears, and sparrow bows.
     - *Chapter 2: Dogali Pass Ambush (1887)* – Ras Alula's mountain guerillas executing cliff-side ambushes with antique flintlocks.
     - *Chapter 3: Victory at Adwa (1896)* – Emperor Menelik II & Empress Taytu's grand army countering colonial infantry with Martini-Henry rifles and artillery.
     - *Chapter 4: Arbegna Guerrilla Resistance (1941)* – Nighttime fortress assaults with modern Kalashnikovs, RPG rockets, and jetpack ambushes.
  3. **In-Game Adaptive Combat HUD**:
     - **Portrait Mode**: Streamlined top status bar featuring hero badge, safe health & fuel bars, compact mission objective pill, and pause trigger. Non-overlapping lower cluster featuring responsive joysticks (left movement, right 360° aim), central action badges (Shotel slash, weapon swap, reload), and dedicated Fire & Jetpack booster buttons.
     - **Landscape Mode**: Panoramic wide layout with full tactical mini-radar, detailed vitals, weapon stats card, and expanded thumb control arcs.
  4. **Mission Complete & Victory Screen**:
     - Battle summary debriefing, stars awarded based on completion time, damage taken, and enemies eliminated, with buttons to proceed to the next chapter or replay.

- **Visual Identity & Theme**:
  - **Aesthetic Direction**: Ethiopian historical battle grandeur blended with modern tactical action HUD (Imperial Gold `#F59E0B`, Emerald Green `#10B981`, Crimson `#EF4444`, and Dark Slate `#090D16`).
  - **Color Palette**: High-contrast, accessibility-tested color tokens with glowing status rings and clear team markers.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Skia Hardening vs Relying on Default Framework Behaviors**
  - *Chosen Approach*: Replace Material 3 `LinearProgressIndicator` lambda calculations with custom Compose `Box` progress meters and sanitize all coordinates (`camX`, `camY`, `player.x`, `alpha`) before passing them to the hardware-accelerated Canvas. Guard all loops with bounded iterations.
  - *Why*: On Android devices and emulators, hardware-accelerated Skia (`libhwui.so`) aborts the process (`SIGABRT`) when encountering `NaN` or infinite dimensions in draw primitives. Sanitizing values at the input layer guarantees crash-proof rendering.
  - *Alternatives Considered*: Retaining M3 progress indicators with try-catch blocks (discarded because internal native render threads can crash outside Java/Kotlin try-catch).

- **Decision 2: Lightweight SoundPool Audio Synthesizer with Guarded Playback**
  - *Chosen Approach*: Ensure all sound generation and SoundPool calls are bounded, rate-limited, and wrapped in complete `Throwable` barriers. If hardware audio is unavailable, degrade gracefully without interrupting gameplay.
  - *Why*: Prevents audio driver crashes and resource exhaustion when multiple bots fire simultaneously.

- **Decision 3: Campaign Progression Architecture**
  - *Chosen Approach*: Campaign state model stored with reactive progression (unlocked levels, high scores, stars) that integrates seamlessly into `GameEngine`'s existing wave and objective mechanics.
  - *Why*: Delivers an engaging story experience without bloating the codebase or requiring external dependencies.

---

### 4. Technical Architecture & Data Strategy (Technical Reference)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Arbegna Strike Application                      │
│                                                                        │
│   ┌───────────────────────────┐      ┌─────────────────────────────┐   │
│   │     Main Menu Screen      │◄────►│   Campaign Missions Hub     │   │
│   │ (Quick, Bots, Survival)   │      │   (4 Historic Chapters)     │   │
│   └─────────────┬─────────────┘      └──────────────┬──────────────┘   │
│                 │                                   │                  │
│                 └─────────────────┬─────────────────┘                  │
│                                   ▼                                    │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │                        In-Game Screen                          │   │
│   │                                                                │   │
│   │   ┌────────────────────────────────────────────────────────┐   │   │
│   │   │           Adaptive HUD (Portrait & Landscape)          │   │   │
│   │   │ • Crash-Proof Box Progress Bars (HP, Armor, Fuel)      │   │   │
│   │   │ • Compact Objective Tracker / Match Timer              │   │   │
│   │   │ • Responsive Twin Joysticks + Dedicated Action FABs    │   │   │
│   │   └───────────────────────────┬────────────────────────────┘   │   │
│   │                               │                                │   │
│   │   ┌───────────────────────────▼────────────────────────────┐   │   │
│   │   │              GameCanvas (Skia Hardware Draw)           │   │   │
│   │   │ • Defensive Try-Catch Frame Wrapper                    │   │   │
│   │   │ • NaN/Inf Coordinate Sanitization                      │   │   │
│   │   │ • Bounded Iteration Parallax & Platform Drawers        │   │   │
│   │   └────────────────────────────────────────────────────────┘   │   │
│   └───────────────────────────────┬────────────────────────────────┘   │
└───────────────────────────────────┼────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     GameEngine & Physics Core                          │
│  • 60 FPS update tick with safe delta-time clamping                    │
│  • Bot AI state machine with bounded aiming and range checks           │
│  • Campaign Mission Manager: objective conditions & win triggers       │
│  • Low-latency GameAudio pool with rate limiting and silent fallback   │
└────────────────────────────────────────────────────────────────────────┘
```

- **Core Components to Implement / Update**:
  1. `GameCanvas.kt`:
     - Wrap canvas body in defensive `try ... catch (t: Throwable)` to catch drawing anomalies.
     - Add strict NaN/Inf sanitization for camera coordinates, player positions, and particle alphas.
     - Guard all canvas `while` loops with maximum iteration limits (e.g. 50-80 iterations max).
     - Protect all `scope.drawText` measurements with fallback handling.
  2. `GameControls.kt`:
     - Implement responsive `TopHudBar` with `BoxWithConstraints` (Compact mode for width < 600dp, Panoramic mode for wide screens).
     - Replace `LinearProgressIndicator` with crash-proof, custom-clipped Compose `Box` progress bars.
     - Adjust joystick and action button spacing for narrow screens to ensure zero touch target overlap.
  3. `GameEngine.kt`:
     - Add `GameMode.CAMPAIGN_MISSION` with historical battle configuration (`CampaignChapter` model).
     - Replace while-loop angle normalization in `applySoftAimAssist` with O(1) modular arithmetic.
     - Guard `initGame`, `update`, and `tryFireWeapon` with defensive exception boundaries.
  4. `CampaignScreens.kt`:
     - New interactive Campaign Mission selection screen with chapter details, objectives, historical context, and star rating display.
  5. `MainActivity.kt`:
     - Add navigation route for `AppScreen.CAMPAIGN_SELECT`.
     - Guard activity-level match launch callbacks and haptic feedback.
  6. `ExampleRobolectricTest.kt`:
     - Comprehensive Robolectric tests verifying Bot Deathmatch, Campaign Mission initialization, adaptive HUD layout rendering, and float sanitization.
