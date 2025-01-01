package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.UserProgress;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDTO {
    private Long id;
    private UserDTO user;
    private CourseDTO course;
    private boolean completed;
    private Integer score;
    private Integer progress;
    private LocalDateTime lastAccessed;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static UserProgress toEntity(UserProgressDTO dto) {
        return UserProgress.builder()
                .id(dto.getId())
                .completed(dto.isCompleted())
                .score(dto.getScore())
                .progress(dto.getProgress())
                .lastAccessed(dto.getLastAccessed())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public static UserProgressDTO fromEntity(UserProgress entity) {
        return UserProgressDTO.builder()
                .id(entity.getId())
                .user(entity.getUser() == null ? null : UserDTO.fromEntity(entity.getUser()))
                .course(entity.getCourse() == null ? null : CourseDTO.fromEntity(entity.getCourse()))
                .completed(entity.isCompleted())
                .score(entity.getScore())
                .progress(entity.getProgress())
                .lastAccessed(entity.getLastAccessed())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }
}