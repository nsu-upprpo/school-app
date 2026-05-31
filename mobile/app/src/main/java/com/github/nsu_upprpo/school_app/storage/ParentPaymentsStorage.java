package com.github.nsu_upprpo.school_app.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.nsu_upprpo.school_app.model.PaymentDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ParentPaymentsStorage {
    private static final String PREFS = "parent_payments_storage";

    private static final String KEY_LOADED = "loaded";
    private static final String KEY_UNPAID_PAYMENTS = "unpaid_payments";
    private static final String KEY_PAYMENT_HISTORY = "payment_history";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ParentPaymentsStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean hasLoadedPayments() {
        return prefs.getBoolean(KEY_LOADED, false);
    }

    public void savePayments(List<PaymentDto> unpaidPayments, List<PaymentDto> paymentHistory) {
        prefs.edit()
                .putBoolean(KEY_LOADED, true)
                .putString(KEY_UNPAID_PAYMENTS, gson.toJson(unpaidPayments))
                .putString(KEY_PAYMENT_HISTORY, gson.toJson(paymentHistory))
                .apply();
    }

    public void saveUnpaidPayments(List<PaymentDto> unpaidPayments) {
        prefs.edit()
                .putBoolean(KEY_LOADED, true)
                .putString(KEY_UNPAID_PAYMENTS, gson.toJson(unpaidPayments))
                .apply();
    }

    public void savePaymentHistory(List<PaymentDto> paymentHistory) {
        prefs.edit()
                .putBoolean(KEY_LOADED, true)
                .putString(KEY_PAYMENT_HISTORY, gson.toJson(paymentHistory))
                .apply();
    }

    public List<PaymentDto> getUnpaidPayments() {
        return getPaymentsByKey(KEY_UNPAID_PAYMENTS);
    }

    public List<PaymentDto> getPaymentHistory() {
        return getPaymentsByKey(KEY_PAYMENT_HISTORY);
    }

    private List<PaymentDto> getPaymentsByKey(String key) {
        String json = prefs.getString(key, "");

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<PaymentDto>>() {}.getType();
        List<PaymentDto> payments = gson.fromJson(json, type);

        return payments == null ? new ArrayList<>() : payments;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
