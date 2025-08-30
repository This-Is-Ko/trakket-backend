package org.trakket.controller;

import org.springframework.http.ResponseEntity;

public interface EventSyncController {

    public ResponseEntity<String> syncEvents();
}
