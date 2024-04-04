package rocketseat.com.passin.dto.attendee;

import java.util.List;

public record AttendeeListResponseDTO(
        List<AttendeeDetailsDTO> attendees,
        Long totalItems,
        Integer totalPages
) {
}
