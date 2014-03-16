import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI implements ActionListener, KeyListener
{
	//this class implements the action listener and key listener classes
	//declarations
	//i hope this works
	JFrame frame, aboutFrame;
	JMenuItem newItem, openItem, saveItem, saveAsItem, undoItem, redoItem, aboutItem, syncItem, exitItem;
	JTextPane textPane;
	JFileChooser fileChooser;
	File openedFile;
	JPanel statusBar;
	String status = "Idle", oldStatus = "";
	JLabel statusLabel, wordCountLabel;
	boolean isSaved = true, timerRunning;
	Timer timer;

	public void aboutFrame()
	{

		if (aboutFrame == null)
		{
			
			aboutFrame = new JFrame("About Filer");
			aboutFrame.setLocationRelativeTo(frame);
			aboutFrame.setSize(300, 200);
			aboutFrame.setResizable(false);
			aboutFrame.setVisible(true);
		}
		else aboutFrame.toFront();
	}

	public GUI()
	//constructor
	{
		frame = new JFrame();
		//creates a new j frame
		timer = new Timer();
		//timer for use in the idle state display

		try
		{
			//makes the window look like the native systems windows
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			// outputs errors if any occur
			e.printStackTrace();
		}

		// statusBar
		//adding GUI elements
		statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.LINE_AXIS));
		//line axis is a horizontal layout
		statusBar.setPreferredSize(new Dimension(frame.getWidth(), 20));
		//as wide as the frame, and 20 pixels tall
		statusLabel = new JLabel("Status: Idle");
		wordCountLabel = new JLabel("Words: 0");
		statusBar.add(Box.createHorizontalStrut(10));
		statusBar.add(statusLabel);
		statusBar.add(Box.createHorizontalStrut(10));
		statusBar.add(new JLabel("|"));
		statusBar.add(Box.createHorizontalStrut(10));
		statusBar.add(wordCountLabel);
		//populate the status bar

		frame.add(statusBar, BorderLayout.SOUTH);
		//adds the status bar to the frame on the south side (bottom)

		// textPane
		//create text pane
		textPane = new JTextPane();
		//add key listener that will allow user to type onto the textpane 
		textPane.addKeyListener(this);
		//set font
		Font font = new Font("Arial", 10, 16);
		textPane.setFont(font);
		//create scroll pane and put the text pane into it
		JScrollPane scrollPane = new JScrollPane(textPane);
		frame.add(scrollPane);

		//create menu bar
		JMenuBar menuBar = new JMenuBar();

		// fileMenu
		JMenu fileMenu = new JMenu("File");
		
		//populate menu bar item "File"
		newItem = new JMenuItem("New");
		newItem.addActionListener(this);
		newItem.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.CTRL_DOWN_MASK));
		
		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
		openItem.setAccelerator(KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK));

		saveItem = new JMenuItem("Save");
		saveItem.addActionListener(this);
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK));

		saveAsItem = new JMenuItem("Save As");
		saveAsItem.addActionListener(this);
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));

		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		exitItem.setAccelerator(KeyStroke.getKeyStroke('E', KeyEvent.CTRL_DOWN_MASK));
		
		//add populations to the menu
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// editMenu
		JMenu editMenu = new JMenu("Edit");
		
		//populate menu button "edit"
		undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', KeyEvent.CTRL_DOWN_MASK));

		redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', KeyEvent.CTRL_DOWN_MASK));

		editMenu.add(undoItem);
		editMenu.add(redoItem);

		// networkMenu
		//populate network button
		JMenu networkMenu = new JMenu("Network");
		syncItem = new JMenuItem("Sync");
		networkMenu.add(syncItem);

		// helpMenu
		//populate help menu
		JMenu helpMenu = new JMenu("Help");
		aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		// add menus to menuBar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(networkMenu);
		menuBar.add(helpMenu);

		frame.addWindowListener(new WindowAdapter()
		{
			//sets up the system reponce to the exit button being pressed
			public void windowClosing(WindowEvent we)
			{
				//this will check if the current entries in the text area has been saved, if not it will bring up the save window
				//(save, no save, cancel)
				//will return true if saved, a save is made or the user selects not to save
				//return false if user presses cancel
				if (saveCheck()) System.exit(0);
			}
		});
		
		
		//WHAT? daniel please comment, is this just prep for the file chooser to be called? 
		fileChooser = new JFileChooser();
		FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("TXT files", "txt");
		FileNameExtensionFilter javaFilter = new FileNameExtensionFilter("JAVA files", "java");
		fileChooser.addChoosableFileFilter(txtFilter);
		fileChooser.addChoosableFileFilter(javaFilter);

		//sets the frame size and starting title, center location,
		//and declares that the "x" button will do nothing initially
		frame.setTitle("Filer - Untitled.txt");
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
		
	}
	
	//this method is used to update the status label on the lower status bar
	private void updateStatus(String s)
	{
		statusLabel.setText("Status: " + s);
	}
	
	//when the timer expires the idle command is set to idle
	public class Task extends TimerTask
	{
		public void run()
		{
			updateStatus("Idle");
		}
	}

	//this is the method called to start the idle timer for 10 seconds, (10000 miliseconds)
	private void startTimer()
	{
		timer.scheduleAtFixedRate(new Task(), 0, 10_000);
		timerRunning = true;
	}

	// returns true if you should continue (closing or making a new document), false if the user wants to cancel the action
	public boolean saveCheck()
	{
		boolean isDone = true;
		//issaved boolean is updated through out the program 
		if (!isSaved)
		{
			//dialouge box appears to prompt user
			int choiceVal = JOptionPane.showConfirmDialog(frame, "Would you like to save?");
			if (choiceVal == JOptionPane.YES_OPTION)
			{
				//user chooses to save
				if (!saveFile()) isDone = false;
			}
			//user chooses cancel
			else if (choiceVal == JOptionPane.CANCEL_OPTION) isDone = false;
		}
		//if the user chooses no the method returns that the data is saved (true)
		return isDone;
	}
	
	//reactions to button presses by the user
	public void actionPerformed(ActionEvent e)
	{
		//open file button
		if (e.getSource() == openItem)
		{
			//change status
			updateStatus("Opening file");
			//opens the file chooser
			int returnVal = fileChooser.showOpenDialog(fileChooser);

			if (returnVal == fileChooser.APPROVE_OPTION)
			{
				//returns the file to be opened
				openedFile = fileChooser.getSelectedFile();

				//attempt to read file
				try
				{
					readFile(openedFile);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		//remaining buttons call connected methods
		else if (e.getSource() == saveAsItem)
		{
			saveFileAs();
		}
		else if (e.getSource() == saveItem)
		{
			saveFile();
		}
		else if (e.getSource() == newItem)
		{
			newFile();
		}
		else if (e.getSource() == exitItem)
		{
			if (saveCheck()) System.exit(0);
		}
		else if (e.getSource() == aboutItem)
		{
			aboutFrame();
		}

	}

	//original state of program at new file
	private void newFile()
	{
		if (saveCheck())
		{
			textPane.setText("");
			updateStatus("New File Created");
			frame.setTitle("Filer - Untitled.txt");
		}
	}

	//saving file method
	private boolean saveFile()
	{
		updateStatus("Saving File");

		//if the file has not been saved before call the SaveFileAs method
		if (openedFile == null) return saveFileAs();
		else
		{
			//if it is an existing file then the file is simply written to the drive
			writeFile(openedFile);
			return true;
		}
	}

	// returns true if the file was saved, or if the user doesn't want to save. Should probably be refactored..
	private boolean saveFileAs()
	{
		updateStatus("Saving File");

		int returnVal = fileChooser.showSaveDialog(fileChooser);
		//opens window
		if (returnVal == fileChooser.APPROVE_OPTION)
			//user chooses to save item
		{
			writeFile(fileChooser.getSelectedFile());
			return true;
		}
		else if (returnVal == fileChooser.CANCEL_OPTION) return false; // cancel option
		return true;

	}

	//read text from file at print to the text area
	private void readFile(File file) throws Exception
	{
		//creates string
		String contents = "";
		//creates buffered reader
		BufferedReader inputStream = null;

		//attempts to read line into the string
		try
		{
			inputStream = new BufferedReader(new FileReader(file));

			String l;
			while ((l = inputStream.readLine()) != null)
			{
				contents += (l + "\r\n"); // \n required for newlines, \r (carriage return) required for notepad
			}
			textPane.setText(contents);
			updateStatus(file.getName() + " opened sucessfully");
			frame.setTitle("Filer - " + file.getName());
		}
		//prints error
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//close stream
		finally
		{
			if (inputStream != null) inputStream.close();
		}
	}

	//write to existing file
	private void writeFile(File file)
	{
		try
		{

			// dealing with saving with extensions
			Pattern p = Pattern.compile("[^.]*");
			Matcher m = p.matcher(file.getPath());
			m.find();

			//get the .***
			String descrip = fileChooser.getFileFilter().getDescription().substring(0, 3);
			String extension = "";
			if (descrip.equals("TXT")) extension = ".txt";
			else if (descrip.equals("JAV")) extension = ".java";

			//file name
			file = new File(m.group(0) + extension);

			//outputs test pane contents to the output stream
			PrintWriter outputStream = new PrintWriter(new FileWriter(file));
			outputStream.print(textPane.getText());
			outputStream.close();
			updateStatus(file.getName() + " Saved sucessfully");
			frame.setTitle("Filer - " + file.getName());
			isSaved = true;
		}
		catch (Exception ex)
		{

		}
	}

	//word count updater
	private void updateWordLabel()
	{
		wordCountLabel.setText("Words: " + findWordCount());
	}

	//word count
	private int findWordCount()
	{
		return textPane.getText().split("\\s+").length;
	}

	//detects if the user is typing
	public void keyPressed(KeyEvent e)
	{
		updateStatus("Typing");
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			updateWordLabel();
		}
	}

	//when key released, start idle timer
	public void keyReleased(KeyEvent e)
	{
		isSaved = false;
		startTimer();
	}

	//method implement
	public void keyTyped(KeyEvent e)
	{

	}
}
