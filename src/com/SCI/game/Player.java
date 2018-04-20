package com.SCI.game;

import java.util.UUID;

public class Player {
    public Player(PlayerClass pClass, int team) {
        this.pClass = pClass;
        this.id = UUID.randomUUID();
        this.team = team;
    }

    public int getTeam() {
        return team;
    }

    public UUID getId() {
        return id;
    }

    public PlayerClass getPClass() {
        return pClass;
    }

    private int team;
    private UUID id;
    private PlayerClass pClass;
}
