package tn.zeros.marketmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.CourseDTO;
import tn.zeros.marketmaster.dto.UserProgressDTO;
import tn.zeros.marketmaster.service.CourseService;
import tn.zeros.marketmaster.service.UserProgressService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
@Slf4j
public class CourseController {
    private final CourseService courseService;
    private final UserProgressService userProgressService;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        return new ResponseEntity<>(courseService.createCourse(courseDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourse(id));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer difficulty) {
        return ResponseEntity.ok(courseService.getCoursesByFilters(title, difficulty));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // User-Progress
    @PostMapping("/{courseTitle}/progress")
    public ResponseEntity<UserProgressDTO> startCourse(
            @RequestParam String username,
            @PathVariable String courseTitle) {
        return new ResponseEntity<>(
                userProgressService.startCourse(username, courseTitle),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/progress")
    public ResponseEntity<UserProgressDTO> updateProgress(
            @RequestBody UserProgressDTO progressDTO) {
        UserProgressDTO updatedProgress = userProgressService.updateProgress( progressDTO);
        return ResponseEntity.ok(updatedProgress);
    }



    @GetMapping("/{courseTitle}/progress/{userName}")
    public ResponseEntity<List<UserProgressDTO>> getCourseProgress(
            @PathVariable String courseTitle,
            @PathVariable String userName) {
        return ResponseEntity.ok(userProgressService.getCourseProgress(courseTitle, userName));
    }

    @GetMapping("/{userName}/progress")
    public ResponseEntity<List<UserProgressDTO>> getAllProgressForUser(@PathVariable String userName) {
        return ResponseEntity.ok(userProgressService.getAllProgressForUser(userName));
    }




}
