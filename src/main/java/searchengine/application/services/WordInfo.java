package searchengine.application.services;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class WordInfo {
    private String originalWord;
    private String lemma;
    private int startPos;
    private int endPos;
    private List<String> tags;
}
