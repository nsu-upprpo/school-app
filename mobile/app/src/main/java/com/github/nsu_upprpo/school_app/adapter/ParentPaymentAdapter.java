package com.github.nsu_upprpo.school_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.model.ParentPaymentItem;

import java.util.ArrayList;
import java.util.List;

public class ParentPaymentAdapter extends RecyclerView.Adapter<ParentPaymentAdapter.PaymentViewHolder> {

    private final List<ParentPaymentItem> payments = new ArrayList<>();

    public void updatePayments(List<ParentPaymentItem> newPayments) {
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
        ParentPaymentItem payment = payments.get(position);

        holder.paymentMonthText.setText(payment.getMonth());
        holder.paymentAmountText.setText(payment.getAmount());
        holder.paymentActionText.setText("Детали  ›");

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(
                        holder.itemView.getContext(),
                        "Детали оплаты пока недоступны",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    @Override
    public int getItemCount() {
        return payments.size();
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