package com.SCI.game.launcher;


import com.SCI.Main;
import com.SCI.game.client.GameServer;
import com.SCI.net.EventMessage;
import com.SCI.net.User;
import com.SCI.util.LimitedList;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class Lobby {
    public static Lobby create (User user, int lobbySize, int allyTeamSize, int enemyTeamSize) {
        Lobby lobby = new Lobby(user, UUID.randomUUID().toString());
        lobby.isAlive = true;
        lobby.size = lobbySize;
        lobby.allyTeamSize = allyTeamSize;
        lobby.enemyTeamSize = enemyTeamSize;
        lobby.players = new LimitedList<>(allyTeamSize + enemyTeamSize - 1);
        lobbies.put(lobby.getId(), lobby);
        return lobby;
    }

    public static Lobby search(String lobbyId) {
        return lobbies.get(lobbyId);
    }

    private Lobby(User owner, String lobbyId) {
        this.owner = owner;
        this.lobbyId = lobbyId;
    }

    public String getId() {
        return lobbyId;
    }

    public boolean join(User user) {
        if (!isAlive) {
            return false;
        }
        boolean joined = players.put(user);
        if (!joined) {
            return  false;
        } else {
            EventMessage joinMessage = new EventMessage();
            joinMessage.write("lobby join " + user.getId());
            broadcast(joinMessage);
            if (players.isFull()) {
                startMatch();
            }
            return true;
        }
    }

    public boolean disconnect(User user) {
        if (user.equals(owner)) {
            destroy();
            return true;
        }
        boolean left = players.remove(user);
        if (left) {
            EventMessage leaveMessage = new EventMessage();
            leaveMessage.write("lobby leave " + user.getId());
            broadcast(leaveMessage);
            return true;
        } else {
            return false;
        }
    }

    private void startMatch() {
        try {
            ServerSocket gameSocket = new ServerSocket();
            LinkedList<User> playas = new LinkedList<>();
            playas.addAll(players.asList());
            playas.add(owner);
            GameServer server = new GameServer(playas, allyTeamSize, gameSocket);
            new Thread(server).start();
            EventMessage message = new EventMessage();
            message.write("game start " + gameSocket.getLocalPort());
            broadcast(message);
        } catch (IOException e) {
            e.printStackTrace();
            EventMessage gameCreateError = new EventMessage();
            gameCreateError.getEventHeaders().addHeader("code", "500");
            gameCreateError.write("game start error");
            Main.users.get(owner.getId()).sendMessage(gameCreateError);
        }
    }

    private void destroy() {
        isAlive = false;
        EventMessage destroyMessage = new EventMessage();
        destroyMessage.write("lobby destroy");
        broadcast(destroyMessage);
    }

    private void broadcast(EventMessage message) {
        for (User player : players) {
            Main.users.get(player.getId()).sendMessage(message);
        }
        Main.users.get(owner.getId()).sendMessage(message);
    }

    private User owner;
    private LimitedList<User> players;
    private String lobbyId;
    private boolean isAlive;
    private int size, allyTeamSize, enemyTeamSize;
    private static HashMap<String, Lobby> lobbies = new HashMap<>();
}
