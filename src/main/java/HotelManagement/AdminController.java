
package HotelManagement;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final BookingRepository bookingRepository;

    public AdminController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "admin";
    }

    @PostMapping("/admin/bookings/{id}/confirm")
    public String confirmBooking(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        return bookingRepository.findById(id).map(booking -> {
            if (!"CANCELLED".equals(booking.getStatus())) {
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                redirectAttributes.addFlashAttribute(
                        "message", "Booking confirmed successfully.");
            }
            return "redirect:/admin";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute(
                    "error", "Booking not found.");
            return "redirect:/admin";
        });
    }

    @PostMapping("/admin/bookings/{id}/cancel")
    public String cancelBooking(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            redirectAttributes.addFlashAttribute(
                    "message", "Booking cancelled successfully.");
            return "redirect:/admin";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute(
                    "error", "Booking not found.");
            return "redirect:/admin";
        });
    }
}

