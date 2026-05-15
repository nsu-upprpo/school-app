package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.PaymentDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ParentPaymentAdapter extends RecyclerView.Adapter<ParentPaymentAdapter.PaymentViewHolder> {

    private final List<PaymentDto> payments = new ArrayList<>();

    public void updatePayments(List<PaymentDto> newPayments) {
        payments.clear();

        if (newPayments != null) {
            payments.addAll(newPayments);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parent_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        PaymentDto payment = payments.get(position);

        holder.paymentMonthText.setText(formatPeriod(payment.getPeriod()));
        holder.paymentAmountText.setText(formatAmount(payment.getAmount()));
        holder.paymentActionText.setText(statusToText(payment.getStatus()));

        holder.itemView.setOnClickListener(v ->
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Детали оплаты")
                        .setMessage(buildDetailsText(payment))
                        .setPositiveButton("Понятно", null)
                        .show()
        );
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        return amount.stripTrailingZeros().toPlainString() + " руб.";
    }

    private String formatPeriod(String period) {
        if (period == null || period.isEmpty()) {
            return "Платёж";
        }

        return period;
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

        return "Детали ›";
    }

    private String buildDetailsText(PaymentDto payment) {
        return "Ребёнок: " + safe(payment.getChildName()) +
                "\nГруппа: " + safe(payment.getGroupName()) +
                "\nПериод оплаты: " + safe(payment.getPeriod()) +
                "\nСумма: " + formatAmount(payment.getAmount()) +
                "\nСтатус: " + statusToText(payment.getStatus()) +
                "\nПокрывает: " + safe(payment.getCoversFrom()) + " — " + safe(payment.getCoversTo()) +
                "\nСрок оплаты: " + safe(payment.getDueDate()) +
                "\nДата отправки оплаты: " + safe(payment.getSubmittedAt()) +
                "\nДата подтверждения: " + safe(payment.getConfirmedAt()) +
                "\nПричина отклонения: " + safe(payment.getRejectionReason());
    }

    private String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView paymentMonthText;
        TextView paymentAmountText;
        TextView paymentActionText;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);

            paymentMonthText = itemView.findViewById(R.id.paymentMonthText);
            paymentAmountText = itemView.findViewById(R.id.paymentAmountText);
            paymentActionText = itemView.findViewById(R.id.paymentActionText);
        }
    }
}
