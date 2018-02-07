package com.SCI.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class Dispenser <T extends Disposable & Closeable> {
    public Dispenser(int interval) {
        final Dispenser<T> self = this;
        managedObjects = new HashMap<>();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LinkedList<String> keys = new LinkedList<>();
                synchronized (self) {
                    for (Map.Entry<String, T> objects : managedObjects.entrySet()) {
                        if (objects.getValue().isDisposable()) {
                            try {
                                objects.getValue().close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            keys.add(objects.getKey());
                        }
                    }
                    for (String key : keys) {
                        managedObjects.remove(key);
                    }
                }
            }
        }, interval * 60 * 1000, interval * 60 * 1000);
    }

    public Dispenser() {
        this(15);
    }

    public T get(String key) {
        return managedObjects.get(key);
    }

    public synchronized void put(String key, T object) {
        managedObjects.put(key, object);
    }

    public boolean has(String key) {
        return managedObjects.containsKey(key);
    }

    public synchronized boolean putIfAbsent(String key, T object) {
        if (has(key)) {
            return false;
        } else {
            managedObjects.put(key, object);
            return true;
        }
    }

    public void kill() {
        timer.cancel();
        timer = null;
    }

    public synchronized void remove(String key) {
        managedObjects.remove(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : managedObjects.keySet()) {
            sb.append("Id: ").append(s).append("\n");
        }
        return sb.toString();
    }

    private Timer timer;
    private HashMap<String, T> managedObjects;
}
