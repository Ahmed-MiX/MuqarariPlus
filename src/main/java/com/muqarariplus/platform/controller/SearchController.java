package com.muqarariplus.platform.controller;

import com.muqarariplus.platform.dto.SearchResultDTO;
import com.muqarariplus.platform.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * GET /search?q=keyword — The Global Smart Search endpoint.
     */
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false, defaultValue = "") String keyword,
                         Model model) {
        SearchResultDTO results = searchService.searchGlobal(keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("courses", results.getCourses());
        model.addAttribute("enrichments", results.getEnrichments());
        model.addAttribute("courseCount", results.getCourses().size());
        model.addAttribute("enrichmentCount", results.getEnrichments().size());
        model.addAttribute("totalResults", results.getTotalResults());

        return "public/search-results";
    }
}
