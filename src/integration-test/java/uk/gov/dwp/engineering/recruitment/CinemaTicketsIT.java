package uk.gov.dwp.engineering.recruitment;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Cinema Tickets Integration")
class CinemaTicketsIT {

  private static final Long VALID_ACCOUNT_ID = 1L;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PaymentService paymentService;

  @MockitoBean
  private SeatReservationService seatReservationService;

  @Test
  @DisplayName("should reject child booking without an adult")
  void shouldRejectChildBookingWithoutAdult() throws Exception {
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
        """)).andExpect(status().isBadRequest()).andExpect(
        jsonPath("$.detail").value("Child tickets cannot be purchased without adult tickets."));
    verifyNoInteractions(paymentService, seatReservationService);
  }

  @Test
  @DisplayName("should process a valid booking and call external services with correct values")
  void shouldProcessValidBooking() throws Exception {
    mockMvc.perform(post("/cinema/bookings").contentType(MediaType.APPLICATION_JSON).content("""
        {
          "accountId": 1,
          "ticketRequests": [
            {
              "type": "ADULT",
              "ticketCount": 1
            },
            {
              "type": "CHILD",
              "ticketCount": 1
            }
          ]
        }
        """)).andExpect(status().isCreated()).andExpect(jsonPath("$.accountId").value(1));

    verify(paymentService).debitAccount(VALID_ACCOUNT_ID, new BigDecimal("43.49"));
    verify(seatReservationService).reserveSeats(VALID_ACCOUNT_ID, 2L);
  }
}
