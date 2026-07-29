# Implementation Plan - Reactive Home Dashboard

The home dashboard is currently not refreshing when a new habit is created because the data flow is synchronous and one-time. I will refactor the repository, service, and view model to use Kotlin Flows for reactive updates.

## User Review Required

> [!IMPORTANT]
> This change introduces `kotlinx.coroutines.flow.Flow` to the core interfaces. All consumers of `IHabitRepository` and `IHomeService` will need to be updated to handle asynchronous data streams.

## Proposed Changes

### Core Data Layer

#### [MODIFY] [IHabitRepository.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/interfaces/IHabitRepository.kt)
- Change `getHabits()` return type from `List<Habit>` to `Flow<List<Habit>>`.

#### [MODIFY] [InMemoryIHabitRepository.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/respositories/InMemoryIHabitRepository.kt)
- Use a `MutableStateFlow` to hold and emit the current list of habits.
- Update `createHabit` to emit the new list through the flow.

### Core Service Layer

#### [MODIFY] [IHomeService.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/services/interfaces/IHomeService.kt)
- Change `getDashboardData()` to return `Flow<HomeDashboardUIState>`.

#### [MODIFY] [HomeService.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/services/implementations/HomeService.kt)
- Update `getDashboardData()` to combine repository flows into a dashboard state flow.

### Home Feature Layer

#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/feature/home/viewmodel/HomeViewModel.kt)
- Introduce `private val _searchQuery = MutableStateFlow("")` and `private val _selectedCategory = MutableStateFlow(HabitCategory.All)`.
- Use `combine` to merge `homeService.getDashboardData()`, `_searchQuery`, and `_selectedCategory` into the final `HomeUiState`.
- Update `onCategorySelected` and `searchHabits` to update these internal flows instead of manually updating `_uiState`.
- Use `stateIn` with `SharingStarted.WhileSubscribed(5000)` to expose the reactive `uiState`.

## Verification Plan

### Automated Tests
- Run existing unit tests for `HomeViewModel` and `HomeService` (if any) and update them for Flow support.
- I will check `C:/Users/zain4/GitProjects/Habitz/app/src/test/java/com/example/habitz/feature/home/` for existing tests.

### Manual Verification
1. Open the app to the Home page.
2. Navigate to Create Habit screen.
3. Create a new habit.
4. Navigate back to Home page (or observe if it auto-navigates).
5. Verify that the new habit appears immediately in the list without manual refresh.
