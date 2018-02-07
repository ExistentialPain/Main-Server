package com.SCI.game.client;

import com.SCI.net.User;

import java.net.ServerSocket;
import java.util.Collections;
import java.util.LinkedList;

public class GameServer implements Runnable {
    public GameServer(LinkedList<User> players, int allyTeamSize, ServerSocket socket) {
        this.socket = socket;
        Collections.shuffle(players);
        allies = new User[allyTeamSize];
        for (int i = 0; i < allies.length; ++i) {
            allies[i] = players.removeFirst();
        }
        enemies = new User[players.size()];
        for (int i = 0; i < enemies.length; ++i) {
            enemies[i] = players.removeFirst();
        }
    }

    @Override
    public void run() {

    }

    public void shutdown() {

    }

    private void accept() {

    }

    private User[] allies, enemies;
    private ServerSocket socket;
}
