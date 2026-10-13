
package HotelManagement;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerController(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerCustomer(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        name = name.trim();
        email = email.trim().toLowerCase();

        if (name.isBlank() || name.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || email.length() > 150
                || password.length() < 8
                || password.length() > 72) {

            model.addAttribute("error",
                    "Enter a valid name and email. Password must be 8–72 characters.");
            return "register";
        }

        if (customerRepository.existsByEmailIgnoreCase(email)) {
            model.addAttribute("error",
                    "An account with this email already exists.");
            return "register";
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(password));

        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute(
                "success",
                "Your account has been created. You can now log in.");

        return "redirect:/login";
    }
}

