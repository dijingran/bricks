/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

import java.io.File;
import java.io.IOException;

import org.dxx.bricks.client.BrickWriter;
import org.dxx.bricks.server.BrickReader;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author dixingxing
 * @2014年9月20日 下午4:41:35
 */
public class ReadWriteTest {
	static int M = 1024 * 1024;
	
	@Test
	public void test() throws IOException, InterruptedException {
		long s = System.currentTimeMillis();
		BrickReader reader = new BrickReader("e:\\allBricks.txt", 1024);
//		BrickReader reader = new BrickReader("e:\\temp.txt", M);
		BrickWriter writer = new BrickWriter("e:\\testWriter.txt", 30 * M);
		
		Brick brick = null;
		while ((brick = reader.getNext()) != null) {
			writer.write(brick + "\r\n");
		}
		writer.flushAndClose();
		
		System.out.println("cost : " + (System.currentTimeMillis() - s) + " ms");
	}
	
	@Before
	public void setup() {
		new File("e:\\testWriter.txt").delete();
	}

}
