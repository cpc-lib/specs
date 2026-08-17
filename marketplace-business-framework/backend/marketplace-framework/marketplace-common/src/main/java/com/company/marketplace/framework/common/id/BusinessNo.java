package com.company.marketplace.framework.common.id;

import java.util.Objects;

public record BusinessNo(String value) {
    public BusinessNo { Objects.requireNonNull(value, "value"); if (value.isBlank()) throw new IllegalArgumentException("blank business no"); }
}
