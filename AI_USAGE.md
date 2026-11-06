# AI Tool Usage (FitStreak – Part 3)

**Tool used:** Claude Sonnet 5.5, Anthropic's AI assistant, used through the Claude app.

**Reference:** Anthropic. 2026. *Claude Sonnet 5.5* [Large language model]. Available at: https://claude.ai [Accessed 5 November 2026].

## How AI was used

| Area | How Claude helped | What I did myself |
|---|---|---|
| Planning | Turned the Part 3 brief and marking rubric into a step-by-step checklist. | Chose which features to build and reviewed every step against the rubric. |
| Single sign-on | Suggested Google sign-in with Firebase Authentication and the Credential Manager library, and drafted the `LoginActivity` code and the PHP `/api/sso` endpoint. | Created the Firebase project, registered my SHA-1 keys, and tested sign-in on my own phone. |
| Offline mode | Drafted the Room `pending_logs` table, `SyncManager` and the network callback that uploads queued workouts. | Tested by switching on flight mode, finishing a workout, and reconnecting. |
| Push notifications | Drafted the Firebase Cloud Messaging service and the notification channel code. | Sent test messages from the Firebase console and checked Logcat. |
| Multi-language | Translated the interface strings into Afrikaans and isiZulu. | Checked the translations on screen and corrected wording. |
| Workout History and Weekly Goal | Drafted the screens, API endpoints and layouts. | Tested them and adjusted the design. |
| Unit tests and CI | Drafted the unit tests and the GitHub Actions workflow. | Ran the tests, fixed failures, and checked that the Actions run was green. |
| Debugging | I pasted Gradle and Logcat errors into Claude and used its explanations to fix them. | Applied and re-tested each fix. |
| README and release notes | Helped structure the documents. | Rewrote them in my own words and added my own screenshots. |

## Citation in the code
Code that was AI-assisted is marked with comments such as `// PART 3` and is explained in comments and Logcat messages. I read and tested all AI-generated code before committing it.

## What was not AI-generated
The app idea, the database design, the streak/XP rules and the screen flow come from my Part 1 research and planning document. The demo video, the screenshots, the Firebase and Play Store set-up, and all testing on a real phone were done by me.

## Limitations I noticed
AI-generated code did not always compile first time and its translations needed checking. I treated everything it produced as a draft that I had to verify.
