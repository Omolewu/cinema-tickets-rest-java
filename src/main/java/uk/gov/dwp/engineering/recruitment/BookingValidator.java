package uk.gov.dwp.engineering.recruitment;

import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

class BookingValidator {

    void validate(final Long accountId, final TicketRequest... ticketRequests)
            throws InvalidBookingException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidBookingException("Account ID must be greater than zero.");
        }

        if (ticketRequests == null || ticketRequests.length == 0) {
            throw new InvalidBookingException("At least one ticket request must be provided.");
        }

        for (final var ticketRequest : ticketRequests) {
            if (ticketRequest == null) {
                throw new InvalidBookingException("Ticket request cannot be null.");
            }
            if (ticketRequest.ticketCount() <= 0) {
                throw new InvalidBookingException("Ticket count must be greater than zero.");
            }
        }

    }

}
