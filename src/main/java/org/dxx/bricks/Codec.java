/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Not thread safe. Do not share this instance.
 * 
 * | brick行号  | brick长度   | brick 内容   <br>
 * --------------------------------  <br>
 * |  4 bytes | 2 bytes | n bytes
 * 
 * @author dixingxing
 * @2014年9月20日 下午3:41:54
 */
public class Codec {
	private static final int HEADER_LENGTH = 6;

	private State state = State.LINE_NUM;
	
	private int lineNum;
	private short brickLength;
	
	/** hold uncompleted package */
	private ByteBuffer half;
	
	enum State {
		LINE_NUM, BRICK_LENGTH, BRICK
	}
	
	static byte[] intToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) (i & 0xFF);
		return result;
	}
	
	static byte[] shortToBytes(short s) {
        byte[] result = new byte[2];
        result[0] = (byte) ((s >> 8) & 0xFF);
		result[1] = (byte) (s & 0xFF);
        return result;  
    }  

	public byte[] encode(Brick brick) {
		byte[] bytes = brick.getContent().getBytes();
		byte[] data = new byte[HEADER_LENGTH + bytes.length];
		System.arraycopy(intToBytes(brick.getLineNum()), 0, data, 0, 4);
		System.arraycopy(shortToBytes((short)bytes.length), 0, data, 4, 2);
		System.arraycopy(bytes, 0, data, HEADER_LENGTH, bytes.length);
		return data;
	}

	public void decode(ByteBuffer bb, List<Brick> bricks) {
		if(half != null && half.hasRemaining()) {
			byte[] afterPadding = new byte[half.remaining() + bb.remaining()];
			
			byte[] rest = new byte[half.remaining()];
			half.get(rest, 0, half.remaining());
			
			System.arraycopy(rest, 0, afterPadding, 0, rest.length);
			System.arraycopy(bb.array(), 0, afterPadding, rest.length, bb.remaining());
			bb = ByteBuffer.wrap(afterPadding);
			half = null;
		}
		
		while (bb.hasRemaining()) {
			if(state == State.LINE_NUM) {
				if(bb.remaining() >= 4) {
					lineNum = bb.getInt();
					state = State.BRICK_LENGTH;
				} else {
					half = bb;
					break;
				}
			}
			
			if(state == State.BRICK_LENGTH) {
				if(bb.remaining() >= 2) {
					brickLength = bb.getShort();
					state = State.BRICK;
				} else {
					half = bb;
					break;
				}
			}
			if(state == State.BRICK) {
				if(bb.remaining() >= brickLength) {
					byte[] bytes = new byte[brickLength];
					bb.get(bytes, 0, bytes.length);
					
					Brick b = new Brick();
					b.setLineNum(lineNum);
					b.setContent(new String(bytes));
					bricks.add(b);
					state = State.LINE_NUM;
				} else {
					half = bb;
					break;
				}
			}
		}
	}
}
