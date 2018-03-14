package com.SCI.game;

import java.util.HashMap;
import java.util.UUID;

public class GameEnvironment {
    public GameEnvironment(HashMap<UUID, Player> players) {
        this.playerHashMap = players;
    }

    public Player getPlayer(UUID userId) {
        return playerHashMap.get(userId);
    }

    private final HashMap<UUID, Player> playerHashMap;
}
