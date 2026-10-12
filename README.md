# Cinema Tickets REST API

A Spring Boot implementation of the Cinema Tickets coding exercise.

## Requirements

-   Java 21 or above
-   Maven 3.9+

## Running the application

```bash
mvn spring-boot:run
```

## Running the tests

Run unit and controller tests:

```bash
mvn test
```

Run the full verification lifecycle, including integration tests:

```bash
mvn clean verify
```

Integration tests are located under:

```text
src/integration-test/java/uk/gov/dwp/engineering/recruitment
```

and are executed by the Maven Failsafe Plugin.

## API

### POST /cinema/bookings

Example request:

```json
{
  "accountId": 1,
  "ticketRequests": [
    { "type": "ADULT", "ticketCount": 2 },
    { "type": "CHILD", "ticketCount": 1 }
  ]
}
```

**Successful response - `201 Created`:**

```json
{
  "accountId": 1
}
```

Invalid booking response - `400 Bad Request`:

```json
{
  "detail": "Child tickets cannot be purchased without adult tickets."
}
```

## Ticket Prices

| Type   | Price  |
|--------|--------|
| ADULT  | £25.99 |
| CHILD  | £17.50 |
| INFANT | £0.00  |

## Business Rules Implemented

-   A maximum of 25 tickets may be purchased in a single transaction, including infant tickets.
-   Adult tickets cost £25.99.
-   Child tickets cost £17.50.
-   Infant tickets are free.
-   Adult and child tickets require seats.
-   Infant tickets do not require seats and sit on an adult's lap.
-   Child tickets cannot be purchased without at least one adult ticket.
-   Each infant ticket requires an accompanying adult ticket, so infant ticket count cannot exceed adult ticket count.
-   Invalid account IDs are rejected.
-   Null or empty ticket requests are rejected.
-   Null ticket requests and null ticket types are rejected.
-   Zero or negative ticket quantities are rejected.
-   Validation is completed before any external service is called.
-   Payment is processed before seat reservation.

## Approach

The solution was implemented using Test-Driven Development.

Business rules were introduced through tests first, followed by the minimum implementation required to make the tests pass. The solution was then refactored while keeping the test suite green.

The implementation keeps responsibilities focused:

| Class                      | Responsibility                                                      |
| -------------------------- | ------------------------------------------------------------------- |
| `CinemaTicketsController`  | Exposes the REST endpoint and delegates booking requests            |
| `CinemaTicketsServiceImpl` | Orchestrates validation, payment, seat calculation and reservation  |
| `BookingValidator`         | Validates account, ticket limits and adult dependency rules         |
| `RestExceptionHandler`     | Converts invalid bookings into HTTP 400 responses                   |

External payment and seat reservation services are treated as supplied dependencies and are not modified.

## Testing Strategy

The test suite includes:

-   Focused unit tests for booking validation
-   Unit tests for payment and seat calculations
-   Controller tests for successful and rejected HTTP requests
-   Integration tests using the real Spring application context
-   Verification that invalid bookings do not call external services

The full build can be verified with:

```bash
mvn clean verify
```

## Assumptions and Ambiguities

The exercise states that an infant does not require a seat and sits on an adult's lap. I interpreted this as each infant requiring one accompanying adult. Therefore, the number of infant tickets cannot exceed the number of adult tickets.

All ticket types, including infants, count towards the maximum of 25 tickets per purchase.

Multiple `TicketRequest` entries of the same ticket type are permitted and are aggregated when calculating the total price, seat count and ticket limit.

Account IDs must be non-null and greater than zero.

Ticket quantities must be greater than zero.

The supplied assumptions state that the payment and seat reservation services succeed once called, so no additional failure-handling behaviour has been introduced for those services.

`BookingConfirmation` has been kept unchanged and minimal because the supplied contract is sufficient for the exercise.

The supplied exception handler returned HTTP 501 Not Implemented for `InvalidBookingException`. This was changed to HTTP 400 Bad Request because the exception represents an invalid client booking request rather than an unimplemented server capability.
