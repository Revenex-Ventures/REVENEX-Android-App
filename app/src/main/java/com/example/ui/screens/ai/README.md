# screens/ai/

Gemini AI assistant integration.

## Files

### `AiAssistantScreen.kt`

A conversational AI assistant powered by Firebase AI (Gemini).

- Chat-style UI with a message list and a text input field.
- Sends user messages to the Gemini model via the Firebase AI SDK.
- The assistant is context-aware of the user's role — Principal queries can ask about school metrics, teachers can ask for lesson plan help, students can ask academic questions.
- Responses are streamed token-by-token for a real-time feel.
- Chat history is maintained in-session (not persisted to Firestore).

**Dependency:** Requires `firebase-ai` included via Firebase BOM 34.17.0.

Accessible to: All roles.
