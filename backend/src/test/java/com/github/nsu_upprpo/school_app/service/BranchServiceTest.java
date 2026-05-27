
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.response.BranchResponse;
import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.repository.BranchRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    void getAllBranches_returnsCentralBranch_forNovosibirskFilter() {
        Branch branch = mockBranch(SchoolSeedData.BRANCH_CENTRAL_ID,
                SchoolSeedData.BRANCH_CENTRAL_NAME, SchoolSeedData.CITY_NSK, true);
        when(branchRepository.findByCityAndActiveTrue(SchoolSeedData.CITY_NSK)).thenReturn(List.of(branch));

        List<BranchResponse> response = branchService.getAllBranches(SchoolSeedData.CITY_NSK);

        assertEquals(1, response.size());
        assertEquals(SchoolSeedData.BRANCH_CENTRAL_NAME, response.get(0).getName());
        assertEquals(SchoolSeedData.CITY_NSK, response.get(0).getCity());
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(branchRepository.findById(SchoolSeedData.BRANCH_CENTRAL_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> branchService.getById(SchoolSeedData.BRANCH_CENTRAL_ID));
    }

    @Test
    void delete_marksBranchInactive() {
        Branch branch = org.mockito.Mockito.mock(Branch.class);
        when(branchRepository.findById(SchoolSeedData.BRANCH_LEFT_ID)).thenReturn(Optional.of(branch));

        branchService.delete(SchoolSeedData.BRANCH_LEFT_ID);

        verify(branch).setActive(false);
        verify(branchRepository).save(branch);
    }

    private Branch mockBranch(java.util.UUID id, String name, String city, boolean active) {
        Branch branch = org.mockito.Mockito.mock(Branch.class);
        when(branch.getId()).thenReturn(id);
        when(branch.getName()).thenReturn(name);
        when(branch.getCity()).thenReturn(city);
        when(branch.getAddress()).thenReturn("ул. Ленина, 1");
        when(branch.getPhone()).thenReturn("+73831234567");
        when(branch.isActive()).thenReturn(active);
        return branch;
    }
}
