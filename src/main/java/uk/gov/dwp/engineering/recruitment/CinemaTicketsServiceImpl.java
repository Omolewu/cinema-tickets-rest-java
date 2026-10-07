package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

  private final PaymentService paymentService;

  private final SeatReservationService seatReservationService;

  public CinemaTicketsServiceImpl(PaymentService paymentService,
      SeatReservationService seatReservationService) {
    this.paymentService = paymentService;
    this.seatReservationService = seatReservationService;
  }

  @Override
  public BookingConfirmation purchaseTickets(final Long accountId,
      final TicketRequest... ticketRequests) throws InvalidBookingException {

    final var ticketCount = ticketRequests[0].ticketCount();
    final var totalAmount = new BigDecimal("25.99").multiply(BigDecimal.valueOf(ticketCount));

    paymentService.debitAccount(accountId, totalAmount);
    seatReservationService.reserveSeats(accountId, (long) ticketCount);

    return new BookingConfirmation(accountId);
  }
}
