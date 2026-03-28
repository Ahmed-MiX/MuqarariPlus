package com.muqarariplus.platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component("messageSource")
public class JsonMessageSource extends AbstractMessageSource {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Locale, Map<String, String>> messagesMap = new HashMap<>();

    public JsonMessageSource() {
        loadMessages(Locale.ENGLISH, "messages_en.json");
        loadMessages(new Locale("ar"), "messages_ar.json"); // Arabic
    }

    private void loadMessages(Locale locale, String filename) {
        try {
            Resource resource = new ClassPathResource(filename);
            if (resource.exists()) {
                InputStream inputStream = resource.getInputStream();
                Map<String, String> messages = objectMapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
                messagesMap.put(locale, messages);
            }
        } catch (IOException e) {
            System.err.println("Could not load translating file " + filename);
        }
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = resolveCodeWithoutArguments(code, locale);
        if (message != null) {
            return new MessageFormat(message, locale);
        }
        return null;
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        // Fallback logic
        Locale lookupLocale = locale;
        if (!messagesMap.containsKey(lookupLocale)) {
            lookupLocale = Locale.ENGLISH;
        }

        Map<String, String> messagesForLocale = messagesMap.get(lookupLocale);
        if (messagesForLocale != null) {
            return messagesForLocale.get(code); // Returns the message or null if not found
        }
        return null; // Return null to indicate code not found
    }
}
