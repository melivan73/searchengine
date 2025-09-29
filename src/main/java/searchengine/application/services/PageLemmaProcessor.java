package searchengine.application.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Getter
public final class PageLemmaProcessor {
    public static final int MINIMAL_TEXT_SIE = 3;
    public static final String WORDS_REGEXP =
        "\\b[\\p{IsAlphabetic}][\\p{IsAlphabetic}-]*[\\p{IsAlphabetic}]?\\b";
    public static final String EN_WORD_REGEXP =
        "\\b[a-zA-Z]{2,}\\b";
    private final String text;
    private final Map<String, Integer> lemmas = new HashMap<>();
    private final List<WordInfo> wordInfoList = new ArrayList<>();
    private final TextAnalyzeMode mode;
    private String processedText = "";

    public boolean process() {
        String text = Jsoup.parse(this.text).text();
        if (text.length() < MINIMAL_TEXT_SIE) {
            return false;
        }
        processedText = text;

        EnglishLuceneMorphology enMorphology;
        RussianLuceneMorphology ruMorphology;
        try {
            enMorphology = new EnglishLuceneMorphology();
            ruMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Pattern wordPattern = Pattern.compile(WORDS_REGEXP,
            Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = wordPattern.matcher(processedText);

        LuceneMorphology wordMorphology;
        Set<String> illegalTags;
        WordInfo wordInfo = null;
        while (matcher.find()) {
            String word = matcher.group().toLowerCase().strip();
            if (word.matches(EN_WORD_REGEXP)) {
                wordMorphology = enMorphology;
                illegalTags = IllegalPartsOfSpeech.getIllegalTags(PageLanguage.ENGLISH);
            } else {
                wordMorphology = ruMorphology;
                illegalTags = IllegalPartsOfSpeech.getIllegalTags(PageLanguage.RUSSIAN);
            }
            if (mode == TextAnalyzeMode.LEMMA_WORD_AND_WORDPOS) {
                wordInfo = new WordInfo();
                wordInfo.setOriginalWord(word);
                wordInfo.setLemma("");
                wordInfo.setTags(null);
                wordInfo.setStartPos(matcher.start());
                wordInfo.setEndPos(matcher.end());
                wordInfoList.add(wordInfo);
            }
            if (!wordMorphology.checkString(word)) {
                continue;
            }
            List<String> baseForms = wordMorphology.getNormalForms(word);
            Set<String> finalIllegalTags = illegalTags;
            for (String baseForm : baseForms) {
                List<String> morphInfos = wordMorphology.getMorphInfo(baseForm);
                boolean isIllegal = morphInfos.stream().anyMatch(
                    info -> finalIllegalTags.stream().anyMatch(info::contains));
                if (!isIllegal) {
                    if (mode == TextAnalyzeMode.LEMMA_WORD_AND_WORDPOS) {
                        wordInfo.setLemma(baseForm);
                        wordInfo.setTags(morphInfos);
                    } else {
                        lemmas.merge(baseForm, 1, Integer::sum);
                    }
                }
            }
        }
        return true;
    }
}
