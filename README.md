# UTS DPM

Backend for UTS DPM. Frontend can be found [here](https://github.com/airfork/uts-dpm-frontend).

This is a Spring Boot app built with Maven, and be run/built using the maven wrapper.
Database scripts are in [db_scripts](/db_scripts) and database settings go in [src/main/resources](/src/main/resources).
There are a few secrets that are placed in an applications.properties file in the [config](config) directory.
There is an [application.properties.example](config/application.properties.example) file that holds the expected structure.

Application is hosted on Heroku

When running locally, swagger docs can be accessed by visiting: http://localhost:8080/swagger-ui/index.html

## Running the Application

### Setup

1. Copy the environment template:
   ```bash
   cp .env.example .env
   ```

2. Fill in the values in `.env` with your credentials

### Start the Server

```bash
# Start with local profile (default)
./start.sh

# Start with a specific profile
./start.sh prod
```

The start script reads `.env` and passes the values as JVM system properties to Spring Boot.

## Running Tests

Tests use Testcontainers with PostgreSQL. Docker must be running.

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AutogenServiceTest

# Run multiple test classes
./mvnw test -Dtest=AutogenServiceTest,MockShiftProviderTest
```

## Autogen Mock Mode

The autogen endpoint can operate in mock mode for local development, bypassing When2Work API calls.

### Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.autogen-mock-enabled` | `false` | Enable mock mode |

In `application-local.properties`, mock mode is enabled by default:
```properties
app.autogen-mock-enabled=true
```

### Behavior

When enabled:
- A warning is logged at startup
- `getAssignedShifts()` returns mock data using real users from the database
- Each user gets a mock shift with cycling W2WColor assignments
- No external API calls are made

When disabled:
- Real When2Work API calls are made
- Requires valid `app.w2wKey` configuration
