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
import com.example.schoolapp.api.ChildApi;
import com.example.schoolapp.api.UserApi;
import com.example.schoolapp.model.ChildDto;
import com.example.schoolapp.model.UserProfile;
import com.example.schoolapp.storage.TokenStorage;
import com.example.schoolapp.storage.UserStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentProfileFragment extends Fragment {

    private EditText parentNameInput;
    private EditText parentPhoneInput;
    private EditText childNameInput;
    private EditText branchInput;
    private Button parentLogoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_profile, container, false);

        parentNameInput = view.findViewById(R.id.parentNameInput);
        parentPhoneInput = view.findViewById(R.id.parentPhoneInput);
        childNameInput = view.findViewById(R.id.childNameInput);
        branchInput = view.findViewById(R.id.branchInput);
        parentLogoutButton = view.findViewById(R.id.parentLogoutButton);

        parentLogoutButton.setOnClickListener(v -> logout());
        loadProfile();

        return view;
    }

    private void loadProfile() {
        UserStorage userStorage = new UserStorage(requireContext());

        if (userStorage.hasParentProfile()) {
            parentNameInput.setText(userStorage.getParentName());
            parentPhoneInput.setText(userStorage.getParentPhone());
            childNameInput.setText(userStorage.getChildName());
            branchInput.setText(userStorage.getBranch());
            return;
        }

        loadParentProfileFromBackend(userStorage);
    }

    private void loadParentProfileFromBackend(UserStorage userStorage) {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;

        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        ChildApi childApi = ApiClient.getClient().create(ChildApi.class);

        userApi.getMyProfile(authHeader).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();

                    final String parentFullName = (
                            safe(profile.getFirstName()) + " " +
                                    safe(profile.getLastName()) + " " +
                                    safe(profile.getPatronymic())
                    ).trim().replaceAll("\\s+", " ");

                    final String parentPhone = safe(profile.getPhone());

                    parentNameInput.setText(parentFullName);
                    parentPhoneInput.setText(parentPhone);

                    childApi.getMyChildren(authHeader).enqueue(new Callback<List<ChildDto>>() {
                        @Override
                        public void onResponse(Call<List<ChildDto>> call, Response<List<ChildDto>> response) {
                            if (!isAdded()) return;

                            String childName = "";
                            String branch = "";

                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                ChildDto child = response.body().get(0);
                                childName = safe(child.getFullName());
                                branch = safe(child.getBranchId());
                            }

                            childNameInput.setText(childName);
                            branchInput.setText(branch);

                            userStorage.saveParentProfile(parentFullName, parentPhone, childName, branch);
                        }

                        @Override
                        public void onFailure(Call<List<ChildDto>> call, Throwable t) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Ошибка загрузки детей: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(requireContext(), "Не удалось загрузить профиль родителя", Toast.LENGTH_SHORT).show();
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