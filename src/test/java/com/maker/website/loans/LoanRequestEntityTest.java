package com.maker.website.loans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoanRequestEntityTest {

    @Test
    void initializesDefaultsAndMaintainsItemRelationship() {
        LoanRequestEntity request = new LoanRequestEntity();
        LoanRequestItemEntity item = new LoanRequestItemEntity();

        request.addItem(item);
        request.onCreate();

        assertEquals(LoanRequestStatusENUM.PENDING, request.getStatus());
        assertNotNull(request.getCreatedAt());
        assertEquals(request, item.getLoanRequest());
        assertEquals(1, request.getItems().size());
    }

    @Test
    void keepsExplicitStatusAndRefreshesUpdateTimestamp() throws InterruptedException {
        LoanRequestEntity request = new LoanRequestEntity();
        request.setStatus(LoanRequestStatusENUM.APPROVED);
        request.onCreate();
        var oldUpdatedAt = request.getUpdatedAt();
        Thread.sleep(2);

        request.onUpdate();

        assertEquals(LoanRequestStatusENUM.APPROVED, request.getStatus());
        assertTrue(request.getUpdatedAt().isAfter(oldUpdatedAt));
    }
}
