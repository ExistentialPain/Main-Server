package com.SCI.game.client;

import com.SCI.game.*;
import com.SCI.game.launcher.Lobby;
import com.SCI.net.EventMessage;
import com.SCI.net.User;
import com.SCI.util.Disposable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class GameServer implements Runnable, Disposable {
    public GameServer(LinkedList<LobbyUser> players, ServerSocket socket) {
        this.disposable = false;
        this.socket = socket;
        Collections.shuffle(players);

        this.players = new HashMap<>();
        while (!players.isEmpty()) {
            LobbyUser user = players.removeFirst();
            this.players.put(user.getUser().getId(), user);
        }

        /*allies = new HashMap<>();
        for (int i = 0; i < allyTeamSize; ++i) {
            User player = players.removeFirst();
            allies.put(player.getId(), player);
        }
        enemies = new HashMap<>();
        int size = players.size();
        for (int i = 0; i < size; ++i) {
            User player = players.removeFirst();
            enemies.put(player.getId(), player);
        }*/
        try {
            HashMap<UUID, Player> hPlayers = new HashMap<>();
            for (LobbyUser user : this.players.values()) {
                Player p = new Player(user.getPlayerClass(), user.getTeam());
                UUID id = p.getId();
                hPlayers.put(id, p);
                user.getUser().setUserData(id);
            }

            /*for (User user : enemies.values()) {
                Player p = new Player(PlayerClass.ARCHER, 2);
                UUID id = p.getId();
                hPlayers.put(id, p);
                user.setUserData(id);
            }*/

            env = new GameEnvironment(hPlayers);
            handler = handlerClass.getConstructor(GameEnvironment.class).newInstance(env);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void setMessageHandlerClass(Class<? extends GameMessageHandler> clazz) throws NoSuchMethodException {
        handlerClass.getConstructor(GameEnvironment.class);
        GameServer.handlerClass = clazz;
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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (LobbyUser user : players.values()) {
                user.getUser().disconnect();
            }
            disposable = true;
        }
    }

    @Override
    public boolean isDisposable() {
        return disposable;
    }

    private synchronized void accept() {
        Socket sock = null;
        try {
            sock = socket.accept();
            EventMessage message = EventMessage.get(sock.getInputStream());
            String author = message.getEventHeaders().get("author");
            String token = message.getEventHeaders().get("token");
            User tmp = new User(author, token);
            tmp.bindEventSocket(sock, item -> {});
            LobbyUser attempted;
            attempted = players.get(author);
            if (attempted == null) {
                EventMessage tmpMessage = new EventMessage();
                tmpMessage.write("xvrrqsZxcsAA");
                tmp.sendMessage(tmpMessage);
                tmp.close();
                return;
            }

            if (!attempted.getUser().equals(tmp)) {
                EventMessage tmpMessage = new EventMessage();
                tmpMessage.write("cQsaVrt21b3n1s");
                tmp.sendMessage(tmpMessage);
                tmp.close();
                return;
            }
            tmp.close();
            if (attempted.getUser().isConnected()) {
                attempted.getUser().disconnect();
            }
            LobbyUser finalAttempted = attempted;
            attempted.getUser().bindEventSocket(sock, rMessage -> {
                UUID id = (UUID) finalAttempted.getUser().getUserData();
                GameArgs args = new GameArgs(env.getPlayer(id));
                handler.handle(rMessage.getBody(), args);
                //TODO: implement the callback method for parsing message
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private boolean disposable;
    private HashMap<String, LobbyUser> players;
    private GameEnvironment env;
    private ServerSocket socket;
    private static Class<? extends GameMessageHandler> handlerClass;
    private GameMessageHandler handler;
}
