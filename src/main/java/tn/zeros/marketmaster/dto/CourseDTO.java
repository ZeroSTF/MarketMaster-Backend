package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Course;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private String title;
    private String description;
    private String content;
    private Integer difficulty;

    public static CourseDTO fromEntity(Course course) {
        return CourseDTO.builder()
                .title(course.getTitle())
                .description(course.getDescription())
                .content(course.getContent())
                .difficulty(course.getDifficulty())
                .build();
    }
    public static Course toEntity(CourseDTO dto) {
        return Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .content(dto.getContent())
                .difficulty(dto.getDifficulty())
                .build();
    }
}
