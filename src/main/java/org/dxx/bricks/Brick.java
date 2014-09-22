/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

import java.io.Serializable;

/**
 * 
 * @author dixingxing
 * @2014年9月20日 下午3:55:11
 */
@SuppressWarnings("serial")
public class Brick implements Serializable {
	private int lineNum;

	private String content;

	public Brick() {
		super();
	}

	public Brick(int lineNum, String content) {
		super();
		this.lineNum = lineNum;
		this.content = content;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Brick [lineNum=" + lineNum + ", content=" + content + "]";
	}
}
