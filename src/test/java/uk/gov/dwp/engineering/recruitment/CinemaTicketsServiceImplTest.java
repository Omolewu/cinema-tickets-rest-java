package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@DisplayName("Cinema Tickets Service Implementation Test")
@ExtendWith(MockitoExtension.class)
class CinemaTicketsServiceImplTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private SeatReservationService seatReservationService;

    private CinemaTicketsService cinemaTicketsService;

    @BeforeEach
    void setUp() {
        cinemaTicketsService = new CinemaTicketsServiceImpl(paymentService, seatReservationService);
    }

    @Test
    @DisplayName("should make payment and reserve seats when purchasing one adult ticket")
    void shouldPurchaseOneAdultTicket() {
        final Long accountId = 1L;
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 1);
        final var confirmation =
                cinemaTicketsService.purchaseTickets(accountId, adultTicketRequest);

        verify(paymentService).debitAccount(accountId, new BigDecimal("25.99"));
        verify(seatReservationService).reserveSeats(accountId, 1L);
        assertEquals(accountId, confirmation.accountId());
    }

    @Test
    @DisplayName("should calculate payment and seats for multiple adult tickets")
    void shouldPurchaseMultipleAdultTickets() {
        final Long accountId = 1L;
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 2);
        final var confirmation =
                cinemaTicketsService.purchaseTickets(accountId, adultTicketRequest);

        verify(paymentService).debitAccount(accountId, new BigDecimal("51.98"));
        verify(seatReservationService).reserveSeats(accountId, 2L);
        assertEquals(accountId, confirmation.accountId());
    }

    @Test
    @DisplayName("Should pay 43.49 pounds and reserve 2 seats for one adult and one child ticket")
    void shouldCalculatePaymentAndSeatsForAdultAndChildTickets() {
        final Long accountId = 1L;
        final var adultTicketRequest = new TicketRequest(TicketType.ADULT, 1);
        final var childTicketRequest = new TicketRequest(TicketType.CHILD, 1);
        
        final var confirmation = cinemaTicketsService.purchaseTickets(accountId, adultTicketRequest,
                childTicketRequest);

        verify(paymentService).debitAccount(accountId, new BigDecimal("43.49"));
        verify(seatReservationService).reserveSeats(accountId, 2L);
        assertEquals(accountId, confirmation.accountId());
    }



}
