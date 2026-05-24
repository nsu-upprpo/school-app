package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.PaymentDto;
import com.github.nsu_upprpo.school_app.util.PaymentUiFormatter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentPaymentAdapter extends RecyclerView.Adapter<ParentPaymentAdapter.PaymentViewHolder> {

    private final List<PaymentDto> payments = new ArrayList<>();
    private OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(PaymentDto payment);
    }

    public void setOnPaymentClickListener(OnPaymentClickListener listener) {
        this.listener = listener;
    }

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

        holder.paymentMonthText.setText(PaymentUiFormatter.formatPeriod(payment.getPeriod()));
        holder.paymentAmountText.setText(formatAmount(payment.getAmount()));
        holder.paymentActionText.setText(statusToText(payment.getStatus()));

        String paidDate = getPaymentDate(payment);
        if (paidDate == null) {
            holder.paymentPaidDateText.setVisibility(View.GONE);
        } else {
            holder.paymentPaidDateText.setVisibility(View.VISIBLE);
            holder.paymentPaidDateText.setText("Оплачено: " + paidDate);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentClick(payment);
            }
        });
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

    private String safe(String value) {
        return value == null || value.isEmpty() ? "не указано" : value;
    }

    private String getPaymentDate(PaymentDto payment) {
        if (payment.getConfirmedAt() != null && !payment.getConfirmedAt().isEmpty()) {
            return formatDateForCard(payment.getConfirmedAt());
        }

        if (payment.getSubmittedAt() != null && !payment.getSubmittedAt().isEmpty()) {
            return formatDateForCard(payment.getSubmittedAt());
        }

        return null;
    }

    private String formatDateForCard(String value) {
        String formatted = formatDate(value);
        return formatted == null ? formatDateTime(value) : formatted;
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

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView paymentMonthText;
        TextView paymentAmountText;
        TextView paymentPaidDateText;
        TextView paymentActionText;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);

            paymentMonthText = itemView.findViewById(R.id.paymentMonthText);
            paymentAmountText = itemView.findViewById(R.id.paymentAmountText);
            paymentPaidDateText = itemView.findViewById(R.id.paymentPaidDateText);
            paymentActionText = itemView.findViewById(R.id.paymentActionText);
        }
    }
}
