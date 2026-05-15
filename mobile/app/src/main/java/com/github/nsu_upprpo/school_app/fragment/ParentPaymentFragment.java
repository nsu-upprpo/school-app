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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private boolean paymentRefreshHadSuccess;
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
        paymentsStorage = new ParentPaymentsStorage(requireContext());

        paidPaymentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        paidPaymentsRecyclerView.setAdapter(adapter);

        payButtonText.setOnClickListener(v -> submitPayment());

        if (paymentsStorage.hasLoadedPayments()) {
            showCachedPayments();
        } else {
            refreshPaymentsFromBackend();
        }

        return view;
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
        paymentRefreshHadSuccess = false;
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

                        paymentRefreshHadSuccess = true;
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

                        paymentRefreshHadSuccess = true;
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

        if (!paymentRefreshHadSuccess) {
            if (!paymentsStorage.hasLoadedPayments()) {
                showNoUnpaidPayments();
            }
            return;
        }

        paymentsStorage.savePayments(pendingUnpaidPayments, pendingPaymentHistory);
        showPaymentState(pendingUnpaidPayments, pendingPaymentHistory);
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

        unpaidMonthText.setText(payment.getPeriod());

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
}
