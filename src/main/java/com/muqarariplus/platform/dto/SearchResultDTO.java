package com.muqarariplus.platform.dto;

import com.muqarariplus.platform.entity.Course;
import com.muqarariplus.platform.entity.CourseEnrichment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDTO {

    private List<Course> courses;
    private List<CourseEnrichment> enrichments;

    public int getTotalResults() {
        int c = courses != null ? courses.size() : 0;
        int e = enrichments != null ? enrichments.size() : 0;
        return c + e;
    }
}
