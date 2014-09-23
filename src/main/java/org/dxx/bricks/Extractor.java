/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

/**
 * 
 * @author dixingxing
 * @2014年9月22日 下午8:16:35
 */
public class Extractor {
	/**
	 * 删除中间3分之一数据
	 */
	public static String extract(String org) {
		int i = org.length() / 3;
		return org.substring(0, i) + org.substring(2 * i, org.length());
	}
}
