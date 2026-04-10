package com.example.schoolapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.schoolapp.LoginActivity;
import com.example.schoolapp.R;
import com.example.schoolapp.api.ApiClient;
import com.example.schoolapp.api.UserApi;
import com.example.schoolapp.model.UpdateProfileRequest;
import com.example.schoolapp.model.UserProfile;
import com.example.schoolapp.storage.TokenStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_profile, container, false);

        Button logoutButton = view.findViewById(R.id.parentLogoutButton);

        logoutButton.setOnClickListener(v -> {
            TokenStorage storage = new TokenStorage(requireContext());
            storage.clear();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}