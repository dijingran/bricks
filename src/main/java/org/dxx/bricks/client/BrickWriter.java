package org.dxx.bricks.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BrickWriter {
	FileChannel channel;
	ByteBuffer bb;
	long index = 0;
	StringBuffer cache = new StringBuffer();
	int bufferSize; 

	public BrickWriter(String file, int bufferSize) {
		this.bufferSize = bufferSize;
		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void write(String brick) throws IOException {
		write(brick, false);
	}

	public void write(String brick, boolean flush) throws IOException {
		cache.append(brick);
		if (flush || cache.length() > bufferSize) {
			flush();
		}
	}

	public void flush() throws IOException {
		bb = channel.map(FileChannel.MapMode.READ_WRITE, index, cache.length());
		byte[] data = cache.toString().getBytes();
		bb.put(data);
		index += data.length;
		cache = new StringBuffer();
	}

	public void flushAndClose() throws IOException {
		flush();
		channel.force(false);
		channel.close();
	}

	public static void main(String args[]) throws Exception {
		BrickWriter bw = new BrickWriter("E:\\client.txt", 30);
		bw.write("1111111111111111");
		bw.write("2222");
		bw.write("333333333333333");
		bw.write("4444444444444444");
		bw.flushAndClose();
	}

}