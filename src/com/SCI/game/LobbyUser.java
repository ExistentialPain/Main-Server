package com.SCI.game;

import com.SCI.net.User;

public class LobbyUser {
    public LobbyUser(User user, int team) {
        this.user = user;
        this.team = team;
    }

    public User getUser() {
        return user;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    private User user;
    private PlayerClass playerClass;
    private int team;
}
