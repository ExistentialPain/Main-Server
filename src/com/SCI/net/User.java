package com.SCI.net;


import com.SCI.Main;
import com.SCI.db.Database;
import com.SCI.db.Query;
import com.SCI.db.Results;
import com.SCI.db.Row;
import com.SCI.util.Disposable;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class User implements Disposable, Closeable {
    public User(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public void pushNotification(Notification n) {
        Database db = Main.getDatabaseConnection();
        try {
            db.execute(new Query("insert into notifications (accountID, author, code, data) values (?,?,?,?)",
                    Long.valueOf(getId()), Long.valueOf(n.getAuthor()), n.getCode(), n.getData()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void bindEventSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
        new Thread(() -> {
            while (this.socket != null && !this.socket.isClosed() && this.socket.isConnected()) {
                try {
                    EventMessage message = EventMessage.get(this.socket.getInputStream());
                    if (!equals(new User(message.getEventHeaders().get("author"), message.getEventHeaders().get("token")))) {
                        EventMessage m = new EventMessage();
                        m.getEventHeaders().addHeader("code", "401");
                        m.write("err_unauthorised");
                        sendMessage(m);
                        return;
                    }
                    eventHandler.handle(message);
                } catch (IOException e) {
                    this.socket = null;
                    this.out = null;
                    this.in = null;
                    return;
                }
            }
        }).start();
    }

    public synchronized void sendMessage(EventMessage message) {
        out.write(message.toString());
        out.flush();
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
        this.socket = null;
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean isDisposable() {
        // actually try and say if can be destroyed
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            throw new IllegalArgumentException("Obj is not a user");
        }
        User other = (User) obj;
        return Objects.equals(id, other.id) && Objects.equals(token, other.token);
    }

    public static void setEventHandler(Handler<EventMessage> handler) {
        eventHandler = handler;
    }

    public List<String> getFriendIds() {
        List<String> friends = new LinkedList<>();
        Database db = Main.getDatabaseConnection();
        try {
            Results results = db.execute(new Query("select * from friends where accountID1=? or accountID2=?",
                    Long.valueOf(getId()), Long.valueOf(getId())));
            for (int i = 0; i < results.length(); ++i) {
                Row row = results.getRow(i);
                if (!row.get("accountID1").toString().equals(getId())) {
                    friends.add(row.get("accountID1").toString());
                } else {
                    friends.add(row.get("accountID2").toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public List<User> getOnlineFriends() {
        List<User> friends = new LinkedList<>();
        List<String> friendIds = getFriendIds();
        for (String friendId : friendIds) {
            User user = Main.users.get(friendId);
            if (user != null) {
                friends.add(user);
            }
        }
        return friends;
    }

    public void notifyFriends(EventMessage message) {
        List<User> friends = getOnlineFriends();
        for (User friend : friends) {
            try {
                friend.sendMessage(message);
            } catch (Exception ignored) {

            }
        }
    }

    public void kill() {
        EventMessage message = new EventMessage();
        message.write("logout");
        try {
            sendMessage(message);
        } catch (Exception ignored) {

        }
        try {
            close();
        } catch (Exception ignored) {

        }
    }

    private String id, token;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static Handler<EventMessage> eventHandler;
}
