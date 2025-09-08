package searchengine.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.application.*;
import searchengine.application.services.ErrorMessage;
import searchengine.web.dto.SearchResponse;
import searchengine.web.dto.StatisticsResponse;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequiredArgsConstructor
public class ApiController {
    private final StartIndexingUseCase startIndexing;
    private final StopIndexingUseCase stopIndexing;
    private final IndexPageUseCase indexPage;
    private final GetStatisticsUseCase getStats;
    private final SearchQueryUseCase searchQuery;

    /**
     * Метод формирует страницу из HTML-файла index.html,
     * который находится в папке resources/templates.
     * Это делает библиотека Thymeleaf.
     */

    public String index() {
        return "index";
    }

    @GetMapping("/api/startIndexing")
    public ResponseEntity<Map<String, Object>> start() throws ExecutionException,
        InterruptedException {


        Future<Boolean> result = startIndexing.executeAsync();
        //String error = result.get() ? "" : ErrorMessage.INDEXING_STARTED.getErrorMessage();

        return ResponseEntity.ok(Map.of(
            "result", true,
            "error", ""
        ));
    }

    @GetMapping("/api/stopIndexing")
    public ResponseEntity<Map<String, Object>> stop() {
        boolean result = stopIndexing.execute();

        String error = result ? "" : ErrorMessage.INDEXING_NOT_STARTED.getErrorMessage();
        return ResponseEntity.ok(Map.of(
            "result", error.isEmpty(),
            "error", error
        ));
    }

    @GetMapping("/api/statistics")
    public StatisticsResponse stats() {
        return getStats.execute();
    }

    @PostMapping(value = "/api/indexPage", params = "url")
    public ResponseEntity<Map<String, Object>> indexPage(
        @RequestParam("url") String pageUrl) {

        boolean result = indexPage.execute(pageUrl);

        String error = result ? "" : ErrorMessage.INDEX_PAGE_NOT_LEGAL.getErrorMessage();
        return ResponseEntity.ok(Map.of(
            "result", error.isEmpty(),
            "error", error
        ));
    }

    @GetMapping(value = "/api/search")
    public ResponseEntity<Map<String, Object>> search(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String site) {

        SearchResponse response = searchQuery.execute(query, site, offset, limit);

        return ResponseEntity.ok(Map.of(
            "result", true,
            "data", response.getItems(),
            "count", response.getTotal()
        ));
    }
}