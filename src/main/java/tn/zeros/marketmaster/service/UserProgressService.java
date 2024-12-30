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

    public UserProgressDTO startCourse(String userName, String courseTitle) {

        if (userProgressRepository.findByUserUsernameAndCourseTitle(userName, courseTitle).isPresent()) {
            throw new UserProgressAlreadyExistsException("User has already started this course");
        }

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userName));
        Course course = courseRepository.findByTitle(courseTitle)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseTitle));

        UserProgress progress = UserProgress.builder()
                .user(user)
                .course(course)
                .completed(false)
                .score(0)
                .progress(0)
                .lastAccessed(LocalDateTime.now())
                .startDate(LocalDateTime.now())
                .build();

        UserProgress savedProgress = userProgressRepository.save(progress);
        return UserProgressDTO.fromEntity(savedProgress);
    }

    public UserProgressDTO updateProgress(Long progressId, UserProgressDTO progressDTO) {
        UserProgress progress = userProgressRepository.findById(progressId)
                .orElseThrow(() -> new UserProgressNotFoundException("Progress not found with id: " + progressId));

        int currentProgress = progress.getProgress();
        int newProgressValue = progressDTO.getProgress();
        int totalProgress = Math.min(currentProgress + newProgressValue, 100);

        progress.setProgress(totalProgress);

        if (totalProgress >= 100) {
            progress.setCompleted(true);
            progress.setEndDate(LocalDateTime.now());
        }

        progress.setScore(progressDTO.getScore());
        progress.setLastAccessed(LocalDateTime.now());

        log.info("Updating progress with id: {}. New progress: {}", progressId, totalProgress);
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
    public List<UserProgressDTO> getAllProgressForUser(String userName) {
        if (userRepository.findByUsername(userName).isEmpty()) {
            throw new UserNotFoundException("User not found with user name: " + userName);
        }

        log.info("Fetching all progress for user: {}", userName);
        return userProgressRepository.findByUserUsername(userName).stream()
                .map(UserProgressDTO::fromEntity)
                .toList();
    }




}