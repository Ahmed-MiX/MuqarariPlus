package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.University;
import com.muqarariplus.platform.payload.requests.UniversityRequest;
import com.muqarariplus.platform.repository.CollegeRepository;
import com.muqarariplus.platform.repository.UniversityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final CollegeRepository collegeRepository;

    public UniversityService(UniversityRepository universityRepository, CollegeRepository collegeRepository) {
        this.universityRepository = universityRepository;
        this.collegeRepository = collegeRepository;
    }

    @Transactional(readOnly = true)
    public List<University> getAllUniversities() {
        return universityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public University getUniversityById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + id));
    }

    @Transactional
    public University createUniversity(UniversityRequest request) {
        University university = new University();
        university.setNameEn(request.nameEn());
        university.setNameAr(request.nameAr());
        return universityRepository.save(university);
    }

    @Transactional
    public University updateUniversity(Long id, UniversityRequest request) {
        University university = getUniversityById(id);
        university.setNameEn(request.nameEn());
        university.setNameAr(request.nameAr());
        return universityRepository.save(university);
    }

    @Transactional
    public void deleteUniversity(Long id) {
        University university = getUniversityById(id);
        long collegeCount = collegeRepository.countByUniversityId(id);
        if (collegeCount > 0) {
            throw new IllegalStateException(
                "Cannot delete university '" + university.getNameEn() + "' — it has " + collegeCount + " college(s) linked to it.");
        }
        universityRepository.delete(university);
    }
}
