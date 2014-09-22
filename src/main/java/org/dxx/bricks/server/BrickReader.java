package org.dxx.bricks.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.dxx.bricks.Brick;

public class BrickReader {
	private int bufferSize;
	private ByteBuffer rBuffer;

	private File f;
	private FileChannel channel;
	
	private StringBuffer tmp = new StringBuffer();
	private int e = 0;
	
	AtomicInteger lineNum = new AtomicInteger(0);

	public BrickReader(String file, int bufferSize) {
		this.bufferSize = bufferSize;
		rBuffer = ByteBuffer.allocate(bufferSize);
		f = new File(file);
		try {
			channel = new RandomAccessFile(f, "r").getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized Brick getNext() throws IOException {
		if(tmp.length() > 0 && (e = tmp.indexOf(String.valueOf((char) 13))) != -1) {
			String line = tmp.substring(0, e);
			tmp.delete(0, e + 2);// 换行符
			return new Brick(lineNum.incrementAndGet(), line);
		} 
		
		if(channel.read(rBuffer) != -1) {
			int size = rBuffer.position();
			byte[] bs = new byte[Math.min(size , bufferSize)];
			rBuffer.rewind();
			rBuffer.get(bs);
			rBuffer.clear();
			tmp.append(new String(bs, 0, size));
			return getNext();
		}
		return null;
	}
	
	public AtomicInteger getLineNum() {
		return lineNum;
	}

	public static void main(String args[]) throws Exception {
		BrickReader reader = new BrickReader("E:\\temp.txt", 30);
		Brick brick = null;
		while ((brick = reader.getNext()) != null) {
			System.out.println(brick);
		}
	}

}