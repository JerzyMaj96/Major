# Major

A developer-focused task management system that keeps task statuses in sync with GitHub automatically. Push a branch named
`task-42-...` or open and merge a Pull Request, and the matching task moves across the board by itself. Every
connected browser sees the change instantly through WebSocket. The project also uses an LLM to draft task descriptions
and weekly progress summaries.

Master's thesis project (Computer Science, Vizja University). **Work in progress.**

[![CI](https://github.com/JerzyMaj96/Major/actions/workflows/ci.yml/badge.svg)](https://github.com/JerzyMaj96/Major/actions/workflows/ci.yml)

---

## Key Features

### 1. Automatic status synchronization (GitHub webhooks)
- Receives `push` and `pull_request` events on `POST /webhook/github`.
- The task ID is extracted from the branch name with the pattern `task-(\d+)`.
- A push moves the task to `IN_PROGRESS`; an opened Pull Request moves it to `IN_REVIEW`; a merged one moves it to `DONE`.
  A PR closed without merging leaves the status unchanged.
- **Signature verification:** every request is checked with HMAC-SHA256 (`X-Hub-Signature-256`). The comparison uses
  `MessageDigest.isEqual()` to prevent timing attacks.
- **Dead letter queue:** events that cannot be processed (for example a branch with no task ID) are stored with status
  `FAILED`, together with the original payload. They can be re-processed on demand through
  `POST /webhook-events/{id}/retry`. There is deliberately no automatic retry loop.

### 2. Real-time updates (WebSocket / STOMP)
- Every task status change is broadcast to `/topic/task-updates` (SockJS endpoint `/ws`).
- The React board subscribes to this topic, so a webhook-triggered change shows up without a page refresh.

### 3. Kanban board
- Four columns: `BACKLOG`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`.
- Drag and drop (`@dnd-kit/react`) changes the task status through the REST API.
- Task creation, a task details modal, and a global activity log page.

### 4. LLM-assisted writing (Spring AI + OpenAI)
- **Task descriptions:** generated from a title with `gpt-4o-mini`.
- **Weekly summary:** a `@Scheduled` job (Mondays, 09:00) produces a short narrative of last week's activity.
- **Division of responsibility:** all numbers (for example completed tasks) are computed deterministically in Java
  from database records. The LLM only turns the already computed data into prose, because LLMs are unreliable at counting.

### 5. Audit trail
- `ActivityLog` records every change: type, old value, new value, timestamp, and the author. Webhook-driven changes
  are attributed to the GitHub webhook rather than to a user.

### 6. Security
- Stateless JWT authentication (`jjwt`), a custom `JwtAuthenticationFilter`, BCrypt-hashed passwords.
- `AuthFacade` abstracts access to the current user from the `SecurityContext`.
- Central `@ControllerAdvice` maps domain exceptions to proper HTTP codes (401, 404, 409, 503, ...).
- CORS origin configurable through an environment variable.

---

## Architecture

```text
GitHub ──webhook──▶ GitHubWebhookController ──▶ WebhookEventService ──▶ TaskService ──▶ PostgreSQL
                         (HMAC check)               (parse, DLQ)            │
                                                                            ├──▶ ActivityLogService (audit)
                                                                            └──▶ SimpMessagingTemplate
                                                                                      │  /topic/task-updates
React SPA ◀──────────── STOMP over SockJS (/ws) ◀───────────────────────────────────┘
   │
   └── REST (JWT) ──▶ Task / Label / User / ActivityLog / WeeklySummary controllers
```

```text
major/
├── backend/             # Spring Boot application
│   └── src/main/java/com/jerzymaj/major/
│       ├── configuration/   # Security, JWT, CORS, WebSocket, API routes
│       ├── controllers/     # REST + webhook endpoints
│       ├── Dtos/            # request/response records, GitHub payload records
│       ├── exceptions/      # domain exceptions + global handler
│       ├── mappers/         # entity <-> DTO
│       ├── models/          # JPA entities and enums
│       ├── repos/           # Spring Data JPA repositories
│       ├── security/        # AuthFacade, UserDetailsService, GitHubSignatureVerifier
│       └── services/        # business logic (Task, Webhook, ActivityLog, Gpt, WeeklySummary, ...)
├── frontend/            # React + TypeScript + Vite
└── .github/workflows/   # CI
```

Domain model (six JPA entities): `User`, `Task`, `Label`, `ActivityLog`, `WebhookEvent`, `WeeklySummary`.

---

## Tech Stack

| Layer    | Technology                                                                                     |
|----------|------------------------------------------------------------------------------------------------|
| Backend  | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA (Hibernate), Spring WebSocket, Spring AI |
| Database | PostgreSQL 16 (Docker), H2 in-memory for tests                                                 |
| Auth     | JWT (`jjwt` 0.12), BCrypt                                                                      |
| AI       | OpenAI `gpt-4o-mini` through Spring AI                                                         |
| Frontend | React 19, TypeScript, Vite, React Router, MUI icons, `@dnd-kit/react`, `@stomp/stompjs` + SockJS |
| Testing  | JUnit 5, Mockito, MockMvc, Spring Security Test                                                |
| DevOps   | Docker (multi-stage build, non-root user), Docker Compose, GitHub Actions                      |

---

## Getting Started

### Prerequisites
- Java 21, Node.js 20+, Docker
- An OpenAI API key
- (optional) [ngrok](https://ngrok.com/) to receive GitHub webhooks on your local machine

### 1. Configure environment variables

Create `backend/.env`:

```properties
DB_USERNAME=major
DB_PASSWORD=change-me
OPENAI_API_KEY=sk-...
JWT_SECRET=a-long-random-string-of-at-least-32-characters
GITHUB_WEBHOOK_SECRET=another-random-string
# optional, defaults to http://localhost:5173
ALLOWED_ORIGIN=http://localhost:5173
```

Create `frontend/.env`:

```properties
VITE_API_BASE_URL=http://localhost:8080
```

### 2. Start the database

```bash
cd backend
docker compose up -d        # PostgreSQL on localhost:5433
```

### 3. Run the backend

```bash
cd backend
./mvnw spring-boot:run      # http://localhost:8080
```

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173
```

### 5. Connect GitHub (optional)

1. Expose the backend: `ngrok http 8080`.
2. In your GitHub repository go to **Settings → Webhooks → Add webhook**:
   - Payload URL: `https://<your-ngrok-id>.ngrok-free.app/webhook/github`
   - Content type: `application/json`
   - Secret: the value of `GITHUB_WEBHOOK_SECRET`
   - Events: **Pushes** and **Pull requests**
3. Create a task in Major (for example, ID `7`), then work on a branch named `task-7-my-feature`.

### Run with Docker

```bash
cd backend
docker build -t major-backend .
```

The image uses a multi-stage build (Maven build stage → slim JRE Alpine runtime) and runs as a non-root user.

---

## API Overview

Base path: `/major/api`. All endpoints require a JWT (`Authorization: Bearer <token>`) unless marked public.

| Method   | Endpoint                                  | Auth   | Description                          |
|----------|-------------------------------------------|--------|--------------------------------------|
| `POST`   | `/auth/login`                             | Public | Log in and receive a JWT             |
| `POST`   | `/users/register`                         | Public | Register a new user                  |
| `GET`    | `/users/me`                               | JWT    | Current user                         |
| `GET`    | `/users/{id}`                             | JWT    | User by ID                           |
| `DELETE` | `/users/delete-me`                        | JWT    | Delete the current user              |
| `POST`   | `/tasks`                                  | JWT    | Create a task                        |
| `GET`    | `/tasks`                                  | JWT    | List tasks                           |
| `GET`    | `/tasks/{taskId}`                         | JWT    | Task by ID                           |
| `PATCH`  | `/tasks/{taskId}`                         | JWT    | Update a task                        |
| `DELETE` | `/tasks/{taskId}`                         | JWT    | Delete a task                        |
| `PATCH`  | `/tasks/{taskId}/status`                  | JWT    | Change status (`?taskStatus=`)           |
| `PATCH`  | `/tasks/{taskId}/assignees/{assigneeId}`  | JWT    | Add an assignee                      |
| `DELETE` | `/tasks/{taskId}/assignees/{assigneeId}`  | JWT    | Remove an assignee                   |
| `PATCH`  | `/tasks/{taskId}/labels/{labelId}`        | JWT    | Add a label                          |
| `DELETE` | `/tasks/{taskId}/labels/{labelId}`        | JWT    | Remove a label                       |
| `GET`    | `/tasks/{taskId}/activity-logs`           | JWT    | Activity history of a task           |
| `GET`    | `/activity-logs`                          | JWT    | Global activity log                  |
| `POST`   | `/labels`                                 | JWT    | Create a label                       |
| `GET`    | `/labels`, `/labels/{labelId}`            | JWT    | List / get labels                    |
| `PATCH`  | `/labels/{labelId}`                       | JWT    | Update a label                       |
| `DELETE` | `/labels/{labelId}`                       | JWT    | Delete a label                       |
| `POST`   | `/gpt/generate-description`               | JWT    | Generate a task description (LLM)    |
| `GET`    | `/webhook-events`                         | JWT    | List webhook events (incl. failed)   |
| `POST`   | `/webhook-events/{id}/retry`              | JWT    | Re-process a failed event            |
| `GET`    | `/weekly-summary/last-week`               | JWT    | Last week's summary                  |
| `GET`    | `/weekly-summary/all`                     | JWT    | All summaries                        |
| `POST`   | `/webhook/github` *(no `/major/api` prefix)* | HMAC | GitHub webhook receiver           |

**WebSocket:** connect to `/ws` (SockJS) and subscribe to `/topic/task-updates`.

---

## Testing & CI

```bash
cd backend
./mvnw verify
```

- **Unit tests** (Mockito) for the Task, Label, User, WebhookEvent and WeeklySummary services. Mocks echo the argument
  they receive (`thenAnswer` / `ArgumentCaptor`) so tests cannot pass against a hard-coded return value.
- **Integration tests** (MockMvc + H2) for every controller, with a custom `@WithMockCustomUser` security annotation.
  Tests never assume fixed IDs; they read generated IDs back from saved entities.
- **GitHub Actions** runs `mvn -B verify` on Java 21 for every push and pull request.

---

## Technical Challenges & Solutions

- **Fragile `equals`/`hashCode`.** Using the database-generated `id` broke `HashSet` membership once Hibernate assigned
  the ID after save. Moved to a stable business field (or a Java-generated `UUID`).
- **Unsafe principal cast.** `(User) getPrincipal()` failed with `ClassCastException` for anonymous requests. Replaced
  with an `instanceof` check and a dedicated `UnauthorizedException` mapped to 401.
- **Wrong HTTP status on failed login** (500 → 403 → 401), caused by importing `AuthenticationException` from
  `javax.naming` instead of Spring Security.
- **One status-change logic for two contexts.** An overloaded method takes an explicit `changedBy` argument. Controllers
  use the logged-in user, while the webhook passes a "GitHub webhook" source, with no duplicated audit or broadcast code.
- **Webhook security.** HMAC-SHA256 verification with a constant-time comparison.
- **Resilience without retry storms.** A dead letter queue with manual retry instead of looping retries on bad data.

---

## Roadmap

- [x] Frontend: webhook event (dead letter queue) view with a retry button
- [x] Frontend: weekly summary view
- [x] `@Async` + `CompletableFuture` for weekly summary generation
- [x] Test coverage report (JaCoCo) in CI
- [ ] Full-stack `docker-compose` (backend + frontend + database)
- [ ] Future work (outside the thesis scope): caching, OAuth2, two-factor authentication, database migrations (Flyway)

---

## License

See [LICENSE](LICENSE).
