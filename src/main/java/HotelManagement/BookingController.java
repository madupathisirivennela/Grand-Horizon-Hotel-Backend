
package HotelManagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookingController {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    public BookingController(
            BookingRepository bookingRepository,
            CustomerRepository customerRepository) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/book")
    public String createBooking(
            @RequestParam LocalDate checkin,
            @RequestParam LocalDate checkout,
            @RequestParam String room,
            @RequestParam Integer guests,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                    .noneMatch(a -> a.getAuthority()
                        .equals("ROLE_CUSTOMER"))) {
            return "redirect:/login";
        }

        Customer customer = customerRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElse(null);

        if (customer == null) {
            redirectAttributes.addFlashAttribute(
                    "bookingError",
                    "Customer account not found. Please log in again.");
            return "redirect:/login";
        }

        if (checkin.isBefore(LocalDate.now())
                || !checkout.isAfter(checkin)
                || guests < 1
                || guests > 10) {
            redirectAttributes.addFlashAttribute(
                    "bookingError",
                    "Please check your dates and number of guests.");
            return "redirect:/#booking";
        }

        if (!List.of(
                "Deluxe Room",
                "Executive Room",
                "Presidential Suite"
        ).contains(room)) {
            redirectAttributes.addFlashAttribute(
                    "bookingError",
                    "Please select a valid room type.");
            return "redirect:/#booking";
        }

        boolean conflict = bookingRepository
                .existsByRoomAndCheckinLessThanAndCheckoutGreaterThanAndStatusIn(
                        room,
                        checkout,
                        checkin,
                        List.of("PENDING", "CONFIRMED"));

        if (conflict) {
            redirectAttributes.addFlashAttribute(
                    "bookingError",
                    "This room type is already booked for some of those dates. Please choose other dates.");
            return "redirect:/#booking";
        }

        Booking booking = new Booking();
        booking.setName(customer.getName());
        booking.setEmail(customer.getEmail());
        booking.setCheckin(checkin);
        booking.setCheckout(checkout);
        booking.setRoom(room);
        booking.setGuests(guests);
        booking.setStatus("PENDING");
        booking.setCustomer(customer);

        bookingRepository.save(booking);

        redirectAttributes.addFlashAttribute(
                "bookingSuccess",
                "Your booking request has been saved successfully and is pending confirmation.");

        return "redirect:/#booking";
    }
}

