# Vacation Request Management System

## Getting Started

### Build and Run the Project
It can take a while on first startup (around 10m depending on cpu and internet speed)
```bash
docker compose build
```
```bash
docker compose up -d
```
If first `docker compose up` fails due to health check, just run it one more time

### Swagger Documentation

Access the API documentation at:
```
http://localhost:9001/swagger-ui/index.html
```

## API Features

### Create Vacation Request

By default profiler service will fail on each 5th request
To test service failure scenarios, configure the request with one of the following conditions:

- **Accounting Service Failure**: Set `budget > 100`
- **Resources Service Failure**: Set time period where `periodTo - periodFrom > 25 days`
- **Profiler Service Failure**: Set `cvUuid` to `00000000-0000-0000-0000-000000000000`

#### Example Request (triggers all failures)

```json
{
  "cvUuid": "00000000-0000-0000-0000-000000000000",
  "periodFrom": "2026-01-01",
  "periodTo": "2026-01-27",
  "budget": 105
}
```

### Get Vacation Requests with Filtering

The filtering feature is implemented using JPA Specification, with queries generated at runtime.

#### Supported Filter Operations

- `>` - Greater than
- `<` - Less than
- `<>` - Not equal
- `<=` - Less than or equal
- `>=` - Greater than or equal
- `~` - LIKE operator (for string fields, e.g., `cvUuid`)

#### Additional Features

- Sorting by any field is supported

### Get Request State History

Returns a list of vacation request state history in chronological order, tracking all state transitions for a given request.

#### Response

Returns an ordered list of state changes including:
- State transition details
- Timestamp of each change
- Chronological ordering from oldest to newest