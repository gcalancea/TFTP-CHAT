package chat;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by georgiana on 22/05/2017.
 */
public class Chat extends JFrame {
    private JPanel mainPanel;
    private JButton sendButton;
    private JTextField inputField;
    private JList chatMessages;
    private JSplitPane message;
    private JList friendList;
    private JLabel available;
    private JList serverAnswers;

    private ArrayList<String> activeUsers;


    private static DefaultListModel<String> friendModel = new DefaultListModel<>();


    public void setActiveUsers(ArrayList<String> users){
        activeUsers = users;
    }

    public Chat(String username) {
        this.add(mainPanel);

        sendButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
//        for(int i=0;i<activeUsers.size();i++){
//            friendModel.addElement(activeUsers.get(i));
//        }
//        friendList.setModel(friendModel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(600, 700));

        this.pack();
        this.setVisible(true);

    }

    public void addFriend(String username) {
        friendModel.addElement(username);
    }


    public void setData(MessageData data) {
        inputField.setText(data.getTextMessage());
    }

    public void getData(MessageData data) {
        data.setTextMessage(inputField.getText());
    }

    public boolean isModified(MessageData data) {
        if (inputField.getText() != null ? !inputField.getText().equals(data.getTextMessage()) : data.getTextMessage() != null)
            return true;
        return false;
    }

}
