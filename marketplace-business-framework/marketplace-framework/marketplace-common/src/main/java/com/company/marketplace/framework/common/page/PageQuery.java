package com.company.marketplace.framework.common.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageQuery(@Min(1) long pageNo, @Min(1) @Max(200) long pageSize) {
    public long offset() { return (pageNo - 1) * pageSize; }
}
