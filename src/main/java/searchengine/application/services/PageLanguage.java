package searchengine.application.services;

import lombok.Getter;

/**
 * All supported languages enum.
 * Code by ISO_639
 */
@Getter
public enum PageLanguage {
    ENGLISH(45), RUSSIAN(570);
    private final int langCode;

    PageLanguage(int langCode) {
        this.langCode = langCode;
    }
}
