package com.vbanjan.inclassassignment02;

public class Chatroom {
    String roomName, roomId;

    public Chatroom(String roomName, String roomId) {
        this.roomName = roomName;
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "roomName='" + roomName + '\'' +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
