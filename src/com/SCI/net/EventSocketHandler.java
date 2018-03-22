package com.SCI.net;

import com.SCI.Main;
import com.SCI.db.Database;
import com.SCI.db.Query;
import com.SCI.db.Results;
import com.SCI.game.launcher.Lobby;

import java.sql.SQLException;

public class EventSocketHandler implements Handler<EventMessage> {
    private static EventSocketHandler ourInstance = new EventSocketHandler();

    public static EventSocketHandler getInstance() {
        return ourInstance;
    }

    private EventSocketHandler() {

    }

    @Override
    public void handle(EventMessage item) {
        System.out.println("Handling:");
        System.out.println(item);
        String[] values = item.getBody().split(" ");
        if (values.length == 0) {
            return;
        }
        try {
            switch (values[0]) {
                case "send-message": {
                    User sendee = Main.users.get(item.getEventHeaders().get("target"));
                    EventMessage message = new EventMessage();
                    message.getEventHeaders().addHeader("author", sendee.getId());
                    StringBuilder sb = new StringBuilder();
                    //sb.append("chat-message ");
                    for (int i = 1; i < values.length; ++i) {
                        sb.append(values[i]);
                        if (i != values.length - 1) {
                            sb.append(' ');
                        }
                    }

                    message.write("chat-message " + sb.toString());
                    sendee.sendMessage(message);
                    break;
                }

                case "leave-lobby": {
                    User leaver = Main.users.get(item.getEventHeaders().get("author"));
                    Lobby lobby = Lobby.search(values[1]);
                    if (lobby.disconnect(leaver)) {
                        /*EventMessage message = new EventMessage();
                        message.write("succ leave-lobby");
                        leaver.sendMessage(message);*/
                    } else {
                        EventMessage message = new EventMessage();
                        message.write("err leave-lobby unknown");
                        leaver.sendMessage(message);
                    }
                    break;
                }

                case "create-lobby": {
                    User creator = Main.users.get(item.getEventHeaders().get("author"));
                    Lobby lobby = Lobby.create(creator, 10, 6, 4);
                    if (lobby == null) {
                        EventMessage message = new EventMessage();
                        message.write("err create-lobby");
                        creator.sendMessage(message);
                        return;
                    }
                    EventMessage message = new EventMessage();
                    message.write("succ create-lobby " + lobby.getId());
                    creator.sendMessage(message);
                    EventMessage notification = new EventMessage();
                    notification.getEventHeaders().addHeader("author", creator.getId());
                    notification.write("friend create-lobby " + lobby.getId());
                    creator.notifyFriends(notification);
                    break;
                }

                case "join-lobby": {
                    User joiner = Main.users.get(item.getEventHeaders().get("author"));
                    Lobby lobby = Lobby.search(values[1]);
                    if (lobby == null) {
                        EventMessage message = new EventMessage();
                        message.write("err join-lobby no-such-lobby");
                        joiner.sendMessage(message);
                        return;
                    }
                    if (lobby.join(joiner)) {
                        EventMessage message = new EventMessage();
                        message.write("succ join-lobby");
                        joiner.sendMessage(message);
                    } else {
                        EventMessage message = new EventMessage();
                        message.write("err join-lobby unknown");
                        joiner.sendMessage(message);
                    }
                    break;
                }

                case "ping": {
                    EventMessage message = new EventMessage();
                    message.getEventHeaders().addHeader("code", String.valueOf(200));
                    message.write("pong");
                    String og = item.getEventHeaders().get("author");
                    Main.users.get(og).sendMessage(message);
                    break;
                }

                case "friend-accept": {
                    try {
                        Database db = Main.getDatabaseConnection();
                        Long senderId = Long.valueOf(values[1]), accepterId = Long.valueOf(item.getEventHeaders().get("author"));
                        Results results = db.execute(new Query("select * from friend_requests where ??=? and ??=?",
                                "sender", senderId, "receiver", accepterId));
                        if (results.length() > 0) {
                            db.execute(new Query("insert into friends (accountID1, accountID2) values (?, ?)",
                                    senderId, accepterId));
                            db.execute(new Query("delete from friend_requests where ??=? and ??=?",
                                    "sender", senderId, "receiver", accepterId));
                            User og = Main.users.get(String.valueOf(senderId));
                            EventMessage message;
                            if (og != null) {
                                message = new EventMessage();
                                message.write("friend-accept " + accepterId);
                                og.sendMessage(message);
                            } else {
                                db.execute(new Query("insert into notifications (accountID, notification) values(?, ?)",
                                        senderId, "friend-accept " + String.valueOf(accepterId)));
                            }
                            message = new EventMessage();
                            message.write("succ friend-accept friend-added");
                            Main.users.get(String.valueOf(accepterId)).sendMessage(message);
                        } else {
                            // no such friend request
                        }
                    } catch (SQLException e) {
                        User user = Main.users.get(item.getEventHeaders().get("author"));
                        EventMessage message = new EventMessage();
                        user.sendMessage(message);
                        message.write("err friend-request database-generic");
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        User user = Main.users.get(item.getEventHeaders().get("author"));
                        EventMessage message = new EventMessage();
                        message.write("err friend-request missing-params");
                        user.sendMessage(message);
                    }
                    break;
                }

                case "friend-request": {
                    Database db = Main.getDatabaseConnection();
                    try {
                        User sender = Main.users.get(item.getEventHeaders().get("author"));
                        Long id = Long.valueOf(values[1]), sId = Long.valueOf(item.getEventHeaders().get("author"));
                        Results results = db.execute(new Query("select * from friends where (??=? and ??=?) or (??=? and ??=?)",
                                "accountID1", id, "accountID2", sId, "accountID1", sId, "accountID2", id));
                        if (results.length() > 0) {
                            EventMessage message = new EventMessage();
                            message.write("err friend-request already-friends");
                            sender.sendMessage(message);
                            return;
                        }
                        results = db.execute(new Query("select * from friend_requests where ??=? and ??=?",
                                "sender", sId, "receiver", id));
                        if (results.length() > 0) {
                            EventMessage message = new EventMessage();
                            message.write("err friend-request already-sent");
                            sender.sendMessage(message);
                            return;
                        }
                        results = db.execute(new Query("select * from friend_requests where ??=? and ??=?",
                                "sender", id, "receiver", sId));
                        if (results.length() > 0) {
                            db.execute(new Query("insert into friends (accountID1, accountID2) values (?, ?)",
                                    id, sId));
                            db.execute(new Query("delete from friend_requests where ??=? and ??=?",
                                    "sender", id, "receiver", sId));
                            EventMessage message = new EventMessage();
                            message.write("succ friend-request friend-added");
                            sender.sendMessage(message);
                            User og = Main.users.get(String.valueOf(id));
                            if (og != null) {
                                message = new EventMessage();
                                message.write("friend-accept " + sId);
                                og.sendMessage(message);
                            } else {
                                og = new User(String.valueOf(id), null);
                                Notification n = new Notification(1);
                                n.setAuthor(String.valueOf(sId));
                                og.pushNotification(n);
                            /*db.execute(new Query("insert into notifications (accountID, notification) values(?, ?)",
                                    id, "friend-accept " + String.valueOf(sId)));*/
                            }
                            return;
                        }
                        results = db.execute(new Query("select * from accounts where ??=?",
                                "accountID", id));
                        if (results.length() == 0) {
                            EventMessage message = new EventMessage();
                            message.write("err friend-request not-exist");
                            sender.sendMessage(message);
                            return;
                        }
                        db.execute(new Query("insert into friend_requests (sender, receiver) values(?, ?)",
                                sId, id));
                        EventMessage message = new EventMessage();
                        message.write("succ friend-request sent " + id);
                        sender.sendMessage(message);
                    } catch (SQLException e) {
                        User user = Main.users.get(item.getEventHeaders().get("author"));
                        EventMessage message = new EventMessage();
                        user.sendMessage(message);
                        message.write("err friend-request database-generic");
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        User user = Main.users.get(item.getEventHeaders().get("author"));
                        EventMessage message = new EventMessage();
                        message.write("err friend-request missing-params");
                        user.sendMessage(message);
                    }
                    break;
                }

                default: {
                    EventMessage message = new EventMessage();
                    message.getEventHeaders().addHeader("code", String.valueOf(400));
                    message.write("err_unknown_action");
                    String og = item.getEventHeaders().get("author");
                    Main.users.get(og).sendMessage(message);
                }
            }
        } catch (Exception e) {
            EventMessage message = new EventMessage();
            message.getEventHeaders().addHeader("code", "500");
            message.write("err_unknown_error");;
            Main.users.get(item.getEventHeaders().get("author")).sendMessage(message);
        }
    }
}
