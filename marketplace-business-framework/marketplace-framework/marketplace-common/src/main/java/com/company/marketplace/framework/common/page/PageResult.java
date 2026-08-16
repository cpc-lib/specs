package com.company.marketplace.framework.common.page;

import java.util.List;

public record PageResult<T>(List<T> items, long total, long pageNo, long pageSize) {
    public PageResult { items = List.copyOf(items); }
}
