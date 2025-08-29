package searchengine.web.dto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

public final class OffsetBasedPageRequest implements Pageable {
    private final long offset;
    private final int pageSize;
    private final Sort sort;

    public OffsetBasedPageRequest(long offset, int pageSize, Sort sort) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public @NonNull Sort getSort() {
        return sort;
    }

    @Override
    public @NonNull Pageable next() {
        return new OffsetBasedPageRequest(offset + pageSize, pageSize, sort);
    }

    @Override
    public @NonNull Pageable previousOrFirst() {
        long newOffset = Math.max(offset - pageSize, 0);
        return new OffsetBasedPageRequest(newOffset, pageSize, sort);
    }

    @Override
    public @NonNull Pageable first() {
        return new OffsetBasedPageRequest(0, pageSize, sort);
    }

    @Override
    public @NonNull Pageable withPage(int pageNumber) {
        return new OffsetBasedPageRequest(offset * pageSize, pageSize, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / pageSize);
    }
}