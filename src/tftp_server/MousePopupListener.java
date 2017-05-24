package tftp_server;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



public class MousePopupListener extends MouseAdapter {
    AnotherTree myTree;
    public MousePopupListener(AnotherTree tree){
        super();

        myTree = tree;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {

            int row = myTree.getClosestRowForLocation(e.getX(), e.getY());
            myTree.setSelectionRow(row);
        }
    }

}
