package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    @DisplayName("Ticket Request validation")
    class TicketRequestsValidationTests {

        @Test
        @DisplayName("should throw exception when no ticket requests are provided")
        void shouldThrowExceptionWhenNoTicketRequestsAreProvided() {
            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID));
        }

        @Test
        @DisplayName("should throw exception when ticket requests are null")
        void shouldThrowExceptionWhenTicketRequestsAreNull() {
            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, (TicketRequest[]) null));
        }

        @Test
        @DisplayName("should throw exception when any ticket request is null")
        void shouldThrowExceptionWhenAnyTicketRequestIsNull() {
            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, (TicketRequest) null));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @DisplayName("should throw exception when ticket count is zero or negative")
        void shouldThrowExceptionWhenTicketCountIsZeroOrNegative(final int ticketCount) {
            final var ticketRequest = new TicketRequest(TicketType.ADULT, ticketCount);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, ticketRequest));
        }

        @Test
        @DisplayName("should throw exception when any ticket category count exceeds 25 tickets")
        void shouldThrowExceptionWhenAnyTicketCategoryCountExceeds25Tickets() {
            final var ticketRequest = new TicketRequest(TicketType.ADULT, 26);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, ticketRequest));
        }

        @Test
        @DisplayName("should throw exception when total ticket count exceeds 25 tickets")
        void shouldThrowExceptionWhenTotalTicketCountExceeds25Tickets() {
            final var adultRequest = new TicketRequest(TicketType.ADULT, 15);
            final var childRequest = new TicketRequest(TicketType.CHILD, 11);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, adultRequest, childRequest));
        }

    }

    @Nested
    @DisplayName("Adult Dependency Validation Tests")
    class AdultDependencyValidationTests {

        @Test
        @DisplayName("Should throw exception when child tickets are purchased without adult tickets")
        void shouldThrowExceptionWhenChildTicketsWithoutAdultTickets() {
            final var childRequest = new TicketRequest(TicketType.CHILD, 1);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, childRequest));
        }

        @Test
        @DisplayName("Should throw exception when infant tickets are purchased without adult tickets")
        void shouldThrowExceptionWhenInfantTicketsWithoutAdultTickets() {
            final var infantRequest = new TicketRequest(TicketType.INFANT, 1);

            assertThrows(InvalidBookingException.class,
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, infantRequest));
        }

    }

    @Nested
    @DisplayName("Valid Booking Scenarios")
    class ValidBookingScenariosTests {

        @Test
        @DisplayName("should allow an adult ticket purchase")
        void shouldAllowAdultTicketPurchase() {
            final var adultRequest = new TicketRequest(TicketType.ADULT, 2);

            assertDoesNotThrow(() -> bookingValidator.validate(VALID_ACCOUNT_ID, adultRequest));
        }

        @Test
        @DisplayName("should allow child tickets when adult tickets are present")
        void shouldAllowChildTicketsWhenAdultTicketsArePresent() {
            final var adultRequest = new TicketRequest(TicketType.ADULT, 2);
            final var childRequest = new TicketRequest(TicketType.CHILD, 1);

            assertDoesNotThrow(
                    () -> bookingValidator.validate(VALID_ACCOUNT_ID, adultRequest, childRequest));
        }

    }
}
