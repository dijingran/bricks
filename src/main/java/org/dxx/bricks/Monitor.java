/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks;

import org.dxx.bricks.server.BrickReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author dixingxing
 * @2014年9月22日 下午5:09:44
 */
public class Monitor implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
	private int tmp = 0;
	private BrickReader brickReader;

	public Monitor(BrickReader brickReader) {
		super();
		this.brickReader = brickReader;
	}

	@Override
	public void run() {
		try {
			while (true) {
				int transferred = brickReader.getLineNum().get();
				Thread.sleep(3000L);
				logger.debug("{} bricks transferred. QPS : {}", transferred,
						(transferred - tmp) / 3);
				tmp = transferred;
			}
		} catch (InterruptedException e) {
			logger.debug("Stop monitoring. {} bricks transferred.", brickReader.getLineNum().get());
		}
	}
}
