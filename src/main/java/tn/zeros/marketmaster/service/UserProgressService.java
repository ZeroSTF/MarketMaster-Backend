package tn.zeros.marketmaster.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.UserProgressDTO;
import tn.zeros.marketmaster.entity.Course;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.UserProgress;
import tn.zeros.marketmaster.exception.*;
import tn.zeros.marketmaster.repository.CourseRepository;
import tn.zeros.marketmaster.repository.UserProgressRepository;
import tn.zeros.marketmaster.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProgressService {
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public UserProgressDTO startCourse(Long userId, Long courseId) {
        if (userProgressRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            throw new UserProgressAlreadyExistsException("User has already started this course");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        UserProgress progress = UserProgress.builder()
                .user(user)
                .course(course)
                .completed(false)
                .score(0)
                .progress("0%")
                .lastAccessed(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .build();

        log.info("Starting new course progress for user: {} and course: {}", userId, courseId);
        UserProgress savedProgress = userProgressRepository.save(progress);
        return UserProgressDTO.fromEntity(savedProgress);
    }

    public UserProgressDTO updateProgress(Long progressId, UserProgressDTO progressDTO) {
        UserProgress progress = userProgressRepository.findById(progressId)
                .orElseThrow(() -> new UserProgressNotFoundException("Progress not found with id: " + progressId));

        double currentProgress = Double.parseDouble(progress.getProgress().replace("%", ""));
        double newProgressValue = Double.parseDouble(progressDTO.getProgress().replace("%", ""));
        double totalProgress = Math.min(currentProgress + newProgressValue, 100.0);

        progress.setProgress(totalProgress + "%");

        if (totalProgress >= 100.0) {
            progress.setCompleted(true);
            progress.setEndDate(LocalDateTime.now());
        }

        progress.setScore(progressDTO.getScore());
        progress.setLastAccessed(LocalDateTime.now());

        log.info("Updating progress with id: {}. New progress: {}", progressId, progress.getProgress());
        UserProgress updatedProgress = userProgressRepository.save(progress);
        return UserProgressDTO.fromEntity(updatedProgress);
    }
    public List<UserProgressDTO> getCourseProgress(Long courseId, Long userId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with id: " + courseId);
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        log.info("Fetching progress for course: {} and user: {}", courseId, userId);
        return userProgressRepository.findByCourseIdAndUserId(courseId, userId).stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }
    public List<UserProgressDTO> getAllProgressForUser(Long userId) {
        // Check if the user exists, if needed
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        log.info("Fetching all progress for user: {}", userId);
        return userProgressRepository.findByUserId(userId).stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }




}