package com.SCI.game;

public enum PlayerClass {
    ARCHER(1), KNIGHT(2), WIZARD(3);

    PlayerClass(int value) {
        this.value = value;
    }

    public int intValue() {
        return value;
    }

    private final int value;
}
