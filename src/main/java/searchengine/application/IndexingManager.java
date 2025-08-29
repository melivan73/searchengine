package searchengine.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import searchengine.application.crawler.CrawlerSiteTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndexingManager {
    private final ObjectProvider<CrawlerSiteTask> taskProvider;
    @Getter
    private final Set<ForkJoinPool> pools = ConcurrentHashMap.newKeySet();
    @Getter
    private final AtomicInteger indexedCount = new AtomicInteger(0);
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public void register(ForkJoinPool pool) {
        pools.add(pool);
    }

    public void stop() {
        stopped.set(true);
        pools.forEach(ForkJoinPool::shutdownNow);
    }

    public void stopPool(ForkJoinPool pool) {
        log.warn("Пул {} остановлен ", pool);
        pool.shutdownNow();
        pools.remove(pool);
    }

    public void reset() {
        stopped.set(false);
        indexedCount.set(0);
    }

    public boolean isStopped() {
        return stopped.get();
    }

    public boolean allPoolsTerminated() {
        return pools.stream().allMatch(ForkJoinPool::isTerminated);
    }

    public void awaitTermination() {
        indexedCount.set(0);
        for (ForkJoinPool pool : pools) {
            pool.shutdown();
        }
        for (ForkJoinPool pool : pools) {
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        pools.removeIf(ForkJoinPool::isTerminated);
    }

    public CrawlerSiteTask createTask(String url, Set<String> visitedUrls,
                                      Set<String> invalidUrls) {
        CrawlerSiteTask task = taskProvider.getObject();
        task.init(url, visitedUrls, invalidUrls);
        return task;
    }
}
