package rocketseat.com.passin.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rocketseat.com.passin.domain.attendee.Attendee;
import rocketseat.com.passin.domain.checkin.CheckIn;
import rocketseat.com.passin.dto.attendee.AttendeeDetailsDTO;
import rocketseat.com.passin.dto.attendee.AttendeeListResponseDTO;
import rocketseat.com.passin.repositories.AttendeeRepository;
import rocketseat.com.passin.repositories.CheckInRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final CheckInRepository checkInRepository;

    public List<Attendee> getAllAttendeesFromEvent(String eventId) {
        return this.attendeeRepository.findByEventId(eventId);
    }

    public AttendeeListResponseDTO getEventAttendees(String eventId) {
        List<Attendee> attendeeList = this.getAllAttendeesFromEvent(eventId);

        List<AttendeeDetailsDTO> attendeeDetailsDTOList = attendeeList.stream().map(attendee -> {
            Optional<CheckIn> checkIn = checkInRepository.findByAttendeeId(attendee.getId());
            LocalDateTime checkedInAt = checkIn.map(CheckIn::getCreatedAt).orElse(null);
            return new AttendeeDetailsDTO(
                    attendee.getId(),
                    attendee.getName(),
                    attendee.getEmail(),
                    attendee.getCreatedAt(),
                    checkedInAt
            );
        }).toList();

        return new AttendeeListResponseDTO(attendeeDetailsDTOList);

    }

}
