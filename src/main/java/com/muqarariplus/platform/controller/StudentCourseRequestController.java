package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.entity.CourseAdditionRequest;
import com.muqarariplus.platform.entity.User;
import com.muqarariplus.platform.payload.requests.CourseAdditionRequestPayload;
import com.muqarariplus.platform.repository.UserRepository;
import com.muqarariplus.platform.service.CourseAdditionRequestService;
import com.muqarariplus.platform.service.UniversityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student/requests")
public class StudentCourseRequestController {

    private final CourseAdditionRequestService requestService;
    private final UserRepository userRepository;
    private final UniversityService universityService;

    public StudentCourseRequestController(CourseAdditionRequestService requestService,
                                          UserRepository userRepository,
                                          UniversityService universityService) {
        this.requestService = requestService;
        this.userRepository = userRepository;
        this.universityService = universityService;
    }

    @GetMapping
    public String viewRequests(Principal principal, Model model) {
        User user = resolveUser(principal);

        List<CourseAdditionRequest> myRequests = requestService.getRequestsByUser(user.getId());
        long pendingCount = myRequests.stream()
                .filter(r -> r.getStatus() == com.muqarariplus.platform.entity.CourseAdditionRequestStatus.PENDING)
                .count();

        model.addAttribute("myRequests", myRequests);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("totalCount", myRequests.size());
        model.addAttribute("universities", universityService.getAllUniversities());

        return "student/course-requests";
    }

    @PostMapping("/submit")
    public String submitRequest(@RequestParam String courseNameAr,
                                @RequestParam String courseNameEn,
                                @RequestParam(required = false) String courseCode,
                                @RequestParam(required = false) String justification,
                                @RequestParam Long majorId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = resolveUser(principal);

            CourseAdditionRequestPayload payload = new CourseAdditionRequestPayload(
                    courseNameAr, courseNameEn, courseCode, justification, majorId
            );
            requestService.submitRequest(payload, user.getId());

            redirectAttributes.addFlashAttribute("successMsg", "request_submitted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/student/requests";
    }

    private User resolveUser(Principal principal) {
        String identifier = principal.getName();
        User user = userRepository.findByEmail(identifier);
        if (user == null) {
            user = userRepository.findByUsername(identifier);
        }
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + identifier);
        }
        return user;
    }
}
