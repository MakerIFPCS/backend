package com.maker.website.users;

import com.maker.website.users.enums.UserRoleENUM;
import com.maker.website.users.enums.UserStatusENUM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void initializesTimestampsAndDefaultRoleAndStatus() {
        UserEntity user = new UserEntity();

        user.prePersist();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(UserRoleENUM.USER, user.getRole());
        assertEquals(UserStatusENUM.ACTIVE, user.getStatus());
    }

    @Test
    void preservesExplicitRoleAndStatusAndUpdatesTimestamp() throws InterruptedException {
        UserEntity user = new UserEntity();
        user.setRole(UserRoleENUM.ADMIN);
        user.setStatus(UserStatusENUM.DELETED);
        user.prePersist();
        var oldUpdatedAt = user.getUpdatedAt();
        Thread.sleep(2);

        user.preUpdate();

        assertEquals(UserRoleENUM.ADMIN, user.getRole());
        assertEquals(UserStatusENUM.DELETED, user.getStatus());
        assertTrue(user.getUpdatedAt().isAfter(oldUpdatedAt));
    }
}
