package com.leapcenter.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Server {

	private static final int PORT = 9001;

	private static HashSet<String> names = new HashSet<String>();

	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running.");
		ServerSocket listener = new ServerSocket(PORT);
		try {
			int i = 0;
			while (true) {
				new Handler(listener.accept()).start();
				System.out.println("Thread " + i++);
			}
		} finally {
			listener.close();
		}
	}

	private static class Handler extends Thread {
		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				while (true) {
					out.println("SUBMITNAME");
					name = in.readLine();
					if (name == null) {
						return;
					}
					synchronized (names) {
						if (!names.contains(name)) {
							names.add(name);
							break;
						}
					}
				}

				out.println("NAMEACCEPTED");
				writers.add(out);
				while (true) {
					if (socket.isClosed())
						break;
					String input = in.readLine();

					if (input == null) {
						return;
					}
					if (input.equals("TERMINATE")) {
						System.out.println("Client " + name
								+ " is requesting for shutdown");
						if (name != null) {
							names.remove(name);
						}
						if (out != null) {
							writers.remove(out);
						}
						try {
							socket.close();
							for (PrintWriter writer : writers) {
								writer.println("EXIT " + name + ": "
										+ "left the chat room");

							}

						} catch (IOException e) {
						}
						break;

					} else {
						for (PrintWriter writer : writers) {
							writer.println("MESSAGE " + name + ": " + input);

						}
					}
				}
			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (name != null) {
					names.remove(name);
				}
				if (out != null) {
					writers.remove(out);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
