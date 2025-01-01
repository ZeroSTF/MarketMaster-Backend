package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.CourseDTO;
import tn.zeros.marketmaster.entity.Course;
import tn.zeros.marketmaster.exception.CourseAlreadyExistsException;
import tn.zeros.marketmaster.exception.CourseNotFoundException;
import tn.zeros.marketmaster.repository.CourseRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor

public class CourseService {

    private final CourseRepository courseRepository;

    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseRepository.findByTitle(courseDTO.getTitle()).isPresent()) {
            throw new CourseAlreadyExistsException("Course with this title already exists");
        }

        Course course = CourseDTO.toEntity(courseDTO);
        log.info("Creating new course: {}", course.getTitle());
        Course savedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(savedCourse);
    }

    public CourseDTO getCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id:" + id));
        return CourseDTO.fromEntity(course);
    }

    public List<CourseDTO> getCoursesByFilters(String title, Integer difficulty) {
        List<Course> courses;
        if (title != null && !title.isEmpty()) {
            courses = courseRepository.findByTitleContainingIgnoreCase(title);
        } else if (difficulty != null) {
            courses = courseRepository.findByDifficulty(difficulty);
        } else {
            courses = courseRepository.findAll();
        }
        return courses.stream()
                .map(CourseDTO::fromEntity)
                .toList();
    }


    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));

        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setContent(courseDTO.getContent());
        course.setDifficulty(courseDTO.getDifficulty());

        log.info("Updating course with id: {}", id);
        Course updatedCourse = courseRepository.save(course);
        return CourseDTO.fromEntity(updatedCourse);
    }

    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));

        log.info("Deleting course with id: {}", id);
        courseRepository.delete(course);
    }
}

