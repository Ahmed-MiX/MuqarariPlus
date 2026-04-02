package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.CourseAdditionRequest;
import com.muqarariplus.platform.entity.CourseAdditionRequestStatus;
import com.muqarariplus.platform.entity.Major;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.payload.requests.CourseAdditionRequestPayload;
import com.muqarariplus.platform.payload.requests.CourseRequest;
import com.muqarariplus.platform.repository.CourseAdditionRequestRepository;
import com.muqarariplus.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseAdditionRequestService {

    private final CourseAdditionRequestRepository requestRepository;
    private final MajorService majorService;
    private final CourseService courseService;
    private final UserRepository userRepository;

    public CourseAdditionRequestService(CourseAdditionRequestRepository requestRepository,
                                        MajorService majorService,
                                        CourseService courseService,
                                        UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.majorService = majorService;
        this.courseService = courseService;
        this.userRepository = userRepository;
    }

    @Transactional
    public CourseAdditionRequest submitRequest(CourseAdditionRequestPayload payload, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Major major = majorService.getMajorById(payload.majorId());

        CourseAdditionRequest request = new CourseAdditionRequest();
        request.setCourseNameAr(payload.courseNameAr());
        request.setCourseNameEn(payload.courseNameEn());
        request.setCourseCode(payload.courseCode());
        request.setJustification(payload.justification());
        request.setRequestedBy(user);
        request.setMajor(major);
        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<CourseAdditionRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CourseAdditionRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course addition request not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<CourseAdditionRequest> getRequestsByStatus(CourseAdditionRequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<CourseAdditionRequest> getRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return requestRepository.findByRequestedBy(user);
    }

    @Transactional(readOnly = true)
    public List<CourseAdditionRequest> getRequestsByMajor(Long majorId) {
        majorService.getMajorById(majorId);
        return requestRepository.findByMajorId(majorId);
    }

    @Transactional
    public CourseAdditionRequest approveRequest(Long id, String adminNotes) {
        CourseAdditionRequest request = getRequestById(id);

        if (request.getStatus() != CourseAdditionRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved.");
        }

        request.setStatus(CourseAdditionRequestStatus.APPROVED);
        request.setAdminNotes(adminNotes);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

        String courseCode = request.getCourseCode();
        if (courseCode == null || courseCode.isBlank()) {
            courseCode = "REQ-" + request.getId();
        }

        CourseRequest courseRequest = new CourseRequest(
                courseCode,
                request.getCourseNameEn(),
                request.getCourseNameAr(),
                null,
                null,
                null,
                request.getMajor().getId()
        );
        courseService.createCourse(courseRequest);

        return request;
    }

    @Transactional
    public CourseAdditionRequest rejectRequest(Long id, String adminNotes) {
        CourseAdditionRequest request = getRequestById(id);

        if (request.getStatus() != CourseAdditionRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected.");
        }

        request.setStatus(CourseAdditionRequestStatus.REJECTED);
        request.setAdminNotes(adminNotes);
        request.setReviewedAt(LocalDateTime.now());
        return requestRepository.save(request);
    }

    @Transactional
    public void deleteRequest(Long id) {
        CourseAdditionRequest request = getRequestById(id);

        if (request.getStatus() == CourseAdditionRequestStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete an approved request — a course has been created from it.");
        }

        requestRepository.delete(request);
    }
}
