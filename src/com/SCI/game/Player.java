package com.SCI.game;

import java.util.UUID;

public class Player {
    public Player(PlayerClass pClass) {
        this.pClass = pClass;
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public PlayerClass getPClass() {
        return pClass;
    }

    private UUID id;
    private PlayerClass pClass;
}
