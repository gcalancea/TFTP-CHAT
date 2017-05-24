package chat;

import java.util.ArrayList;
import java.util.List;

public class MessageData {
    private String textMessage;


    @Override
    public String toString() {
        return "MessageData{}";
    }

    public MessageData() {

    }

    public static void main(String[] args){


    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(final String textMessage) {
        this.textMessage = textMessage;
    }
}