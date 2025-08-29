package searchengine.application.services;

import java.util.*;

public final class SnippetBuilder {

    private static class Span {
        int from, to;
        String a, b; // какие леммы покрывает (для информации)

        Span(int from, int to, String a, String b) {
            this.from = from;
            this.to = to;
            this.a = a;
            this.b = b;
        }

        int width() {return Math.max(0, to - from);}
    }


    public static String buildSnippetSimple(String text,
                                            List<WordInfo> words,
                                            Set<String> queryLemmas,
                                            int ctx,
                                            int distThreshold,
                                            int maxFragments) {
        Map<String, List<WordInfo>> byLemma = new HashMap<>();
        for (WordInfo w : words) {
            String lm = w.getLemma();
            if (lm == null || !queryLemmas.contains(lm)) {
                continue;
            }
            byLemma.computeIfAbsent(lm, k -> new ArrayList<>()).add(w);
        }
        if (byLemma.isEmpty()) {
            return "";
        }

        for (List<WordInfo> lst : byLemma.values()) {
            lst.sort(Comparator.comparingInt(WordInfo::getStartPos));
        }

        // самые "частые" леммы
        List<String> cores = new ArrayList<>(byLemma.keySet());
        cores.sort((x, y) -> Integer.compare(byLemma.get(y).size(), byLemma.get(x).size()));
        if (cores.size() > 2) {
            cores = cores.subList(0, 2);
        }

        List<Span> candidates = new ArrayList<>();
        for (String core : cores) {
            for (String lm : byLemma.keySet()) {
                if (lm.equals(core)) {
                    continue;
                }
                Span s = bestPairSpan(byLemma.get(core), byLemma.get(lm));
                if (s != null && s.width() <= distThreshold) {
                    candidates.add(s);
                }
            }
        }
        if (cores.size() == 2) {
            Span s = bestPairSpan(byLemma.get(cores.get(0)), byLemma.get(cores.get(1)));
            if (s != null && s.width() <= distThreshold) {
                candidates.add(s);
            }
        }

        if (candidates.isEmpty()) {
            String core = cores.get(0);
            Span s2 = bestCluster(byLemma.get(core), 2);
            if (s2 == null) {
                s2 = bestCluster(byLemma.get(core), 1);
            }
            if (s2 != null) {
                candidates.add(s2);
            }
        }

        candidates.sort(Comparator.comparingInt(Span::width));
        List<int[]> intervals = new ArrayList<>();
        for (Span s : candidates) {
            int from = Math.max(0, s.from - ctx);
            int to = Math.min(text.length(), s.to + ctx);
            while (from > 0 && !Character.isWhitespace(text.charAt(from - 1))) {
                from--;
            }
            while (to < text.length() && !Character.isWhitespace(text.charAt(to))) {
                to++;
            }

            boolean overlaps = false;
            for (int[] iv : intervals) {
                int inter = Math.min(iv[1], to) - Math.max(iv[0], from);
                int union = Math.max(iv[1], to) - Math.min(iv[0], from);
                if (inter > 0 && inter * 1.0 / union > 0.6) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                intervals.add(new int[]{from, to});
                if (intervals.size() >= maxFragments) {
                    break;
                }
            }
        }
        if (intervals.isEmpty()) {
            return "";
        }
        intervals.sort(Comparator.comparingInt(a -> a[0]));

        return renderSnippet(text, intervals, words, queryLemmas);
    }

    private static Span bestPairSpan(List<WordInfo> a, List<WordInfo> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return null;
        }
        int i = 0, j = 0;
        Span best = null;
        while (i < a.size() && j < b.size()) {
            WordInfo wa = a.get(i), wb = b.get(j);
            int from = Math.min(wa.getStartPos(), wb.getStartPos());
            int to = Math.max(wa.getEndPos(), wb.getEndPos());
            Span cand = new Span(from, to, wa.getLemma(), wb.getLemma());
            if (best == null || cand.width() < best.width()) {
                best = cand;
            }
            // двигаем тот, у кого раньше начало, чтобы приблизить позиции
            if (wa.getStartPos() < wb.getStartPos()) {
                i++;
            } else {
                j++;
            }
        }
        return best;
    }

    private static Span bestCluster(List<WordInfo> occ, int K) {
        if (occ == null || occ.isEmpty()) {
            return null;
        }
        if (K <= 1) {
            WordInfo w = occ.get(0);
            return new Span(w.getStartPos(), w.getEndPos(), w.getLemma(), w.getLemma());
        }
        if (occ.size() < K) {
            K = occ.size();
        }
        Span best = null;
        for (int i = 0; i + K - 1 < occ.size(); i++) {
            WordInfo ws = occ.get(i);
            WordInfo we = occ.get(i + K - 1);
            Span cand = new Span(ws.getStartPos(), we.getEndPos(), ws.getLemma(),
                ws.getLemma());
            if (best == null || cand.width() < best.width()) {
                best = cand;
            }
        }
        return best;
    }

    private static String renderSnippet(String text,
                                        List<int[]> intervals,
                                        List<WordInfo> words,
                                        Set<String> query) {
        List<WordInfo> sorted = new ArrayList<>(words);
        sorted.sort(Comparator.comparingInt(WordInfo::getStartPos));

        StringBuilder out = new StringBuilder();
        int prevEnd = 0;
        for (int k = 0; k < intervals.size(); k++) {
            int from = intervals.get(k)[0];
            int to = intervals.get(k)[1];

            if (k == 0 && from > 0) {
                out.append("…");
            }
            if (k > 0) {
                out.append("…");
            }

            int cur = from;
            for (WordInfo w : sorted) {
                if (w.getEndPos() <= from) {
                    continue;
                }
                if (w.getStartPos() >= to) {
                    break;
                }
                String lm = w.getLemma();
                if (lm == null || !query.contains(lm)) {
                    continue;
                }

                int s = Math.max(from, w.getStartPos());
                int e = Math.min(to, w.getEndPos());
                if (s <= cur && e <= cur) {
                    continue;
                }
                if (cur < s) {
                    out.append(text, cur, s);
                }
                if (e > s) {
                    out.append("<b>").append(text, s, e).append("</b>");
                }
                cur = e;
            }
            if (cur < to) {
                out.append(text, cur, to);
            }
            prevEnd = to;
        }
        if (prevEnd < text.length()) {
            out.append("…");
        }
        return out.toString();
    }
}
