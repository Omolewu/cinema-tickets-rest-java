package uk.gov.dwp.engineering.recruitment;


import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
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

        int totalTicketCount = 0;
        boolean hasAdultTicket = false;
        boolean hasChildTicket = false;
        boolean hasInfantTicket = false;
        for (final var ticketRequest : ticketRequests) {
            if (ticketRequest == null) {
                throw new InvalidBookingException("Ticket request cannot be null.");
            }
            if (ticketRequest.ticketCount() <= 0) {
                throw new InvalidBookingException("Ticket count must be greater than zero.");
            }
            if (ticketRequest.ticketCount() > 25) {
                throw new InvalidBookingException("Ticket count cannot exceed 25.");
            }

            totalTicketCount += ticketRequest.ticketCount();

            if (ticketRequest.type() == TicketType.CHILD) {
                hasChildTicket = true;
            }

            if (ticketRequest.type() == TicketType.ADULT) {
                hasAdultTicket = true;
            }

            if (ticketRequest.type() == TicketType.INFANT) {
                hasInfantTicket = true;
            }
        }

        if (totalTicketCount > 25) {
            throw new InvalidBookingException("Total ticket count cannot exceed 25.");
        }


        if (hasChildTicket && !hasAdultTicket) {
            throw new InvalidBookingException(
                    "Child tickets cannot be purchased without adult tickets.");
        }
        if (hasInfantTicket && !hasAdultTicket) {
            throw new InvalidBookingException(
                    "Infant tickets cannot be purchased without adult tickets.");
        }

    }

}
