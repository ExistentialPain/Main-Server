package com.SCI.net;

import com.SCI.Main;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EventServer implements Closeable {
    public EventServer(int port) throws IOException {
        sock = new ServerSocket(port);
    }

    public void start() {
        new Thread(() -> {
            while (!sock.isClosed()) {
                try {
                    Socket socket = sock.accept();
                    EventMessage message = EventMessage.get(socket.getInputStream());
                    System.out.println("Message gotted");
                    User user = Main.users.get(message.getEventHeaders().get("author"));
                    System.out.println("The author is: " + message.getEventHeaders().get("author"));
                    if (!user.equals(new User(message.getEventHeaders().get("author"),
                            message.getEventHeaders().get("token")))) {
                        System.out.println("Unauthorised");
                        EventMessage response = new EventMessage();
                        response.getEventHeaders().addHeader("code", "401");
                        response.write("err_unauthorised");
                        socket.getOutputStream().write(response.toString().getBytes());
                        socket.close();
                    } else {
                        Main.users.get(message.getEventHeaders().get("author")).bindEventSocket(socket);
                        //EventSocket.create(socket, AuthToken.get(message.getEventHeaders().get("token")));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void close() throws IOException {
        sock.close();
    }

    private ServerSocket sock;
}
