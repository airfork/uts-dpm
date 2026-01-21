# Test Suite

Integration and unit tests for UTS-DPM services and controllers.

## Index

| File | Contents (WHAT) | Read When (WHEN) |
|------|-----------------|------------------|
| `BaseIntegrationTest.kt` | Test base class with DB access and entity creation helpers | Extending test suite, creating new integration tests |
| `UtsDpmBackendApplicationTests.kt` | Application context load test | Debugging Spring Boot configuration issues |
| `README.md` | Testing architecture, patterns, conventions | Understanding test structure, writing new tests |

### config/

| File | Contents (WHAT) | Read When (WHEN) |
|------|-----------------|------------------|
| `TestContainersConfig.kt` | PostgreSQL container configuration for tests | Debugging database test setup, container issues |

### controllers/

| File | Contents (WHAT) | Read When (WHEN) |
|------|-----------------|------------------|
| `AuthControllerTest.kt` | Login, password change endpoint tests (@WebMvcTest) | Testing authentication endpoints, JWT flows |
| `AutogenControllerTest.kt` | Autogen retrieval and submission endpoint tests | Testing autogen feature endpoints |
| `DataGenControllerTest.kt` | Spreadsheet generation endpoint tests | Testing data export functionality |
| `DpmControllerTest.kt` | DPM CRUD and group management endpoint tests | Testing DPM endpoints, role-based access |
| `UserControllerTest.kt` | User CRUD and management endpoint tests | Testing user endpoints, authorization |

### models/

| File | Contents (WHAT) | Read When (WHEN) |
|------|-----------------|------------------|
| `AutogenDpmTest.kt` | AutogenDpm description parsing unit tests | Testing autogen shift description logic |

### services/

| File | Contents (WHAT) | Read When (WHEN) |
|------|-----------------|------------------|
| `AuthServiceTest.kt` | Authentication, current user, password change service tests | Testing auth logic, troubleshooting login issues |
| `AutogenServiceTest.kt` | Shift filtering, DPM generation service tests | Testing autogen business logic |
| `DpmServiceTest.kt` | DPM/group CRUD service tests | Testing DPM management logic |
| `MockShiftProviderTest.kt` | Mock shift provider unit tests | Testing autogen mock mode |
| `UserDpmServiceTest.kt` | User DPM retrieval, approval, role-based access tests | Testing DPM approval workflows |
| `UserServiceTest.kt` | User CRUD, fullTime transition, email trigger tests | Testing user management logic |
