package org.dxx.bricks.server;

import static org.dxx.bricks.Config.S_FILE;
import static org.dxx.bricks.Config.S_READ_BUFFER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.dxx.bricks.Brick;
import org.dxx.bricks.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	static final Logger logger = LoggerFactory.getLogger(Server.class);
	
	private static final BrickReader brickReader = new BrickReader(S_FILE, S_READ_BUFFER);
	
	private static Thread monitor = new Thread(new Monitor(brickReader));
	
	@SuppressWarnings("resource")
	public static void main(String args[]) throws IOException {
		int port = 8899;
		ServerSocket server = new ServerSocket(port);
		monitor.start();
		while (true) {
			Socket socket = server.accept();
			new Thread(new Task(socket)).start();
		}
	}

	/**
	 * 用来处理Socket请求的
	 */
	static class Task implements Runnable {

		private Socket socket;

		public Task(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				handleSocket();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 跟客户端Socket进行通信
		 * 
		 * @throws Exception
		 */
		private void handleSocket() throws Exception {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			Writer writer = new OutputStreamWriter(socket.getOutputStream());
			while (br.readLine() != null) {
				Brick b = brickReader.getNext();
				if(b != null) {
					writer.write(b.getContent());
				} else {
					writer.write("finished");
					monitor.interrupt();
				}
				writer.write("\n");
				writer.flush();
			}
			writer.close();
			br.close();
			socket.close();
		}
	}
}
