package com.github.nsu_upprpo.school_app.fragment;

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

import com.github.nsu_upprpo.school_app.LoginActivity;
import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.UserApi;
import com.github.nsu_upprpo.school_app.model.UserProfile;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.storage.UserStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherProfileFragment extends Fragment {
    private EditText teacherNameInput;
    private EditText teacherPhoneInput;
    private Button teacherLogoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_profile, container, false);

        teacherNameInput = view.findViewById(R.id.teacherNameInput);
        teacherPhoneInput = view.findViewById(R.id.teacherPhoneInput);
        teacherLogoutButton = view.findViewById(R.id.teacherLogoutButton);

        teacherLogoutButton.setOnClickListener(v -> logout());
        loadProfile();

        return view;
    }
    private void loadProfile() {
        UserStorage userStorage = new UserStorage(requireContext());

        if (userStorage.hasTeacherProfile()) {
            teacherNameInput.setText(userStorage.getTeacherName());
            teacherPhoneInput.setText(userStorage.getTeacherPhone());
            return;
        }

        TokenStorage storage = new TokenStorage(requireContext());
        String token = storage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
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

                    fullName = fullName.trim().replaceAll("\\s+", " ");
                    String phone = safe(profile.getPhone());

                    teacherNameInput.setText(fullName);
                    teacherPhoneInput.setText(phone);

                    userStorage.saveTeacherProfile(fullName, phone);
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
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        UserStorage userStorage = new UserStorage(requireContext());

        tokenStorage.clear();
        userStorage.clear();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}