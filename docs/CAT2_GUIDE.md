# CAT 2 Guide — StudyConnect

## 1. Rubric map (30 marks)
| Component (marks) | Evidence to show |
|---|---|
| Problem & relevance (4) | Concept note + README: problem, users, current WhatsApp approach, its limits, how the app fixes it |
| UI/UX (4) | Splash, login with inline errors, home grid, forms, loading / empty / error states |
| CRUD (4) | Tutor: create, edit, delete. Booking: create, reschedule (update), cancel (update), delete |
| Persistence (4) | Add data → force-close app → reopen → still there. Show `AppDatabase.kt`, `Entities.kt`, `Daos.kt` |
| Architecture & data flow (4) | Diagram in README; trace "Book a session" through every layer (section 4) |
| Navigation & functionality (2) | Bottom bar, `tutorId`/`bookingId` arguments, back stack |
| API / background (2) | Resources tab (Retrofit) · reminder notification (WorkManager) |
| Testing, validation, errors (2) | `ValidatorsTest`, `BookingDaoTest`, invalid-input demo, airplane-mode demo |
| GitHub (2) | Section 3: commits per member, branches, issues, PRs |
| Presentation & defence (2) | Sections 4 and 5 |

## 2. Demo script (about 6 minutes)
1. **Problem** (30 s) — WhatsApp chaos, double-booking.
2. **Register** with a blank form → show inline errors → register properly.
3. **Home**: search "stat", tap the Programming chip, toggle "Available now". Open a tutor.
4. **Book** a session. Try a past time (error). Book a valid slot. Try the same slot again → conflict error.
5. **Sessions** tab: reschedule, then cancel (confirm dialog). Show Past tab and delete a record.
6. **Profile** → Add tutor profile (try rating `9` → error; duplicate subject → error), edit it, toggle availability, delete it.
7. **Resources**: pick a subject; turn on airplane mode, pick another → error state → turn off → "Try again".
8. **Reminders**: Profile → "Send test reminder" → notification appears.
9. **Persistence**: swipe the app away, reopen → still logged in, data intact.

## 3. GitHub plan (5–6 members)
Work for the whole semester, not the night before. Every member needs commits on their own branch and at least one merged PR.

| Member | Branch | Owns | Starter issues |
|---|---|---|---|
| 1 – Lead / Navigation & theme | `feature/navigation-theme` | `ui/theme`, `ui/components`, `ui/navigation`, `MainActivity` | #1 Project setup + CI, #2 Theme & components, #3 Bottom nav |
| 2 – Auth | `feature/auth` | `AuthRepository`, `AuthViewModel`, `AuthScreens`, `SessionManager`, `Validators` (auth) | #4 Register, #5 Login, #6 Session persistence |
| 3 – Database | `feature/database` | `Entities`, `Daos`, `AppDatabase`, seed data, `BookingDaoTest` | #7 Entities, #8 DAOs, #9 Seed data, #10 DAO tests |
| 4 – Tutors CRUD | `feature/tutors` | `Home*`, `TutorDetail*`, `TutorForm*`, `TutorRepository` | #11 Tutor list + search, #12 Tutor form, #13 Edit/delete |
| 5 – Booking | `feature/booking` | `BookSession*`, `Sessions*`, `BookingRepository` | #14 Book session, #15 Conflict rules, #16 Reschedule/cancel |
| 6 – API, WorkManager, Profile, docs | `feature/api-reminders` | `Resources*`, `OpenLibraryApi`, `work/`, `Profile*`, README, `ValidatorsTest` | #17 Retrofit API, #18 ReminderWorker, #19 Profile, #20 Unit tests |

Workflow: create issue → branch `feature/...` → small commits (`feat: add tutor form validation`, `fix: ...`, `test: ...`) → pull request → one teammate reviews → merge to `main`.
Tip: commit the project skeleton first, then let each member build their own part on their branch so the history shows real contribution.

## 4. Data flow example: "Confirm booking"
1. `BookSessionScreen` – user taps **Confirm booking** → calls `viewModel.confirm()`.
2. `BookSessionViewModel` – `Validators.sessionTime` checks the time is in the future; sets `saving = true`; launches a coroutine.
3. `BookingRepository.book` – checks tutor exists/available, not your own profile, no tutor or student overlap (`countTutorConflicts`, `countStudentConflicts`).
4. `BookingDao.insert` – Room runs `INSERT INTO bookings ...` and returns the new id.
5. Back in the ViewModel – `ReminderScheduler.schedule(id, start)` queues a WorkManager job; state `done = true`.
6. Screen – `LaunchedEffect(state.done)` navigates back. Because DAO queries return `Flow`, **Home banner and Sessions list update automatically**.

## 5. Defence Q&A
- **Why this architecture?** Separation of concerns: the UI only draws state; ViewModels survive rotation and hold logic; repositories hold business rules and hide where data comes from; easy to test and swap data sources.
- **Why Room?** Compile-time checked SQL, Flow support for live updates, relations and foreign keys, less boilerplate than raw SQLite.
- **How does CRUD work?** See section 4. Create = `@Insert`, Read = `@Query` returning `Flow`, Update = `@Update`, Delete = `@Delete`, all behind repository functions.
- **Invalid input?** `Validators` return messages; the ViewModel puts them in the UI state; `AppTextField` shows them under the field. Business errors (duplicate email, double booking) come back as `AppResult.Failure`.
- **API unavailable?** `ResourceRepository` catches `IOException` / `HttpException` and returns `Failure`; the UI shows `ErrorState` with a **Try again** button.
- **Why WorkManager?** The reminder must fire after the app is closed or the phone reboots, is battery-friendly, and needs no exact-alarm permission. A coroutine or `Thread` would die with the app.
- **Why Flow/StateFlow?** Streams of database changes; the UI re-renders automatically when data changes.
- **How are passwords stored?** SHA-256 hash of email+password, never plain text. (Production: server-side bcrypt/Argon2.)
- **What would you improve?** Cloud backend so tutors and students on different phones see the same data, push notifications, tutor ratings from students, chat, calendar sync, dark mode, UI tests.

## 6. Known limitations (be upfront)
- Data lives on each phone (local Room database) — there is no shared backend. The seed tutors illustrate the idea.
- Login is local only; "Forgot password" is not implemented.
- Light theme only.
