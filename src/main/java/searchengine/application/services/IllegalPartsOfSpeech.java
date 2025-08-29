package searchengine.application.services;

import java.util.Map;
import java.util.Set;

public final class IllegalPartsOfSpeech {
    private static final Set<String> ENGLISH_ILLEGAL_TAGS = Set.of(
        "CONJ", "PREP", "PRT", "INT", "ARTICLE", "PN", "PN_ADJ"
    );

    private static final Set<String> RUSSIAN_ILLEGAL_TAGS = Set.of(
        "СОЮЗ", "ПРЕДЛ", "ЧАСТ", "МЕЖД", "МС", "МС-П", "ВВОДН"
    );

    private static final Map<PageLanguage, Set<String>> LANGUAGE_TAGS = Map.of(
        PageLanguage.ENGLISH, ENGLISH_ILLEGAL_TAGS,
        PageLanguage.RUSSIAN, RUSSIAN_ILLEGAL_TAGS
    );

    public static Set<String> getIllegalTags(PageLanguage lang) {
        return LANGUAGE_TAGS.getOrDefault(lang, Set.of());
    }
}