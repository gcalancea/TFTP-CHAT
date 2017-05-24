package tftp_server;


import chat.MyServer;


import javax.swing.*;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

import java.net.URL;
import java.util.ArrayList;

public class TreeIcon extends JPanel
        implements TreeSelectionListener {
    public static AnotherTree tree;
    public static ArrayList<FileItem> allFiles = new ArrayList<>();
    public static ArrayList<String> fileNames;
    public static DefaultMutableTreeNode top;
    public JScrollPane treeView;

    public TreeIcon() {
        super(new GridLayout(1, 0));
        fileNames= MyServer.get_available_files();
        for(int i=0;i<fileNames.size();i++){
            FileItem newItem = new FileItem(fileNames.get(i));
            allFiles.add(newItem);
        }

        top = new DefaultMutableTreeNode("TFTP Server Info");
        createNodes(top, allFiles);

        tree = new AnotherTree(top);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addTreeSelectionListener(this);

        ImageIcon leafIcon = createImageIcon("middle.gif");
        if (leafIcon != null) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(leafIcon);
            tree.setCellRenderer(renderer);
        } else {
            System.err.println("Leaf icon missing; using default.");
        }

        treeView = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension minimumSize = new Dimension(100, 20);
        treeView.setMinimumSize(minimumSize);


        this.add(treeView);
    }

    public static void addNodes(String fileName) {
        DefaultMutableTreeNode file = null;
        FileItem f = new FileItem(fileName);
        if(!allFiles.contains(f)){
            allFiles.add(f);
            file = new DefaultMutableTreeNode(f);
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
            root.add(file);
            model.reload(root);
        }
    }


    private void createNodes(DefaultMutableTreeNode top, ArrayList<FileItem> it) {
        DefaultMutableTreeNode nd = null;

        for (int i = 0; i < it.size(); i++) {
            nd = new DefaultMutableTreeNode(it.get(i));
            top.add(nd);
        }


    }

    protected static ImageIcon createImageIcon(String path) {
        URL imgURL = TreeIcon.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
    }

}