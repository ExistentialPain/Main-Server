package com.SCI.Connection;

import com.SCI.util.Pair;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

public class GameServer {
    GameServer(Socket[] players, MessageParser parser) {
        isOver = false;
        this.parser = parser;
        messageQueue = new LinkedList<>();
        playerSockets = new HashMap<>();
        for (Socket player : players) {
            PlayerSocket ps = new PlayerSocket(player, UUID.randomUUID());
            playerSockets.put(ps.getId(), ps);
        }
        for (final UUID id : playerSockets.keySet()) {
            new Thread(() -> {
                while (!isOver) {
                    try {
                        PlayerSocket sock = playerSockets.get(id);
                        Message msg = sock.getMessage();
                        postMessage(id, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        new Thread(() -> {
            synchronized (this) {
                while (!isOver) {
                    try {
                        while (messageQueue.isEmpty()) {
                            wait();
                        }
                        while (!messageQueue.isEmpty()) {
                            Pair<UUID, Message> tmp = messageQueue.removeFirst();
                            ParserResponse response = parser.parse(tmp.first, tmp.second);
                            if (response.isBroadcast) {
                                broadcast(response.message);
                            } else {
                                playerSockets.get(response.target).sendMessage(response.message);
                            }
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private synchronized void postMessage(UUID owner, Message message) {
        messageQueue.add(new Pair<>(owner, message));
        notifyAll();
    }

    public synchronized void broadcast(Message message) throws IOException {
        for (PlayerSocket socket : playerSockets.values()) {
            socket.sendMessage(message);
        }
    }

    private HashMap<UUID, PlayerSocket> playerSockets;
    private volatile boolean isOver;
    private MessageParser parser;
    private LinkedList<Pair<UUID, Message>> messageQueue;
}
