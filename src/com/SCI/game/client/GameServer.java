package com.SCI.game.client;

import com.SCI.game.*;
import com.SCI.net.EventMessage;
import com.SCI.net.User;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

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
        try {
            HashMap<UUID, Player> hPlayers = new HashMap<>();
            for (User user : allies.values()) {
                Player p = new Player(PlayerClass.ARCHER);
                UUID id = UUID.randomUUID();
                hPlayers.put(id, p);
                user.setUserData(id);
            }

            for (User user : enemies.values()) {
                Player p = new Player(PlayerClass.ARCHER);
                UUID id = UUID.randomUUID();
                hPlayers.put(id, p);
                user.setUserData(id);
            }

            env = new GameEnvironment(hPlayers);
            handlerClass.getConstructor(GameEnvironment.class).newInstance(env);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
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
            tmp.bindEventSocket(sock, item -> {});
            User attempted;
            attempted = allies.get(author);
            if (attempted == null) {
                attempted = enemies.get(author);
            }
            if (attempted == null) {
                EventMessage tmpMessage = new EventMessage();
                tmpMessage.write("xvrrqsZxcsAA");
                tmp.sendMessage(tmpMessage);
                return;
            }

            if (!attempted.equals(tmp)) {
                EventMessage tmpMessage = new EventMessage();
                tmpMessage.write("cQsaVrt21b3n1s");
                tmp.sendMessage(tmpMessage);
                return;
            }
            if (attempted.isConnected()) {
                attempted.disconnect();
            }
            User finalAttempted = attempted;
            attempted.bindEventSocket(sock, rMessage -> {
                UUID id = (UUID) finalAttempted.getUserData();
                GameArgs args = new GameArgs(env.getPlayer(id));
                handler.handle(rMessage.getBody(), args);
                //TODO: implement the callback method for parsing message
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, User> allies, enemies;
    private GameEnvironment env;
    private ServerSocket socket;
    private static Class<? extends GameMessageHandler> handlerClass;
    private GameMessageHandler handler;
}
