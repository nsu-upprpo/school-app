package com.github.nsu_upprpo.school_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.adapter.ParentPaymentAdapter;
import com.github.nsu_upprpo.school_app.model.ParentPaymentItem;

import java.util.ArrayList;
import java.util.List;

public class ParentPaymentFragment extends Fragment {

    private View unpaidBlock;
    private TextView unpaidMonthText;
    private TextView unpaidAmountText;
    private TextView payButtonText;
    private TextView noUnpaidText;

    private RecyclerView paidPaymentsRecyclerView;
    private ParentPaymentAdapter adapter;

    private boolean hasUnpaidPayment = true;

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
        paidPaymentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        paidPaymentsRecyclerView.setAdapter(adapter);

        payButtonText.setOnClickListener(v -> showPaymentUnavailableDialog());

        showUnpaidPayment();
        adapter.updatePayments(createPaidPayments());

        return view;
    }

    private void showPaymentUnavailableDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Оплата недоступна")
                .setMessage("Серверные endpoint-ы для проведения платежей пока не реализованы.")
                .setPositiveButton("Понятно", null)
                .show();
    }

    private void showUnpaidPayment() {
        if (hasUnpaidPayment) {
            unpaidBlock.setVisibility(View.VISIBLE);
            noUnpaidText.setVisibility(View.GONE);

            unpaidMonthText.setText("Апрель 2025");
            unpaidAmountText.setText("7300 руб.");
        } else {
            unpaidBlock.setVisibility(View.GONE);
            noUnpaidText.setVisibility(View.VISIBLE);
        }
    }

    private List<ParentPaymentItem> createPaidPayments() {
        List<ParentPaymentItem> payments = new ArrayList<>();

        payments.add(new ParentPaymentItem("Март 2025", "7300 руб.", true));
        payments.add(new ParentPaymentItem("Февраль 2025", "7300 руб.", true));
        payments.add(new ParentPaymentItem("Январь 2025", "7300 руб.", true));
        payments.add(new ParentPaymentItem("Декабрь 2024", "7300 руб.", true));
        payments.add(new ParentPaymentItem("Ноябрь 2024", "7300 руб.", true));
        payments.add(new ParentPaymentItem("Октябрь 2024", "7300 руб.", true));
        payments.add(new ParentPaymentItem("Сентябрь 2024", "7300 руб.", true));

        return payments;
    }
}