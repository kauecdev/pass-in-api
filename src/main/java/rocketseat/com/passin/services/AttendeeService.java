package rocketseat.com.passin.services;

import lombok.RequiredArgsConstructor;
import org.hsqldb.lib.StringUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import rocketseat.com.passin.domain.attendee.Attendee;
import rocketseat.com.passin.domain.attendee.exceptions.AttendeeAlreadyExistsException;
import rocketseat.com.passin.domain.attendee.exceptions.AttendeeNotFoundException;
import rocketseat.com.passin.domain.checkin.CheckIn;
import rocketseat.com.passin.dto.attendee.AttendeeBadgeResponseDTO;
import rocketseat.com.passin.dto.attendee.AttendeeDetailsDTO;
import rocketseat.com.passin.dto.attendee.AttendeeListResponseDTO;
import rocketseat.com.passin.dto.attendee.AttendeeBadgeDTO;
import rocketseat.com.passin.repositories.AttendeeRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendeeService {

    private final AttendeeRepository attendeeRepository;
    private final CheckInService checkInService;

    private Attendee getAttendeeById(String attendeeId) {
        return this.attendeeRepository.findById(attendeeId).orElseThrow(() -> new AttendeeNotFoundException("Attendee not found with ID: " + attendeeId));
    }

    public Page<Attendee> getPaginatedAttendeesFromEvent(String eventId, Integer pageIndex, String query) {
        int defaultSize = 10;
        Pageable pageable = PageRequest.of(pageIndex, defaultSize, Sort.Direction.DESC, "createdAt");

        if (StringUtil.isEmpty(query)) {
            return this.attendeeRepository.findByEventId(eventId, pageable);
        }

        return this.attendeeRepository.findByEventIdAndNameContainsIgnoreCaseOrEmailContainsIgnoreCase(eventId, query, query, pageable);
    }

    public List<Attendee> getAllAttendeesFromEvent(String eventId) {
        return this.attendeeRepository.findByEventId(eventId);
    }

    public AttendeeListResponseDTO getEventAttendees(String eventId, Integer pageIndex, String query) {
        Page<Attendee> attendeePage  = this.getPaginatedAttendeesFromEvent(eventId, pageIndex, query);

        List<AttendeeDetailsDTO> attendeeDetailsDTOList = attendeePage.getContent().stream().map(attendee -> {
            Optional<CheckIn> checkIn = this.checkInService.getCheckInByAttendeeId(attendee.getId());
            LocalDateTime checkedInAt = checkIn.map(CheckIn::getCreatedAt).orElse(null);
            return new AttendeeDetailsDTO(
                    attendee.getId(),
                    attendee.getName(),
                    attendee.getEmail(),
                    attendee.getCreatedAt(),
                    checkedInAt
            );
        }).toList();

        return new AttendeeListResponseDTO(attendeeDetailsDTOList, attendeePage.getTotalElements(), attendeePage.getTotalPages());

    }

    public void verifyAttendeeSubscription(String email, String eventId) {
        Optional<Attendee> isAttendeeRegistered = this.attendeeRepository.findByEventIdAndEmail(eventId, email);

        if (isAttendeeRegistered.isPresent()) {
            throw new AttendeeAlreadyExistsException("Attendee is already registered.");
        }
    }

    public Attendee registerAttendee(Attendee newAttendee) {
        this.attendeeRepository.save(newAttendee);
        return newAttendee;
    }

    public AttendeeBadgeResponseDTO getAttendeeBadge(String attendeeId, UriComponentsBuilder uriComponentsBuilder) {
        Attendee attendee = this.getAttendeeById(attendeeId);

        String uri = uriComponentsBuilder.path("/attendees/{attendeeId}/check-in").buildAndExpand(attendeeId).toUri().toString();

        AttendeeBadgeDTO attendeeBadgeDTO = new AttendeeBadgeDTO(attendee.getName(), attendee.getEmail(), uri, attendee.getEvent().getId());
        return new AttendeeBadgeResponseDTO(attendeeBadgeDTO);
    }

    public void checkInAttendee(String attendeeId) {
        Attendee attendee = this.getAttendeeById(attendeeId);
        this.checkInService.registerCheckIn(attendee);
    }

}
