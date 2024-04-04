package rocketseat.com.passin.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rocketseat.com.passin.domain.attendee.Attendee;

import java.util.List;
import java.util.Optional;

public interface AttendeeRepository extends JpaRepository<Attendee, String> {

    List<Attendee> findByEventId(String eventId);

    Page<Attendee> findByEventId(String eventId, Pageable pageable);

    Page<Attendee> findByEventIdAndNameContainsIgnoreCaseOrEmailContainsIgnoreCase(String eventId, String name, String email, Pageable pageable);

    Optional<Attendee> findByEventIdAndEmail(String eventId, String email);

}
