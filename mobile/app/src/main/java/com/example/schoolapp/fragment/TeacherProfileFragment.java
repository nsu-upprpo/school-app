package com.example.schoolapp.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.schoolapp.model.UserProfile;
import com.example.schoolapp.storage.TokenStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherProfileFragment extends Fragment {
    private EditText teacherNameInput;
    private EditText teacherPhoneInput;
    private Button teacherLogoutButton;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_profile, container, false);

        teacherNameInput = view.findViewById(R.id.teacherNameInput);
        teacherPhoneInput = view.findViewById(R.id.teacherPhoneInput);
        teacherLogoutButton = view.findViewById(R.id.teacherLogoutButton);
        saveButton = view.findViewById(R.id.saveButton);

        teacherLogoutButton.setOnClickListener(v -> logout());
        saveButton.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Сохранение пока не реализовано", Toast.LENGTH_SHORT).show()
        );

        loadProfile();
        return view;
    }
    private void loadProfile() {
        TokenStorage storage = new TokenStorage(requireContext());
        String token = storage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            teacherNameInput.setText("Тестовый учитель");
            teacherPhoneInput.setText("+7");
            return;
        }

        String authHeader = "Bearer " + token;

        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getMyProfile(authHeader).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();

                    String fullName =
                            safe(profile.getFirstName()) + " " +
                                    safe(profile.getLastName()) + " " +
                                    safe(profile.getPatronymic());

                    teacherNameInput.setText(fullName.trim().replaceAll("\\s+", " "));
                    teacherPhoneInput.setText(safe(profile.getPhone()));
                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить профиль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        TokenStorage storage = new TokenStorage(requireContext());
        storage.clear();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}