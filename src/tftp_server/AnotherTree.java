package tftp_server;


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;


public class AnotherTree extends JTree {
    public AnotherTree(DefaultMutableTreeNode node) {
        super(node);
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object nodeInfo = node.getUserObject();

        if(!nodeInfo.toString().contains("TFTP")){
            FileItem file = (FileItem) nodeInfo;
            return file.getName();
        }
        return value.toString();
    }
}
