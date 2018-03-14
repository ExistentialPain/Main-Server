package com.SCI.game;

public abstract class GameMessageHandler {
    public GameMessageHandler(GameEnvironment env) {
        this.env = env;
    }
    public abstract GameResponse handle(String message, GameArgs data);
    protected final GameEnvironment env;
}
