# StudyConnect 🎓 — Peer Tutoring App

CAT 2 · BBIT Year 3.2 · Mobile Application Development · **Theme 3: Education Access and Equity**

## The problem
Students struggle with specific units (e.g. Statistics, Programming) but have no structured way to find a peer who
understands the material. Help happens through WhatsApp groups and word of mouth, so nobody knows who is available,
what they can teach, or when. The result is missed help, double-booked tutors and students falling behind before exams.

## The solution
StudyConnect lets students **register as tutors**, **search tutors by subject**, **book one-hour sessions** (with
double-booking prevention), **manage upcoming/past sessions**, find **learning resources** from a public API, and get a
**reminder notification** 30 minutes before each session.

## Features → CAT 2 requirements
| Requirement | Where it is in the code |
|---|---|
| Kotlin + OOP | Whole project; data classes, sealed interfaces, repositories |
| Functional UI (forms, lists, validation, loading/empty/error states) | `ui/` (Jetpack Compose, Material 3) |
| CRUD | Tutors: create/edit/delete (`TutorForm*`, `Profile*`). Bookings: book / reschedule / cancel / delete (`BookSession*`, `Sessions*`) |
| Data persistence | Room database `StudyConnect.db` (`data/local/`) – 3 tables with foreign keys |
| Architecture | UI (Compose) → ViewModel → Repository → Room / Retrofit |
| Navigation | Navigation Compose, 10 destinations, arguments (`tutorId`, `bookingId`), bottom bar, back stack handling |
| Dynamic lists | `LazyVerticalGrid` (tutors), `LazyColumn` (sessions, resources, my profiles) |
| API | Open Library search API via Retrofit (`data/remote/`), with offline/error handling |
| Background processing | WorkManager `ReminderWorker` + `ReminderScheduler` (`work/`) |
| Validation & errors | `util/Validators.kt`, inline field errors, duplicate + conflict checks |
| Testing | `ValidatorsTest` (unit), `BookingDaoTest` (instrumented, in-memory Room) |

## Architecture
```
 User
  ↓
 Compose screens (ui/*Screen.kt)      – draw state, send user events
  ↓
 ViewModels (ui/*ViewModel.kt)        – validation, UI state (StateFlow), business flow
  ↓
 Repositories (data/repository/)      – single source of truth, business rules (conflicts, duplicates)
  ↓
 Room DAOs / Retrofit API             – SQLite (data/local) · Open Library (data/remote)
```
`AppContainer` builds everything once (manual dependency injection) and `AppViewModelProvider` gives each ViewModel its repositories.

## Data model
- **users** (id, name, admissionNo*, email*, passwordHash) — `*` unique
- **tutors** (id, ownerId → users, name, subject, rating, bio, available)
- **bookings** (id, studentId → users, tutorId → tutors, startTime, notes, status)

Deleting a tutor cascades to its bookings (`ON DELETE CASCADE`). First launch seeds a "Peer Support Office" admin user with six sample tutors.

## Run it
1. Install **Android Studio** (Ladybug or newer) with JDK 17.
2. `File → Open` this folder. Let Gradle sync (it will download the wrapper and dependencies).
3. Run on an emulator or device (Android 8.0 / API 26+).
4. Create an account on the Sign-up screen, then explore.

Run tests: `./gradlew test` (unit) · `./gradlew connectedAndroidTest` (needs a device/emulator).

## Team
| Member | Admission no. | Main area |
|---|---|
| _name_ | _no._ | 
|Baraka Chemingich|190278|
|Michael Maina|190528|
|Alvin Muchiri|175883|
|Stanley Mutiga|190056|
|Michael Wairia|189948| 
