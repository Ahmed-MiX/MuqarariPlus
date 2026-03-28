package com.muqarariplus.platform.service;

import com.muqarariplus.platform.entity.SiteContent;
import com.muqarariplus.platform.repository.SiteContentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ContentService fetches all SiteContent rows from MySQL and exposes
 * a locale-aware Map<String, String> for Thymeleaf template injection.
 *
 * The service builds two flat maps (en + ar) from the database on every
 * call. For high-traffic production use, wrap with @Cacheable or a
 * scheduled refresh. For now this keeps the architecture simple and correct.
 */
@Service
public class ContentService {

    private final SiteContentRepository siteContentRepository;

    public ContentService(SiteContentRepository siteContentRepository) {
        this.siteContentRepository = siteContentRepository;
    }

    /**
     * Returns a flat map of {content_key -> locale-correct value}.
     * Templates access values via ${content['some.key']}.
     * Falls back to the English value if the Arabic translation is blank.
     */
    public Map<String, String> getContentMap(Locale locale) {
        List<SiteContent> rows = siteContentRepository.findAll();
        Map<String, String> map = new HashMap<>();
        boolean isArabic = "ar".equals(locale.getLanguage());

        for (SiteContent row : rows) {
            String value;
            if (isArabic) {
                String ar = row.getValueAr();
                value = (ar != null && !ar.isBlank()) ? ar : row.getValueEn();
            } else {
                String en = row.getValueEn();
                value = (en != null && !en.isBlank()) ? en : row.getValueAr();
            }
            map.put(row.getContentKey(), value != null ? value : "");
        }
        return map;
    }
}
