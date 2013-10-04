package com.leapcenter.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {

	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame("Chat");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);
	Socket socket;

	public Client() {

		// Layout GUI
		textField.setEditable(false);
		messageArea.setEditable(false);
		frame.getContentPane().add(textField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();

		// Add Listeners
		textField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				out.println(textField.getText());
				textField.setText("");
			}
		});
	}

	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame,
				"Enter IP Address of the Server:", "Welcome to the Chat Room",
				JOptionPane.QUESTION_MESSAGE);
	}

	private String getName() {
		return JOptionPane.showInputDialog(frame, "Enter a screen name:",
				"Screen name selection", JOptionPane.PLAIN_MESSAGE);
	}

	private void run() throws IOException {

		// Make connection and initialize streams
		String serverAddress = getServerAddress();
		System.out.println("Server Address:" + serverAddress);
		socket = new Socket(serverAddress, 9001);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// Process all messages from server, according to the protocol.
		while (true) {
			if (socket.isClosed())
				break;
			String line = in.readLine();
			System.out.println(line);
			if (line.startsWith("SUBMITNAME")) {
				out.println(getName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(line.substring(8) + "\n");
			} else if (line.startsWith("EXIT")) {
				messageArea.append(line.substring(5) + "\n");
			}

		}
	}

	public void attachHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				out.println("TERMINATE");
				try {
					socket.close();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) {
		try{
		Client client = new Client();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.attachHook();
		client.run();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}