package uk.gov.dwp.engineering.recruitment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.exception.RestExceptionHandler;

@DisplayName("Cinema Tickets Controller Test")
@ExtendWith(MockitoExtension.class)
class CinemaTicketsControllerTest {

    @Mock
    private CinemaTicketsService cinemaTicketsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        final var controller = new CinemaTicketsController(cinemaTicketsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler()).build();

    }

    @Test
    @DisplayName("should return bad request when booking is invalid")
    void shouldReturnBadRequestWhenBookingIsInvalid() throws Exception {
        final var errorMessage = "Child tickets cannot be purchased without adult tickets.";

        when(cinemaTicketsService.purchaseTickets(eq(1L), any(TicketRequest[].class)))
                .thenThrow(new InvalidBookingException(errorMessage));

        mockMvc.perform(post("/cinema/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                {
                    "accountId": 1,
                    "ticketRequests": [
                        {
                            "type": "CHILD",
                            "ticketCount": 1
                        }
                    ]
                }
                """)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(errorMessage));

    }

    @Test
    @DisplayName("should return created with booking confirmation for a valid booking")
    void shouldReturnCreatedWithBookingConfirmationForValidBooking() throws Exception {
        final var bookingConfirmation = new BookingConfirmation(1L);

        when(cinemaTicketsService.purchaseTickets(eq(1L), any(TicketRequest[].class)))
                .thenReturn(bookingConfirmation);

        mockMvc.perform(post("/cinema/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "accountId": 1,
                  "ticketRequests": [
                    {
                      "type": "ADULT",
                      "ticketCount": 1
                    }
                  ]
                }
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.accountId").value(1));
    }

    @Test
    @DisplayName("should return bad request when account ID is invalid")
    void shouldReturnBadRequestWhenAccountIdIsInvalid() throws Exception {
        final var errorMessage = "Account ID must be greater than zero.";

        when(cinemaTicketsService.purchaseTickets(eq(0L), any(TicketRequest[].class)))
                .thenThrow(new InvalidBookingException(errorMessage));

        mockMvc.perform(post("/cinema/bookings").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "accountId": 0,
                  "ticketRequests": [
                    {
                      "type": "ADULT",
                      "ticketCount": 1
                    }
                  ]
                }
                """)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(errorMessage));
    }

}
