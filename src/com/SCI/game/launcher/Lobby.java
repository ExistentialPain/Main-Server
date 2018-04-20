package com.SCI.game.launcher;


import com.SCI.Main;
import com.SCI.game.LobbyUser;
import com.SCI.game.PlayerClass;
import com.SCI.game.client.GameServer;
import com.SCI.net.EventMessage;
import com.SCI.net.User;
import com.SCI.util.LimitedList;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class Lobby {
    public static Lobby create (User user, int lobbySize, int[] teamSizes) {
        Lobby lobby = new Lobby(user, UUID.randomUUID().toString());
        lobby.isAlive = true;
        lobby.teams = new ArrayList<>();
        for (int teamSize : teamSizes) {
            lobby.teams.add(new LimitedList<>(teamSize));
        }
        lobbies.put(lobby.getId(), lobby);
        return lobby;
    }

    public static Lobby search(String lobbyId) {
        return lobbies.get(lobbyId);
    }

    private Lobby(User owner, String lobbyId) {
        this.owner = owner;
        this.lobbyId = lobbyId;
        this.teams.get(0).put(new LobbyUser(owner, 0));
    }

    public String getId() {
        return lobbyId;
    }

    public synchronized boolean join(User user) {
        if (!isAlive) {
            return false;
        }
        boolean joined = false;
        LobbyUser lobbyUser = null;
        for (int i = 0; i < teams.size(); ++i) {
            lobbyUser = new LobbyUser(user, i);
            joined = teams.get(i).put(lobbyUser);
            if (joined) {
                break;
            }
        }
        if (!joined) {
            return  false;
        } else {
            EventMessage joinMessage = new EventMessage();
            joinMessage.write("lobby join " + user.getId() + " " + lobbyUser.getTeam());
            broadcast(joinMessage);
            StringBuilder lobbyData = new StringBuilder();
            lobbyData.append("lobby-data ").append(lobbyId);
            for (LimitedList<LobbyUser> team : teams) {
                for (LobbyUser lUser : team) {
                    lobbyData.append(lUser.getUser().getId())
                            .append(" ")
                            .append(lUser.getTeam())
                            .append(" ")
                            .append(lUser.getPlayerClass().intValue())
                            .append("\n");
                }
            }
            EventMessage message = new EventMessage();
            message.write(lobbyData.toString());
            user.sendMessage(message);
            boolean isFull = true;
            for (LimitedList<LobbyUser> team : teams) {
                if (!team.isFull()) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                startMatch();
            }
            return true;
        }
    }

    public void changeClass(User user, PlayerClass playerClass) {
        LobbyUser u = findUser(user);
        if (u != null) {
            u.setPlayerClass(playerClass);
            EventMessage message = new EventMessage();
            message.write("lobby-class " + user.getId() + " " + playerClass.intValue());
            broadcast(message);
        }
    }

    public synchronized void changeTeam(User user, int teamIndex) {
        LimitedList<LobbyUser> team = teams.get(teamIndex);
        if (team.isFull()) {
            EventMessage message = new EventMessage();
            message.write("err lobby-team full");
            user.sendMessage(message);
            return;
        }
        for (LimitedList<LobbyUser> lobbyUsers : teams) {
            for (LobbyUser lobbyUser : lobbyUsers) {
                if (lobbyUser.getUser().equals(user)) {
                    lobbyUsers.remove(lobbyUser);
                    team.put(lobbyUser);
                    lobbyUser.setTeam(teamIndex);
                    EventMessage response = new EventMessage();
                    response.write("succ lobby-team");
                    user.sendMessage(response);
                    EventMessage message = new EventMessage();
                    message.write("lobby-team " + user.getId() + " " + teamIndex);
                    broadcast(message);
                    return;
                }
            }
        }
    }

    private LobbyUser findUser(User user) {
        for (LimitedList<LobbyUser> team : teams) {
            for (LobbyUser lobbyUser : team) {
                if (lobbyUser.getUser().equals(user)) {
                    return lobbyUser;
                }
            }
        }
        return null;
    }

    public boolean disconnect(User user) {
        if (user.equals(owner)) {
            destroy();
            return true;
        }
        boolean left = false;
        nani:
        for (LimitedList<LobbyUser> team : teams) {
            for (LobbyUser lobbyUser : team) {
                if (lobbyUser.getUser().equals(user)) {
                    left = team.remove(lobbyUser);
                    break nani;
                }
            }
        }

        //boolean left = players.remove(user);
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
            LinkedList<LobbyUser> playas = new LinkedList<>();
            for (LimitedList<LobbyUser> team : teams) {
                playas.addAll(team.asList());
            }
            //playas.addAll(players.asList());
            GameServer server = new GameServer(playas, gameSocket);
            new Thread(server).start();
            EventMessage message = new EventMessage();
            message.write("game start " + gameSocket.getLocalPort());
            broadcast(message);
            isAlive = false;
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
        for (LimitedList<LobbyUser> team : teams) {
            for (LobbyUser lobbyUser : team) {
                User player = lobbyUser.getUser();
                Main.users.get(player.getId()).sendMessage(message);
            }
        }
    }

    private User owner;
    private ArrayList<LimitedList<LobbyUser>> teams;
    private String lobbyId;
    private boolean isAlive;
    private static HashMap<String, Lobby> lobbies = new HashMap<>();
}
