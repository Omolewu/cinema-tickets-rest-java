package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

@DisplayName("Booking Validator Test")
class BookingValidatorTest {
    private static final Long VALID_ACCOUNT_ID = 1L;

    private BookingValidator bookingValidator;

    @BeforeEach
    void setUp() {
        bookingValidator = new BookingValidator();
    }

    @Nested
    @DisplayName("Account ID validation")
    class AccountIdValidationTests {

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("should throw exception when account ID is zero or negative")
        void shouldThrowExceptionWhenAccountIdIsZeroOrNegative(final Long accountId) {
            final var ticketRequest = new TicketRequest(TicketType.ADULT, 1);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(accountId, ticketRequest));
        }

        @Test
        @DisplayName("should throw exception when account ID is null")
        void shouldThrowExceptionWhenAccountIdIsNull() {
            final var ticketRequest = new TicketRequest(TicketType.ADULT, 1);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(null, ticketRequest));
        }
    }

    @Nested
    @DisplayName("Ticket Requests validation")
    class TicketRequestsValidationTests {

        @Test
        @DisplayName("should throw exception when no ticket requests are provided")
        void shouldThrowExceptionWhenNoTicketRequestsAreProvided() {
            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID));
        }

    }

}
