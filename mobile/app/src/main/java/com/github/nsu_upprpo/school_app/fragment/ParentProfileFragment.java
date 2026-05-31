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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nsu_upprpo.school_app.LoginActivity;
import com.github.nsu_upprpo.school_app.R;
import com.github.nsu_upprpo.school_app.adapter.ParentChildAdapter;
import com.github.nsu_upprpo.school_app.api.ApiClient;
import com.github.nsu_upprpo.school_app.api.ChildApi;
import com.github.nsu_upprpo.school_app.api.UserApi;
import com.github.nsu_upprpo.school_app.model.ChildDto;
import com.github.nsu_upprpo.school_app.model.UserProfile;
import com.github.nsu_upprpo.school_app.storage.ParentLessonsStorage;
import com.github.nsu_upprpo.school_app.storage.ParentPaymentsStorage;
import com.github.nsu_upprpo.school_app.storage.TokenStorage;
import com.github.nsu_upprpo.school_app.storage.UserStorage;
import com.github.nsu_upprpo.school_app.api.GroupApi;
import com.github.nsu_upprpo.school_app.model.GroupDto;
import com.github.nsu_upprpo.school_app.storage.ChildrenStorage;
import com.github.nsu_upprpo.school_app.storage.ParentChildProfileStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParentProfileFragment extends Fragment {

    private EditText parentNameInput;
    private EditText parentPhoneInput;
    private RecyclerView childrenRecyclerView;
    private ParentChildAdapter childAdapter;
    private Button parentLogoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_profile, container, false);

        parentNameInput = view.findViewById(R.id.parentNameInput);
        parentPhoneInput = view.findViewById(R.id.parentPhoneInput);
        childrenRecyclerView = view.findViewById(R.id.childrenRecyclerView);
        parentLogoutButton = view.findViewById(R.id.parentLogoutButton);

        childAdapter = new ParentChildAdapter();
        childrenRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        childrenRecyclerView.setAdapter(childAdapter);
        childAdapter.setOnChildClickListener(this::openChildProfile);

        parentLogoutButton.setOnClickListener(v -> logout());

        loadProfile();

        return view;
    }

    private void loadProfile() {
        UserStorage userStorage = new UserStorage(requireContext());
        ChildrenStorage childrenStorage = new ChildrenStorage(requireContext());
        boolean hasCachedProfile = userStorage.hasParentProfile();
        boolean hasCachedChildren = childrenStorage.hasChildren();

        if (hasCachedProfile) {
            parentNameInput.setText(userStorage.getParentName());
            parentPhoneInput.setText(userStorage.getParentPhone());
        } else {
            parentNameInput.setText("Загрузка...");
            parentPhoneInput.setText("");
        }

        if (hasCachedChildren) {
            childAdapter.updateChildren(childrenStorage.getChildren());
            childAdapter.setChildBranchNames(childrenStorage.getChildBranches());
        } else {
            childAdapter.updateChildren(null);
        }

        loadParentProfileFromBackend(userStorage, hasCachedProfile);
        loadChildrenFromBackend(childrenStorage, hasCachedChildren);
    }

    private void loadParentProfileFromBackend(UserStorage userStorage, boolean hasCachedProfile) {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

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

                    String parentFullName = (
                            safe(profile.getFirstName()) + " " +
                                    safe(profile.getLastName()) + " " +
                                    safe(profile.getPatronymic())
                    ).trim().replaceAll("\\s+", " ");

                    String parentPhone = safe(profile.getPhone());

                    parentNameInput.setText(parentFullName);
                    parentPhoneInput.setText(parentPhone);

                    userStorage.saveParentProfile(parentFullName, parentPhone, "", "");
                } else {
                    showParentProfileRefreshError(hasCachedProfile);
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                if (!isAdded()) return;
                showParentProfileRefreshError(hasCachedProfile);
            }
        });
    }

    private void loadChildrenFromBackend(ChildrenStorage childrenStorage, boolean hasCachedChildren) {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        String token = tokenStorage.getAccessToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;

        ChildApi childApi = ApiClient.getClient().create(ChildApi.class);
        GroupApi groupApi = ApiClient.getClient().create(GroupApi.class);

        childApi.getMyChildren(authHeader).enqueue(new Callback<List<ChildDto>>() {
            @Override
            public void onResponse(Call<List<ChildDto>> call, Response<List<ChildDto>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<ChildDto> children = response.body();
                    childAdapter.updateChildren(children);

                    childrenStorage.saveChildren(children);

                    for (ChildDto child : children) {
                        loadBranchForChild(authHeader, groupApi, child);
                    }
                } else {
                    showChildrenRefreshError(hasCachedChildren);
                }
            }

            @Override
            public void onFailure(Call<List<ChildDto>> call, Throwable t) {
                if (!isAdded()) return;
                showChildrenRefreshError(hasCachedChildren);
            }
        });
    }

    private void showParentProfileRefreshError(boolean hasCachedProfile) {
        if (hasCachedProfile) {
            Toast.makeText(requireContext(), "Не удалось обновить профиль, показаны сохранённые данные", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Не удалось загрузить профиль родителя", Toast.LENGTH_SHORT).show();
            parentNameInput.setText("");
            parentPhoneInput.setText("");
        }
    }

    private void showChildrenRefreshError(boolean hasCachedChildren) {
        if (hasCachedChildren) {
            Toast.makeText(requireContext(), "Не удалось обновить детей, показаны сохранённые данные", Toast.LENGTH_SHORT).show();
        } else {
            childAdapter.updateChildren(null);
            Toast.makeText(requireContext(), "Не удалось загрузить детей", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBranchForChild(String authHeader, GroupApi groupApi, ChildDto child) {
        if (child.getGroups() == null || child.getGroups().isEmpty()) {
            return;
        }

        GroupDto shortGroup = child.getGroups().get(0);
        String groupId = shortGroup.getGroupId();

        if (groupId == null || groupId.isEmpty()) {
            return;
        }

        groupApi.getGroupById(authHeader, groupId).enqueue(new Callback<GroupDto>() {
            @Override
            public void onResponse(Call<GroupDto> call, Response<GroupDto> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    String branchName = response.body().getBranchName();

                    ChildrenStorage childrenStorage = new ChildrenStorage(requireContext());
                    childrenStorage.saveChildBranch(child.getId(), branchName);

                    childAdapter.setChildBranchName(child.getId(), branchName);
                }
            }

            @Override
            public void onFailure(Call<GroupDto> call, Throwable t) {
            }
        });
    }

    private void openChildProfile(ChildDto child, String branchName) {
        if (child == null || child.getId() == null || child.getId().isEmpty()) {
            return;
        }

        ParentChildProfileFragment fragment = ParentChildProfileFragment.newInstance(
                child.getId(),
                child.getFullName()
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.parentFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void logout() {
        TokenStorage tokenStorage = new TokenStorage(requireContext());
        UserStorage userStorage = new UserStorage(requireContext());
        ChildrenStorage childrenStorage = new ChildrenStorage(requireContext());
        ParentLessonsStorage parentLessonsStorage = new ParentLessonsStorage(requireContext());
        ParentChildProfileStorage parentChildProfileStorage = new ParentChildProfileStorage(requireContext());
        ParentPaymentsStorage parentPaymentsStorage = new ParentPaymentsStorage(requireContext());

        tokenStorage.clear();
        userStorage.clear();
        childrenStorage.clear();
        parentLessonsStorage.clear();
        parentChildProfileStorage.clear();
        parentPaymentsStorage.clear();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
