# Test Credentials (Auto-created on startup)

These credentials are seeded automatically by `DataInitializer.java` when the backend starts.
Idempotent — safe to restart, will not duplicate.

## School Code: `demo`

Enter this on the Flutter login screen as the school code.

| Role | Email | Password | Name |
|------|-------|----------|------|
| SCHOOL ADMIN | admin@demo.com | Admin@123 | Demo School Admin |
| TEACHER | teacher@demo.com | Teacher@123 | Mrs. Priya Sharma |
| STUDENT | student@demo.com | Student@123 | Rahul Kumar (Class 7 - A) |

## Super Admin (Platform level)

| Role | Email | Password |
|------|-------|----------|
| SUPER_ADMIN | superadmin@platform.com | SuperAdmin@123 |

Toggle "Platform Admin" on the login page to use this account.

## Testing the Full Flow

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `flutter run -d chrome`
3. Login as **teacher@demo.com** → Homework → Assign to **Class 7 - A**
4. Logout → Login as **student@demo.com** → See homework → Tap **Ask AI**
5. For AI to work: `ollama pull llama3 && ollama serve`

AI is pre-configured: Ollama provider, all 3 modes (Tutor/Solve/Practice) enabled, 50 questions/day limit.
