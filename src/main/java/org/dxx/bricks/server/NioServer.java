package org.dxx.bricks.server;

import static org.dxx.bricks.Config.S_FILE;
import static org.dxx.bricks.Config.S_READ_BUFFER;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.dxx.bricks.Brick;
import org.dxx.bricks.Codec;
import org.dxx.bricks.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServer {
	private static final Logger logger = LoggerFactory.getLogger(NioServer.class);

	private Selector selector;
	
	private Codec encoder = new Codec();
	
	private BrickReader brickReader = new BrickReader(S_FILE, S_READ_BUFFER);
	
	private boolean finished = false;
	
	private Thread monitor = new Thread(new Monitor(brickReader));

	public NioServer init(int port) throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		monitor.start();
		return this;
	}

	public void listen() throws IOException {
		while (true) {
			selector.select();
			Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = ite.next();
				ite.remove();
				if (key.isAcceptable()) {
					logger.debug("New channel accepted.");
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					SocketChannel channel = server.accept();
					channel.configureBlocking(false);
					channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}  else if (key.isReadable()) {
					if(!finished) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer bb = ByteBuffer.allocate(2);
						channel.read(bb);
						if(bb.hasRemaining()) {
							byte[] request = new byte[bb.remaining()];
							bb.get(request);
							Brick brick = brickReader.getNext();
							if(brick != null) {
								channel.write(ByteBuffer.wrap(encoder.encode(brick)));
							} else {
								Brick signal = new Brick();
								signal.setLineNum(-1);
								signal.setContent("");
								channel.write(ByteBuffer.wrap(encoder.encode(signal)));
								finished = true;
								logger.debug("Finished.");
								monitor.interrupt();
								return;
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		new NioServer().init(9981).listen();
	}
}