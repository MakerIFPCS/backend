package com.maker.website.materials;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialEntityTest {

    @Test
    void setsCreationAndUpdateTimestamps() throws InterruptedException {
        MaterialEntity material = new MaterialEntity();
        material.onCreate();
        var created = material.getCreatedAt();
        var updated = material.getUpdatedAt();
        Thread.sleep(2);

        material.onUpdate();

        assertNotNull(created);
        assertEquals(created, updated);
        assertTrue(material.getUpdatedAt().isAfter(updated));
    }
}
