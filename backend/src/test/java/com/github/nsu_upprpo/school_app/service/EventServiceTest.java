
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateEventRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.EventResponse;
import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Event;
import com.github.nsu_upprpo.school_app.repository.EventRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private BranchService branchService;

    @InjectMocks
    private EventService eventService;

    @Test
    void getUpcoming_returnsOpenDayForCentralBranch() {
        Event event = mockEvent();
        when(eventRepository.findByBranchIdAndStartTimeAfterOrderByStartTimeAsc(
                org.mockito.Mockito.eq(SchoolSeedData.BRANCH_CENTRAL_ID), any()))
                .thenReturn(List.of(event));

        List<EventResponse> response = eventService.getUpcoming(SchoolSeedData.BRANCH_CENTRAL_ID);

        assertEquals(1, response.size());
        assertEquals("День открытых дверей", response.get(0).getName());
        assertEquals(SchoolSeedData.BRANCH_CENTRAL_ID, response.get(0).getBranchId());
    }

    @Test
    void create_returnsSavedEventResponse() {
        CreateEventRequest request = org.mockito.Mockito.mock(CreateEventRequest.class);
        when(request.getBranchId()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_ID);
        when(request.getName()).thenReturn("День открытых дверей");
        when(request.getDescription()).thenReturn("Презентация курсов и знакомство с преподавателями");
        when(request.getStartTime()).thenReturn(SchoolSeedData.EVENT_START);
        when(request.getEndTime()).thenReturn(SchoolSeedData.EVENT_END);
        when(request.getLocation()).thenReturn("Актовый зал");

        Branch branch = org.mockito.Mockito.mock(Branch.class);
        when(branchService.findById(SchoolSeedData.BRANCH_CENTRAL_ID)).thenReturn(branch);

        Event saved = mockEvent();
        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        EventResponse response = eventService.create(request);

        assertEquals(SchoolSeedData.EVENT_OPEN_DAY_ID, response.getId());
        assertEquals("День открытых дверей", response.getName());
    }

    @Test
    void delete_throwsNotFound_whenEventMissing() {
        when(eventRepository.existsById(SchoolSeedData.EVENT_OPEN_DAY_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> eventService.delete(SchoolSeedData.EVENT_OPEN_DAY_ID));
    }

    private Event mockEvent() {
        Branch branch = org.mockito.Mockito.mock(Branch.class);
        when(branch.getId()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_ID);
        when(branch.getName()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_NAME);

        Event event = org.mockito.Mockito.mock(Event.class);
        when(event.getId()).thenReturn(SchoolSeedData.EVENT_OPEN_DAY_ID);
        when(event.getName()).thenReturn("День открытых дверей");
        when(event.getDescription()).thenReturn("Презентация курсов и знакомство с преподавателями");
        when(event.getBranch()).thenReturn(branch);
        when(event.getStartTime()).thenReturn(SchoolSeedData.EVENT_START);
        when(event.getEndTime()).thenReturn(SchoolSeedData.EVENT_END);
        when(event.getLocation()).thenReturn("Актовый зал");
        return event;
    }
}
