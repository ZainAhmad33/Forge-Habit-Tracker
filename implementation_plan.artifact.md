# Room Database Integration Plan

Integrate Room as the primary database store for the Habitz project, replacing existing in-memory implementations.

## User Review Required

> [!IMPORTANT]
> The migration will transition data storage from in-memory (volatile) to Room (persistent). Initial data currently in `dummyDatabase` will need to be seeded into Room if we want to preserve it for the first run.

## Proposed Changes

### Dependencies & Setup

#### [MODIFY] [libs.versions.toml](file:///C:/Users/zain4/GitProjects/Habitz/gradle/libs.versions.toml)
- Add Room version and library definitions.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/zain4/GitProjects/Habitz/app/build.gradle.kts)
- Apply Room dependencies and KSP for annotation processing.

---

### Core Database Layer

#### [NEW] [Converters.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/Converters.kt)
- Type converters for `UUID`, `Date`, `LocalTime`, `List<LocalTime>`, `List<Int>`, and various Enums.

#### [NEW] [HabitzDatabase.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/HabitzDatabase.kt)
- Define the Room database class with entities: `Habit`, `HabitActivity`, `User`, `Reward`.

#### [NEW] [DAOs](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/dao/)
- `HabitDao`: CRUD for habits.
- `HabitActivityDao`: CRUD for habit tracking logs.
- `UserDao`: CRUD for user profile.
- `RewardDao`: CRUD for rewards.

---

### Entities Modification

#### [MODIFY] [Habit.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/entity/Habit.kt)
- Add `@Entity` and `@PrimaryKey` annotations.

#### [MODIFY] [HabitActivity.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/entity/HabitActivity.kt)
- Add `@Entity` and `@PrimaryKey` annotations.

#### [MODIFY] [User.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/entity/User.kt)
- Add `@Entity` and `@PrimaryKey` annotations. (Will need an ID if not present).

#### [MODIFY] [Reward.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/entity/Reward.kt)
- Add `@Entity` and `@PrimaryKey` annotations.

---

### Repositories Implementation

#### [NEW] [Room repositories](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/database/respositories/)
- `RoomHabitRepository`: Implementation of `IHabitRepository` using `HabitDao`.
- `RoomHabitActivityRepository`: Implementation of `IHabitActivityRepository` using `HabitActivityDao`.
- `RoomUserRepository`: Implementation of `IUserRepository` using `UserDao`.

---

### Dependency Injection

#### [NEW] [DatabaseModule.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/di/DatabaseModule.kt)
- Hilt module to provide `HabitzDatabase` and DAOs.

#### [MODIFY] [RepositoryModule.kt](file:///C:/Users/zain4/GitProjects/Habitz/app/src/main/java/com/example/habitz/core/di/RepositoryModule.kt)
- Update `@Binds` to use Room-based repository implementations.

## Verification Plan

### Automated Tests
- Create `HabitDaoTest` to verify Room operations.
- Run `gradlew test` to ensure no regressions in existing logic.

### Manual Verification
- Deploy the app and verify that habits are saved across app restarts.
- Add a new habit and confirm it appears in the list after a restart.
