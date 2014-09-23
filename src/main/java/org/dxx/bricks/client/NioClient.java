package org.dxx.bricks.client;

import static org.dxx.bricks.Config.C_FILE;
import static org.dxx.bricks.Config.C_WRITE_BUFFER;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dxx.bricks.Brick;
import org.dxx.bricks.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioClient implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(NioClient.class);
	
	private String serverHost;
	private int serverPort;
	
    private Selector selector;
    
    private Codec decoder = new Codec();

    private State state = State.WRITE;
    
    private static final String ENTER = "\r\n";
    
    BrickWriter bw = new BrickWriter(C_FILE, C_WRITE_BUFFER);
    
    private ByteBuffer request;
    
    enum State {
    	READ, WRITE
    }
    
    public NioClient(String serverHost, int serverPort) {
    	this.serverHost = serverHost;
    	this.serverPort = serverPort;
    	request = ByteBuffer.allocateDirect(1);
    	request.put(Byte.valueOf("1"));
    }
    
    

	@Override
	public void run() {
		try {
			init();
			listen();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}



	public NioClient init() throws IOException{
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        selector=Selector.open();
        channel.connect(new InetSocketAddress(serverHost, serverPort));
        channel.register(selector, SelectionKey.OP_CONNECT);
        return this;
    }
    
    public void listen() throws IOException{
    	long start = System.currentTimeMillis();
        while(true){
            selector.select();
            Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
            while(ite.hasNext()){
                SelectionKey key = ite.next();
                ite.remove();
                if(key.isConnectable()){
                    SocketChannel channel=(SocketChannel)key.channel();
                    if(channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    logger.debug("Channel created!");
                } else if(state == State.READ && key.isReadable()){
                	SocketChannel channel = (SocketChannel)key.channel();
                	ByteBuffer buffer = ByteBuffer.allocate(512);
                    channel.read(buffer);
                    buffer.flip();
                    
                    List<Brick> bricks = new ArrayList<Brick>();
                    decoder.decode(buffer, bricks);
                    
                    for(Brick b : bricks) {
                    	if(b.getLineNum() == -1) {
                    		logger.debug("Mission completed. Cost {} seconds.", 
                    				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start));
                    		bw.flushAndClose();
                    		return;
                    	}
                    	bw.write(b.getContent() + ENTER);
                    }
                    state = State.WRITE;
                } else if(state == State.WRITE && key.isWritable()){
                	SocketChannel channel = (SocketChannel)key.channel();
                	request.flip();
                	channel.write(request);
                	state = State.READ;
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
    	String host = "127.0.0.1";
    	int conns = 2;
    	try {
	    	if(args.length > 0 && args[0] != null) {
	    		host = args[0];
	    	}
	    	if(args.length > 1 && args[1] != null) {
	    		C_FILE = args[1];
	    	}
	    	if(args.length > 2 && args[2] != null) {
	    		conns = Integer.valueOf(args[2]);
	    	}
	    	
	    	new File(C_FILE).delete();
	    	for(int i = 0 ; i < conns; i++) {
	    		new Thread(new NioClient(host, 9981)).start();
	    	}
    		logger.info("Nio client is running! server : {}, targetFile : {}, conn : {}", 
    				new Object[] {host, C_FILE, conns});
    	} catch (Exception e) {
    		logger.error("Err : " + e.getMessage(), e);
    	}
	}
   
}