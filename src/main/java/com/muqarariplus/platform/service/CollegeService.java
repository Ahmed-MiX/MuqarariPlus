package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.College;
import com.muqarariplus.platform.entity.University;
import com.muqarariplus.platform.payload.requests.CollegeRequest;
import com.muqarariplus.platform.repository.CollegeRepository;
import com.muqarariplus.platform.repository.MajorRepository;
import com.muqarariplus.platform.repository.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final UniversityRepository universityRepository;
    private final MajorRepository majorRepository;

    public CollegeService(CollegeRepository collegeRepository, UniversityRepository universityRepository, MajorRepository majorRepository) {
        this.collegeRepository = collegeRepository;
        this.universityRepository = universityRepository;
        this.majorRepository = majorRepository;
    }

    @Transactional(readOnly = true)
    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public College getCollegeById(Long id) {
        return collegeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("College not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<College> getCollegesByUniversityId(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + universityId));
        return university.getColleges();
    }

    @Transactional
    public College createCollege(CollegeRequest request) {
        University university = universityRepository.findById(request.universityId())
                .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + request.universityId()));

        College college = new College();
        college.setNameEn(request.nameEn());
        college.setNameAr(request.nameAr());
        college.setUniversity(university);
        return collegeRepository.save(college);
    }

    @Transactional
    public College updateCollege(Long id, CollegeRequest request) {
        College college = getCollegeById(id);
        college.setNameEn(request.nameEn());
        college.setNameAr(request.nameAr());

        if (!college.getUniversity().getId().equals(request.universityId())) {
            University newUniversity = universityRepository.findById(request.universityId())
                    .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + request.universityId()));
            college.setUniversity(newUniversity);
        }
        return collegeRepository.save(college);
    }

    @Transactional
    public void deleteCollege(Long id) {
        College college = getCollegeById(id);
        long majorCount = majorRepository.countByCollegeId(id);
        if (majorCount > 0) {
            throw new IllegalStateException(
                "Cannot delete college '" + college.getNameEn() + "' — it has " + majorCount + " major(s) linked to it.");
        }
        collegeRepository.delete(college);
    }
}
