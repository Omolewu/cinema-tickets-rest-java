package uk.gov.dwp.engineering.recruitment;

import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

class BookingValidator {

    void validate(final Long accountId, final TicketRequest... ticketRequests)
            throws InvalidBookingException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidBookingException("Account ID must be greater than zero.");
        }

    }

}
