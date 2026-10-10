package uk.gov.dwp.engineering.recruitment;

import org.springframework.stereotype.Component;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

@Component 
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
        int adultTicketCount = 0;
        int infantTicketCount = 0;
        int childTicketCount = 0;

        for (final var ticketRequest : ticketRequests) {
            if (ticketRequest == null) {
                throw new InvalidBookingException("Ticket request cannot be null.");
            }

            if (ticketRequest.type() == null) {
                throw new InvalidBookingException("Ticket type cannot be null.");
            }

            if (ticketRequest.ticketCount() <= 0) {
                throw new InvalidBookingException("Ticket count must be greater than zero.");
            }

            totalTicketCount += ticketRequest.ticketCount();

            switch (ticketRequest.type()) {
                case ADULT -> adultTicketCount += ticketRequest.ticketCount();
                case CHILD -> childTicketCount += ticketRequest.ticketCount();
                case INFANT -> infantTicketCount += ticketRequest.ticketCount();
            }
        }

        if (totalTicketCount > 25) {
            throw new InvalidBookingException("Total ticket count cannot exceed 25.");
        }

        if (infantTicketCount > adultTicketCount) {
            throw new InvalidBookingException(
                    "Each infant ticket requires an accompanying adult ticket.");
        }

        if (childTicketCount > 0 && adultTicketCount == 0) {
            throw new InvalidBookingException(
                    "Child tickets cannot be purchased without adult tickets.");
        }

    }



}
