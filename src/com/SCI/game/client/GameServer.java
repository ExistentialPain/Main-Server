package com.SCI.game.client;

import com.SCI.game.GameMessageHandler;
import com.SCI.net.EventMessage;
import com.SCI.net.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class GameServer implements Runnable {
    public GameServer(LinkedList<User> players, int allyTeamSize, ServerSocket socket) {
        this.socket = socket;
        Collections.shuffle(players);
        allies = new HashMap<>();
        for (int i = 0; i < allyTeamSize; ++i) {
            User player = players.removeFirst();
            allies.put(player.getId(), player);
        }
        enemies = new HashMap<>();
        int size = players.size();
        for (int i = 0; i < size; ++i) {
            User player = players.removeFirst();
            enemies.put(player.getId(), player);
        }
    }

    public static void setMessageHandler(GameMessageHandler handler) {

    }

    @Override
    public void run() {
        new Thread(() -> {
            while (!socket.isClosed()) {
                accept();
            }
        }).start();
    }

    public void shutdown() {
        // TODO: attempt to close gracefully
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (User user : allies.values()) {
                user.disconnect();
            }

            for (User user : enemies.values()) {
                user.disconnect();
            }
        }
    }

    private synchronized void accept() {
        try {
            Socket sock = socket.accept();
            EventMessage message = EventMessage.get(sock.getInputStream());
            String author = message.getEventHeaders().get("author");
            String token = message.getEventHeaders().get("token");
            User tmp = new User(author, token);
            User attempted;
            attempted = allies.get(author);
            if (attempted == null) {
                attempted = enemies.get(author);
            }
            if (attempted == null) {
                //TODO: send err message
                return;
            }

            if (!attempted.equals(tmp)) {
                //TODO: send err message;
                return;
            }
            if (attempted.isConnected()) {
                attempted.disconnect();
            }
            attempted.bindEventSocket(sock, item -> {

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, User> allies, enemies;
    private ServerSocket socket;
}
