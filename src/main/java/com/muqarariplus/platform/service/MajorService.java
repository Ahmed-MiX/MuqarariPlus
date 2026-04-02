package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.College;
import com.muqarariplus.platform.entity.Major;
import com.muqarariplus.platform.payload.requests.MajorRequest;
import com.muqarariplus.platform.repository.CollegeRepository;
import com.muqarariplus.platform.repository.CourseRepository;
import com.muqarariplus.platform.repository.MajorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MajorService {

    private final MajorRepository majorRepository;
    private final CollegeRepository collegeRepository;
    private final CourseRepository courseRepository;

    public MajorService(MajorRepository majorRepository, CollegeRepository collegeRepository, CourseRepository courseRepository) {
        this.majorRepository = majorRepository;
        this.collegeRepository = collegeRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Major getMajorById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Major not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Major> getMajorsByCollegeId(Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new IllegalArgumentException("College not found with ID: " + collegeId));
        return college.getMajors();
    }

    @Transactional
    public Major createMajor(MajorRequest request) {
        College college = collegeRepository.findById(request.collegeId())
                .orElseThrow(() -> new IllegalArgumentException("College not found with ID: " + request.collegeId()));

        Major major = new Major();
        major.setNameEn(request.nameEn());
        major.setNameAr(request.nameAr());
        major.setCode(request.code());
        major.setCollege(college);
        return majorRepository.save(major);
    }

    @Transactional
    public Major updateMajor(Long id, MajorRequest request) {
        Major major = getMajorById(id);
        major.setNameEn(request.nameEn());
        major.setNameAr(request.nameAr());
        major.setCode(request.code());

        if (!major.getCollege().getId().equals(request.collegeId())) {
            College newCollege = collegeRepository.findById(request.collegeId())
                    .orElseThrow(() -> new IllegalArgumentException("College not found with ID: " + request.collegeId()));
            major.setCollege(newCollege);
        }
        return majorRepository.save(major);
    }

    @Transactional
    public void deleteMajor(Long id) {
        Major major = getMajorById(id);
        long courseCount = courseRepository.countByMajorId(id);
        if (courseCount > 0) {
            throw new IllegalStateException(
                "Cannot delete major '" + major.getNameEn() + "' — it has " + courseCount + " course(s) linked to it.");
        }
        majorRepository.delete(major);
    }
}
