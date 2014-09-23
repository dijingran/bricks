/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

import static org.junit.Assert.*;

import org.junit.Test;

/** 
 * 
 * @author dixingxing
 * @2014年9月22日 下午8:18:45  
 */
public class ExtractorTest {

	@Test
	public void test() {
		assertEquals("1345", Extractor.extract("12345"));
		assertEquals("1256", Extractor.extract("123456"));
		assertEquals("123789", Extractor.extract("123456789"));
		assertEquals("1237890", Extractor.extract("1234567890"));
	}

}
