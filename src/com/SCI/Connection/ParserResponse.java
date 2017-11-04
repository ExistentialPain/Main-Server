package com.SCI.Connection;

import java.util.UUID;

public class ParserResponse {
    public ParserResponse(Message message, boolean isBroadcast, UUID target) {
        this.message = message;
        this.isBroadcast = isBroadcast;
        this.target = target;
    }

    public ParserResponse(Message message, boolean isBroadcast) {
        this(message, isBroadcast, null);
    }

    public ParserResponse(Message message, UUID target) {
        this(message, false, target);
    }

    public final boolean isBroadcast;
    public final Message message;
    public final UUID target;
}
