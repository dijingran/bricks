package org.dxx.bricks.client;

import static org.dxx.bricks.Config.C_FILE;
import static org.dxx.bricks.Config.C_WRITE_BUFFER;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
	private static final Logger logger = LoggerFactory.getLogger(Client.class);

	private static final BrickWriter bw = new BrickWriter(C_FILE, C_WRITE_BUFFER);
	
	public static void main(String args[]) throws Exception {
		String host = "127.0.0.1";
		int port = 8899;
		Socket client = new Socket(host, port);
		Writer writer = new OutputStreamWriter(client.getOutputStream());
		BufferedReader br = new BufferedReader(new InputStreamReader(
				client.getInputStream()));
		boolean finished = false;
		while (true) {
			writer.write("a\n");
			writer.flush();
			String temp;
			while ((temp = br.readLine()) != null) {
				if (finished = "finished".equals(temp)) {
					logger.debug("Finished!");
				} else {
					bw.write(temp + "\r\n");
				}
				break;
			}
			if (finished) {
				break;
			}
		}
		writer.close();
		br.close();
		client.close();
	}
}
