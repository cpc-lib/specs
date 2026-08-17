package com.company.marketplace.framework.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount"); Objects.requireNonNull(currency, "currency");
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
    }
    public static Money cny(BigDecimal amount) { return new Money(amount, Currency.getInstance("CNY")); }
    public Money add(Money other) { sameCurrency(other); return new Money(amount.add(other.amount), currency); }
    public Money subtract(Money other) { sameCurrency(other); return new Money(amount.subtract(other.amount), currency); }
    private void sameCurrency(Money other) { if (!currency.equals(other.currency)) throw new IllegalArgumentException("currency mismatch"); }
}
