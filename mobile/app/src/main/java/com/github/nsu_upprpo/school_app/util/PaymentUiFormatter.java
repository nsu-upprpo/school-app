package com.github.nsu_upprpo.school_app.util;

public final class PaymentUiFormatter {

    private static final String[] MONTH_NAMES = {
            "Январь",
            "Февраль",
            "Март",
            "Апрель",
            "Май",
            "Июнь",
            "Июль",
            "Август",
            "Сентябрь",
            "Октябрь",
            "Ноябрь",
            "Декабрь"
    };

    private PaymentUiFormatter() {
    }

    public static String formatPeriod(String period) {
        if (period == null || period.isEmpty()) {
            return safe(period);
        }

        String[] parts = period.split("-");

        if (parts.length != 2) {
            return safe(period);
        }

        try {
            int month = Integer.parseInt(parts[1]);

            if (month < 1 || month > 12) {
                return safe(period);
            }

            return MONTH_NAMES[month - 1] + " " + parts[0];
        } catch (NumberFormatException e) {
            return safe(period);
        }
    }

    private static String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }
}
