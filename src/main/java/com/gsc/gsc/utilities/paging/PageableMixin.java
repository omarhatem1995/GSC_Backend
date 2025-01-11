package com.gsc.gsc.utilities.paging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public abstract class PageableMixin implements Pageable {

    @JsonCreator
    public PageableMixin(@JsonProperty("pageNumber") int pageNumber,
                         @JsonProperty("pageSize") int pageSize) {
        // Use these properties to create an instance of PageRequest
    }

    @Override
    @JsonProperty
    public abstract int getPageNumber();

    @Override
    @JsonProperty
    public abstract int getPageSize();

    @Override
    @JsonIgnore
    public abstract long getOffset();

    @Override
    @JsonIgnore
    public abstract Sort getSort();
}
