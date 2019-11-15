package com.vbanjan.inclassassignment02;

import android.os.Build;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Message implements Serializable {

    String messageId;
    String messageText;
    String messageTimeStamp;
    String userId;
    String userName;
    String chatRoomId;
    Boolean isTrip;
    Double FromLat;
    Double FromLng;
    String status;
    Double ToLat;
    Double ToLng;
    String FromLocationName;
    String ToLocationName;

    ArrayList<String> upvotedBy;

    public Message() {
    }

    public Message(String messageId, String messageText, String messageTimeStamp, String userId, String userName, String chatRoomId, Boolean isTrip, Double fromLat, Double fromLng, String status, Double toLat, Double toLng, String fromLocationName, String toLocationName, ArrayList<String> upvotedBy) {
        this.messageId = messageId;
        this.messageText = messageText;
        this.messageTimeStamp = messageTimeStamp;
        this.userId = userId;
        this.userName = userName;
        this.chatRoomId = chatRoomId;
        this.isTrip = isTrip;
        FromLat = fromLat;
        FromLng = fromLng;
        this.status = status;
        ToLat = toLat;
        ToLng = toLng;
        FromLocationName = fromLocationName;
        ToLocationName = toLocationName;
        this.upvotedBy = upvotedBy;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", messageText='" + messageText + '\'' +
                ", messageTimeStamp='" + messageTimeStamp + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", chatRoomId='" + chatRoomId + '\'' +
                ", isTrip=" + isTrip +
                ", FromLat=" + FromLat +
                ", FromLng=" + FromLng +
                ", status='" + status + '\'' +
                ", ToLat=" + ToLat +
                ", ToLng=" + ToLng +
                ", FromLocationName='" + FromLocationName + '\'' +
                ", ToLocationName='" + ToLocationName + '\'' +
                ", upvotedBy=" + upvotedBy +
                '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(messageId, message.messageId);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
