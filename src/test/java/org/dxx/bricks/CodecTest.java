/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** 
 * 
 * @author dixingxing
 * @2014年9月20日 下午4:15:06  
 */
public class CodecTest {

	@Test
	public void test() {
		Codec codec = new Codec();
		
		Brick b1 = new Brick();
		b1.setLineNum(1);
		b1.setContent("dddddddddddd");
		
		byte[] bytes = codec.encode(b1);
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		List<Brick> bricks = new ArrayList<Brick>();
		
		codec.decode(bb, bricks);
		assertTrue(bricks.size() > 0);
		assertTrue(bricks.get(0).getLineNum() == 1);
		assertEquals("dddddddddddd", bricks.get(0).getContent());
	}

}
