
package HotelManagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatusOrderByIdDesc(String status);

    boolean existsByRoomAndCheckinLessThanAndCheckoutGreaterThanAndStatusIn(
            String room,
            LocalDate checkout,
            LocalDate checkin,
            List<String> statuses
    );
}

