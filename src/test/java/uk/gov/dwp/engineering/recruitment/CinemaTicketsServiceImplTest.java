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

@DisplayName("Cinema Tickets Service Implementation Test")
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
    @DisplayName("should make payment and reserve seats when purchasing one adult ticket")
    void shouldPurchaseOneAdultTicket() {

        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 1);
        final var confirmation =
                cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, adultTicketRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("25.99"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 1L);
        assertEquals(VALID_ACCOUNT_ID, confirmation.accountId());
    }

    @Test
    @DisplayName("should calculate payment and seats for multiple adult tickets")
    void shouldPurchaseMultipleAdultTickets() {

        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 2);
        final var confirmation =
                cinemaTicketsService.purchaseTickets(VALID_ACCOUNT_ID, adultTicketRequest);

        verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("51.98"));
        verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 2L);
        assertEquals(VALID_ACCOUNT_ID, confirmation.accountId());
    }

    @Test
    @DisplayName("Should pay 43.49 pounds and reserve 2 seats for one adult and one child ticket")
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



}
