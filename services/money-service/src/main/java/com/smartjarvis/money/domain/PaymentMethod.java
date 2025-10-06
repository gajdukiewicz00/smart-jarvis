package com.smartjarvis.money.domain;

/**
 * Payment method enumeration
 */
public enum PaymentMethod {
    CASH("Наличные"),
    CARD("Карта"),
    TRANSFER("Перевод"),
    DIGITAL_WALLET("Электронный кошелек"),
    CRYPTO("Криптовалюта"),
    CHECK("Чек"),
    OTHER("Другое");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
