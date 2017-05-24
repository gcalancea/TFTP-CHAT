package tftp_server;

import java.awt.*;

import javax.swing.*;

public class ProgressBarTest extends JFrame {
	private static final long serialVersionUID = 1L;
	private Container pane;
	private TreeIcon newTree;
	private JPanel progressBars = new JPanel();

	public ProgressBarTest() {

		super("TFTP Manager");
		pane = this.getContentPane();

        newTree = new TreeIcon();
        newTree.setOpaque(true);

        progressBars.setLayout(new BoxLayout(progressBars, BoxLayout.Y_AXIS));
        progressBars.setOpaque(true);


        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(newTree);
        splitPane.setBottomComponent(progressBars);

        Dimension minimumSize = new Dimension(100, 50);
        newTree.setMinimumSize(minimumSize);
        progressBars.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100);

        pane.add(splitPane);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));



		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(500, 600));

		this.pack();
        this.setVisible(true);
	}

	public class dynamicProgressBar {
		private int currentSize;
		private boolean running;
		private Thread barThread;

		
		public dynamicProgressBar(String fileName, int fileSize) {
			currentSize = 0;
			barThread = new Thread() {

				public void run() {
					JProgressBar progressBar = new JProgressBar(0, fileSize);
					progressBar.setString(fileName);
					progressBar.setStringPainted(true);
					progressBars.add(progressBar);
					progressBars.validate();
					progressBars.repaint();
					while (currentSize != fileSize) {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (currentSize != progressBar.getValue()) {
							progressBar.setValue(currentSize);
						}
					}
					progressBar.setValue(currentSize);
				}
			};
			barThread.start();
		};
		

		public dynamicProgressBar(String fileName){
			currentSize = 0;
			running = true;
			barThread = new Thread() {

				public void run() {
					JProgressBar progressBar = new JProgressBar();
					progressBar.setString(fileName);
					progressBar.setIndeterminate(true);
					progressBar.setStringPainted(true);
                    progressBars.add(progressBar);
                    progressBars.validate();
                    progressBars.repaint();
					while (running) {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (currentSize != progressBar.getValue()) {
							progressBar.setValue(currentSize);
						}
					}
					progressBar.setMaximum(currentSize);
					progressBar.setValue(currentSize);
					progressBar.setIndeterminate(false);
				}
			};
			barThread.start();
		}

		public void incValue(int newFileSize) {
			currentSize += newFileSize;
		}
		
		public void done(){
			running = false;
		}
		
	}

	public dynamicProgressBar addProgressBar(String fileName, int fileSize) {

		return new dynamicProgressBar(fileName, fileSize);
	}
	
	public dynamicProgressBar addProgressBar(String fileName) {

		return new dynamicProgressBar(fileName);
	}
}