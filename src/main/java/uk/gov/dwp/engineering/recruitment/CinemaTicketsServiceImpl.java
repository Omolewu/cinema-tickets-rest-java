package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

  private final PaymentService paymentService;

  private final SeatReservationService seatReservationService;

  private final BookingValidator bookingValidator;

  private static final BigDecimal ADULT_TICKET_PRICE = new BigDecimal("25.99");

  private static final BigDecimal CHILD_TICKET_PRICE = new BigDecimal("17.50");

  public CinemaTicketsServiceImpl(
   final  PaymentService paymentService,
   final  SeatReservationService seatReservationService, 
   final BookingValidator bookingValidator
  ) {
    this.paymentService = paymentService;
    this.seatReservationService = seatReservationService;
    this.bookingValidator = bookingValidator;
  }

  @Override
  public BookingConfirmation purchaseTickets(
    final Long accountId,
    final TicketRequest... ticketRequests
    ) throws InvalidBookingException {

    bookingValidator.validate(accountId, ticketRequests);
    var totalAmount = BigDecimal.ZERO;
    long totalSeats = 0L;

    for (final var ticketRequest : ticketRequests) {
      final var ticketPrice = switch (ticketRequest.type()) {
        case ADULT -> ADULT_TICKET_PRICE;
        case CHILD -> CHILD_TICKET_PRICE;
        case INFANT -> BigDecimal.ZERO;
      };
      totalAmount =
          totalAmount.add(ticketPrice.multiply(BigDecimal.valueOf(ticketRequest.ticketCount())));
      if (ticketRequest.type() != TicketType.INFANT) {
        totalSeats += ticketRequest.ticketCount();
      }
    }

    paymentService.debitAccount(accountId, totalAmount);
    seatReservationService.reserveSeats(accountId, totalSeats);

    return new BookingConfirmation(accountId);
  }
}
