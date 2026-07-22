# Habitz Architecture

Habitz is organized as a feature-first, local-first, clean architecture Android app.

The app currently stays in a single Gradle `:app` module so development can move quickly. Packages are arranged so individual features can be extracted into separate modules later if the codebase grows.

## Package Layout

```text
com.example.habitz
+-- core
|   +-- common
|   +-- database
|   |   +-- dao
|   |   +-- entity
|   |   +-- migration
|   +-- datastore
|   +-- designsystem
|   |   +-- component
|   |   +-- theme
|   +-- model
|   +-- navigation
|   +-- notifications
|   +-- testing
|   +-- time
+-- di
+-- feature
    +-- home
    +-- habits
    +-- checkins
    +-- statistics
    +-- settings
```

Each feature follows this shape:

```text
feature/<feature-name>
+-- data
|   +-- local
|   +-- mapper
|   +-- repository
+-- domain
|   +-- model
|   +-- repository
|   +-- usecase
+-- presentation
    +-- components
    +-- screen
    +-- state
```

## Layer Rules

- `presentation` contains Compose screens, state holders, UI events, and feature-specific UI components.
- `domain` contains business models, repository contracts, and use cases. It should not depend on Android, Room, DataStore, or Compose.
- `data` contains repository implementations, local data sources, persistence mappers, and storage-specific code.
- `core/database` owns the Room database, DAOs, entities, and migrations.
- `core/datastore` owns local preferences and settings persistence.
- `core/designsystem` owns reusable Material Expressive styling, shared Compose components, colors, typography, and shape decisions.
- `core/navigation` owns app-level routes and navigation helpers.
- `di` wires dependencies together. Keep this thin and boring.

## Local-First Boundaries

Habitz does not depend on remote APIs. Source of truth should be local storage:

- Use Room for structured habit, completion, streak, and history data.
- Use DataStore for lightweight preferences such as theme, reminder settings, and first-run state.
- Keep repository interfaces in `domain`; implement them in `data`.
- Convert database entities to domain models in `data/mapper`.
- Do not expose Room entities directly to `presentation`.

## Suggested Feature Ownership

- `feature/home`: dashboard composition, today overview, category filtering, and entry points into habit actions.
- `feature/habits`: create, edit, archive, reorder, and view habit definitions.
- `feature/checkins`: daily completion flow, undo, skip, notes, and local completion history.
- `feature/statistics`: streaks, completion rates, calendar summaries, and trend visualizations.
- `feature/settings`: appearance, local reminders, backup/export options, and app preferences.

## Testing Layout

Mirror feature packages under `app/src/test` for domain and repository tests. Use `app/src/androidTest` for Compose UI tests and Room integration tests that need Android runtime behavior.
