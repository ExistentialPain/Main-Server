package com.SCI.util;

import java.util.Iterator;
import java.util.LinkedList;

public class LimitedList<T> implements Iterable<T> {
    public LimitedList(int maxSize) {
        this.maxSize = maxSize;
        list = new LinkedList<>();
    }

    public synchronized boolean isFull() {
        return maxSize == list.size();
    }

    public synchronized boolean put(T object) {
        if (maxSize > list.size()) {
            list.add(object);
            return true;
        } else {
            return false;
        }
    }

    public synchronized T get(int index) {
        return list.get(index);
    }

    public synchronized boolean remove(T object) {
        if (!isFull() && list.contains(object)) {
            list.remove(object);
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean remove(int index) {
        if (!isFull()) {
            list.remove(index);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public LinkedList<T> asList() {
        return list;
    }

    private int maxSize;
    private LinkedList<T> list;
}
