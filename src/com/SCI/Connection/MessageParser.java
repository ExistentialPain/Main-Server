package com.SCI.Connection;

import java.util.UUID;

public interface MessageParser {
    ParserResponse parse(UUID authorID, Message message);
    void setUserData(Object o) throws IllegalArgumentException;
}
