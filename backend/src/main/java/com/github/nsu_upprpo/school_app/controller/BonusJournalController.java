package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.response.BonusJournalResponse;
import com.github.nsu_upprpo.school_app.service.BonusJournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parents/me/children/{childId}/bonus-journal")
@RequiredArgsConstructor
public class BonusJournalController {

    private final BonusJournalService bonusJournalService;

    @GetMapping
    public ResponseEntity<List<BonusJournalResponse>> getBonusJournal(
            @PathVariable UUID childId
    ) {
        UUID parentId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(
                bonusJournalService.getForParentChild(parentId, childId)
        );
    }
}
