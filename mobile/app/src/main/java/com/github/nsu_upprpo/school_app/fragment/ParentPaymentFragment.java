package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.adapter.ParentPaymentAdapter;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.PaymentApi;
import com.github.nsu_upprpo.school_app.model.PaymentDto;
import com.github.nsu_upprpo.school_app.storage.ParentPaymentsStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.util.PaymentUiFormatter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentPaymentFragment extends Fragment {

    private View unpaidBlock;

    private TextView unpaidMonthText;
    private TextView unpaidAmountText;
    private TextView payButtonText;
    private TextView noUnpaidText;

    private RecyclerView paidPaymentsRecyclerView;

    private ParentPaymentAdapter adapter;

    private PaymentDto currentUnpaidPayment;
    private ParentPaymentsStorage paymentsStorage;

    private int pendingPaymentLoads;
    private boolean unpaidPaymentsLoaded;
    private boolean paymentHistoryLoaded;
    private List<PaymentDto> pendingUnpaidPayments = new ArrayList<>();
    private List<PaymentDto> pendingPaymentHistory = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_parent_payment, container, false);

        unpaidBlock = view.findViewById(R.id.unpaidBlock);

        unpaidMonthText = view.findViewById(R.id.unpaidMonthText);
        unpaidAmountText = view.findViewById(R.id.unpaidAmountText);
        payButtonText = view.findViewById(R.id.payButtonText);
        noUnpaidText = view.findViewById(R.id.noUnpaidText);

        paidPaymentsRecyclerView = view.findViewById(R.id.paidPaymentsRecyclerView);

        adapter = new ParentPaymentAdapter();
        adapter.setOnPaymentClickListener(this::showPaymentDetailsDialog);
        paymentsStorage = new ParentPaymentsStorage(requireContext());

        paidPaymentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        paidPaymentsRecyclerView.setAdapter(adapter);

        payButtonText.setOnClickListener(v -> submitPayment());

        loadPayments();

        return view;
    }

    private void loadPayments() {
        if (paymentsStorage.hasLoadedPayments()) {
            showCachedPayments();
        }

        refreshPaymentsFromBackend();
    }

    private void showCachedPayments() {
        showPaymentState(
                paymentsStorage.getUnpaidPayments(),
                paymentsStorage.getPaymentHistory()
        );
    }

    private void refreshPaymentsFromBackend() {
        TokenStorage storage = new TokenStorage(requireContext());

        String token = storage.getAccessToken();

        if (token == null || token.isEmpty()) {
            return;
        }

        String authHeader = "Bearer " + token;

        PaymentApi api = ApiClient.getClient().create(PaymentApi.class);

        pendingPaymentLoads = 2;
        unpaidPaymentsLoaded = false;
        paymentHistoryLoaded = false;
        pendingUnpaidPayments = new ArrayList<>();
        pendingPaymentHistory = new ArrayList<>();

        loadUnpaidPayments(api, authHeader);
        loadPaymentHistory(api, authHeader);
    }

    private void loadUnpaidPayments(PaymentApi api, String authHeader) {
        api.getMyPaymentsByStatus(authHeader, "UNPAID")
                .enqueue(new Callback<List<PaymentDto>>() {

                    @Override
                    public void onResponse(Call<List<PaymentDto>> call,
                                           Response<List<PaymentDto>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            finishPaymentLoad();
                            return;
                        }

                        unpaidPaymentsLoaded = true;
                        pendingUnpaidPayments = response.body();
                        finishPaymentLoad();
                    }

                    @Override
                    public void onFailure(Call<List<PaymentDto>> call, Throwable t) {
                        finishPaymentLoad();
                    }
                });
    }

    private void loadPaymentHistory(PaymentApi api, String authHeader) {
        api.getMyPayments(authHeader)
                .enqueue(new Callback<List<PaymentDto>>() {

                    @Override
                    public void onResponse(Call<List<PaymentDto>> call,
                                           Response<List<PaymentDto>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            finishPaymentLoad();
                            return;
                        }

                        paymentHistoryLoaded = true;
                        pendingPaymentHistory = filterPaymentHistory(response.body());
                        finishPaymentLoad();
                    }

                    @Override
                    public void onFailure(Call<List<PaymentDto>> call, Throwable t) {
                        finishPaymentLoad();
                    }
                });
    }

    private void finishPaymentLoad() {
        pendingPaymentLoads--;

        if (pendingPaymentLoads > 0) {
            return;
        }

        if (!unpaidPaymentsLoaded && !paymentHistoryLoaded) {
            if (!paymentsStorage.hasLoadedPayments()) {
                showNoUnpaidPayments();
            }
            return;
        }

        if (unpaidPaymentsLoaded && paymentHistoryLoaded) {
            paymentsStorage.savePayments(pendingUnpaidPayments, pendingPaymentHistory);
            showPaymentState(pendingUnpaidPayments, pendingPaymentHistory);
            return;
        }

        if (unpaidPaymentsLoaded) {
            paymentsStorage.saveUnpaidPayments(pendingUnpaidPayments);
        }

        if (paymentHistoryLoaded) {
            paymentsStorage.savePaymentHistory(pendingPaymentHistory);
        }

        List<PaymentDto> unpaidPayments = unpaidPaymentsLoaded
                ? pendingUnpaidPayments
                : paymentsStorage.getUnpaidPayments();
        List<PaymentDto> paymentHistory = paymentHistoryLoaded
                ? pendingPaymentHistory
                : paymentsStorage.getPaymentHistory();

        showPaymentState(unpaidPayments, paymentHistory);

        if (isAdded()) {
            Toast.makeText(
                    requireContext(),
                    "Часть данных оплаты не обновилась, показаны последние сохранённые данные",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private List<PaymentDto> filterPaymentHistory(List<PaymentDto> payments) {
        List<PaymentDto> history = new ArrayList<>();

        if (payments == null) {
            return history;
        }

        for (PaymentDto payment : payments) {

            if ("PAID".equalsIgnoreCase(payment.getStatus())
                    || "PENDING_CONFIRMATION".equalsIgnoreCase(payment.getStatus())
                    || "REJECTED".equalsIgnoreCase(payment.getStatus())) {

                history.add(payment);
            }
        }

        return history;
    }

    private void showPaymentState(List<PaymentDto> unpaidPayments, List<PaymentDto> paymentHistory) {
        if (unpaidPayments == null || unpaidPayments.isEmpty()) {
            currentUnpaidPayment = null;
            showNoUnpaidPayments();
        } else {
            currentUnpaidPayment = unpaidPayments.get(0);
            showUnpaidPayment(currentUnpaidPayment);
        }

        adapter.updatePayments(paymentHistory);
    }

    private void showUnpaidPayment(PaymentDto payment) {

        unpaidBlock.setVisibility(View.VISIBLE);
        noUnpaidText.setVisibility(View.GONE);

        unpaidMonthText.setText(PaymentUiFormatter.formatPeriod(payment.getPeriod()));

        unpaidAmountText.setText(
                formatAmount(payment.getAmount())
        );
    }

    private void showNoUnpaidPayments() {
        unpaidBlock.setVisibility(View.GONE);
        noUnpaidText.setVisibility(View.VISIBLE);
    }

    private void submitPayment() {

        if (currentUnpaidPayment == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Подтвердить оплату?")
                .setMessage(
                        "После подтверждения платёж перейдёт в статус ожидания проверки администратора."
                )
                .setPositiveButton("Оплатить", (dialog, which) -> performSubmitPayment())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void performSubmitPayment() {

        TokenStorage storage = new TokenStorage(requireContext());

        String token = storage.getAccessToken();

        if (token == null || token.isEmpty()) {
            return;
        }

        String authHeader = "Bearer " + token;

        PaymentApi api = ApiClient.getClient().create(PaymentApi.class);

        api.submitPayment(authHeader, currentUnpaidPayment.getId())
                .enqueue(new Callback<PaymentDto>() {

                    @Override
                    public void onResponse(Call<PaymentDto> call,
                                           Response<PaymentDto> response) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    requireContext(),
                                    "Платёж отправлен на подтверждение",
                                    Toast.LENGTH_LONG
                            ).show();

                            refreshPaymentsFromBackend();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Не удалось отправить оплату",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentDto> call, Throwable t) {

                        Toast.makeText(
                                requireContext(),
                                "Ошибка соединения",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private String formatAmount(BigDecimal amount) {

        if (amount == null) {
            return "";
        }

        return amount.stripTrailingZeros().toPlainString() + " руб.";
    }

    private void showPaymentDetailsDialog(PaymentDto payment) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_payment_details, null);

        TextView childText = dialogView.findViewById(R.id.paymentDetailsChildText);
        TextView groupText = dialogView.findViewById(R.id.paymentDetailsGroupText);
        TextView periodText = dialogView.findViewById(R.id.paymentDetailsPeriodText);
        TextView amountText = dialogView.findViewById(R.id.paymentDetailsAmountText);
        TextView statusText = dialogView.findViewById(R.id.paymentDetailsStatusText);
        TextView coversText = dialogView.findViewById(R.id.paymentDetailsCoversText);
        TextView dueDateText = dialogView.findViewById(R.id.paymentDetailsDueDateText);
        TextView submittedAtText = dialogView.findViewById(R.id.paymentDetailsSubmittedAtText);
        TextView confirmedAtText = dialogView.findViewById(R.id.paymentDetailsConfirmedAtText);
        View rejectionBlock = dialogView.findViewById(R.id.paymentDetailsRejectionBlock);
        TextView rejectionReasonText = dialogView.findViewById(R.id.paymentDetailsRejectionReasonText);

        childText.setText("Ребёнок: " + safe(payment.getChildName()));
        groupText.setText("Группа: " + safe(payment.getGroupName()));
        periodText.setText("Период оплаты: " + PaymentUiFormatter.formatPeriod(payment.getPeriod()));
        amountText.setText("Сумма: " + nonEmpty(formatAmount(payment.getAmount())));
        statusText.setText("Статус: " + statusToText(payment.getStatus()));
        coversText.setText("Покрывает: " + formatRange(payment.getCoversFrom(), payment.getCoversTo()));
        dueDateText.setText("Срок оплаты: " + safeDate(payment.getDueDate()));
        submittedAtText.setText("Дата отправки оплаты: " + safeDateTime(payment.getSubmittedAt()));
        confirmedAtText.setText("Дата подтверждения: " + safeDateTime(payment.getConfirmedAt()));

        if (payment.getRejectionReason() == null || payment.getRejectionReason().isEmpty()) {
            rejectionBlock.setVisibility(View.GONE);
        } else {
            rejectionBlock.setVisibility(View.VISIBLE);
            rejectionReasonText.setText(payment.getRejectionReason());
        }

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Понятно", null)
                .show();
    }

    private String statusToText(String status) {
        if ("PAID".equalsIgnoreCase(status)) {
            return "Оплачено";
        }

        if ("PENDING_CONFIRMATION".equalsIgnoreCase(status)) {
            return "Ожидает";
        }

        if ("REJECTED".equalsIgnoreCase(status)) {
            return "Отклонено";
        }

        if ("UNPAID".equalsIgnoreCase(status)) {
            return "Не оплачено";
        }

        return "не указано";
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }

    private String nonEmpty(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }

    private String safeDate(String value) {
        String formatted = formatDate(value);
        return formatted == null ? "не указано" : formatted;
    }

    private String safeDateTime(String value) {
        String formatted = formatDateTime(value);
        return formatted == null ? "не указано" : formatted;
    }

    private String formatRange(String from, String to) {
        String formattedFrom = formatDate(from);
        String formattedTo = formatDate(to);

        if (formattedFrom == null && formattedTo == null) {
            return "не указано";
        }

        return nonEmpty(formattedFrom) + " – " + nonEmpty(formattedTo);
    }

    private String formatDate(String value) {
        Date date = parseDate(value);

        if (date == null) {
            return null;
        }

        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date);
    }

    private String formatDateTime(String value) {
        if (value == null || !value.contains("T")) {
            return formatDate(value);
        }

        Date date = parseDate(value);

        if (date == null) {
            return null;
        }

        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(date);
    }

    private Date parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.getDefault()).parse(value);
            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}
