package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@DisplayName("Cinema Tickets Service Implementation")
@ExtendWith(MockitoExtension.class)
class CinemaTicketsServiceImplTest {
    private static final Long VALID_ACCOUNT_ID = 1L;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private CinemaTicketsService cinemaTicketsService;

    @BeforeEach
    void setUp() {
        cinemaTicketsService = new CinemaTicketsServiceImpl(paymentService, seatReservationService,
                new BookingValidator());
    }

    @Test
    @DisplayName("should calculate payment and seats for one adult ticket")
    void shouldCalculatePaymentAndSeatsForOneAdultTicket() {
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 1);
        final var confirmation =
                cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, adultTicketRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("25.99"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 1L);
        assertEquals(VALID_ACCOUNT_ID, confirmation.accountId());
    }

    @Test
    @DisplayName("should calculate payment and seats for multiple adult tickets")
    void shouldCalculatePaymentAndSeatsForMultipleAdultTickets() {
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 2);
        final var confirmation =
                cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, adultTicketRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("51.98"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 2L);
        assertEquals(VALID_ACCOUNT_ID, confirmation.accountId());
    }

    @Test
    @DisplayName("should calculate payment and seats for one adult and one child ticket")
    void shouldCalculatePaymentAndSeatsForAdultAndChildTickets() {
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 1);
        final var childTicketRequest = new TicketRequest(TicketType.CHILD, 1);
        final var confirmation = cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID,
                adultTicketRequest, childTicketRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("43.49"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 2L);
        assertEquals(VALID_ACCOUNT_ID, confirmation.accountId());
    }

    @Test
    @DisplayName("should reject invalid booking before payment or seat reservation")
    void shouldRejectInvalidBookingBeforePaymentOrSeatReservation() {
        final var childTicketRequest = new TicketRequest(TicketType.CHILD, 1);

        assertThrows(InvalidBookingException.class,
                () -> cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, childTicketRequest));
        verifyNoInteractions(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("should charge only for adult tickets and reserve one seat for an adult and infant")
    void shouldCalculatePaymentAndSeatsForAdultAndInfantTickets() {
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 1);
        final var infantTicketRequest = new TicketRequest(TicketType.INFANT, 1);
        final var confirmation = cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID,
                adultTicketRequest, infantTicketRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("25.99"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 1L);
        assertEquals(VALID_ACCOUNT_ID, confirmation.accountId());
    }

    @Test
    @DisplayName("should calculate payment and seats for mixed ticket types")
    void shouldCalculatePaymentAndSeatsForMixedTicketTypes() {
        final var adultRequest = new TicketRequest(TicketType.ADULT, 2);
        final var childRequest = new TicketRequest(TicketType.CHILD, 2);
        final var infantRequest = new TicketRequest(TicketType.INFANT, 1);

        cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, adultRequest, childRequest,
                infantRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("86.98"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 4L);
    }

    @Test
    @DisplayName("should aggregate multiple requests of the same ticket type")
    void shouldAggregateMultipleRequestsOfSameTicketType() {
        final var firstAdultRequest = new TicketRequest(TicketType.ADULT, 1);
        final var secondAdultRequest = new TicketRequest(TicketType.ADULT, 2);

        cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, firstAdultRequest,
                secondAdultRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("77.97"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 3L);
    }
}
