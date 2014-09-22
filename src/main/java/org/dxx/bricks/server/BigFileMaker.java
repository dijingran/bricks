/*
 * PROPRIETARY and CONFIDENTIAL
 * gina
 * @version V1.0
 * SINA Corporation, All Rights Reserved
 */
package org.dxx.bricks.server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Random;

/**
 * 题目叫码农来搬砖，题目的要求为：
 * <li>实现一个客户端和一个服务器端，客户端把服务器端的一个1G的文件搬到客户端，文件中的每行字符串为一块砖头，字符串由随机的ascii32
 * -127的字符组成，每行的长度为随机的1-200字节； 
 * <li>服务端必须是单进程并且只能监听一个端口；
 * <li>客户端每个发起获取砖头的请求“线程“必须是一问一答，类似这样： 砖头 result = client.getZhuanTou();
 * 服务端收到请求后，必须是按顺序获取下一块砖头，类似这样： 砖头 next = server.getNext();
 * <li>不允许批量处理请求，也不允许服务端处理请求的”线程“批量返回砖头，服务端处理请求的”线程“每次只能处理一个请求，并返回一块砖头；
 * <li>服务端或客户端需要对砖头进行处理
 * ，处理方式为去掉行中间的三分之一字符（从size/3字符开始去掉size/3个字符，除法向下取整)后将剩余部分以倒序的方式传输 例如 123456789
 * => 123789 => 987321 每块砖头需要标上序号，例如上面的123456789是第5行，那么最后的砖头结果应该为：5987321；
 * <li>
 * 客户端需要顺序的输出最终处理过的砖头内容到一个文件中
 * ，此文件中的砖头的顺序要和服务端的原始文件完全一致，文件不需要写透磁盘（例如java里就是可以不强制调sync）； 
 * <li>不允许采用内核层面的patch；
 * <li>不建议采用通信框架，例如netty之类的这种； 不限语言、通信协议和连接数。
 * <li>比赛的运行方式： 服务端启动5s后启动客户端，客户端启动就开始计时，一直到客户端搬完所有砖头并退出计算为耗时时间；
 * 
 * @author dixingxing
 * @2014年9月20日 上午10:01:31
 */
public class BigFileMaker {
	private static final int M = 1024 * 1024;
	
	// data chunk be written per time
    private static final int DATA_CHUNK = 128 * M; 
 
    // total data size is 1G
    private static final long LEN = 1L * M * 1024L; 
    
    
	 /**
     * write big file with MappedByteBuffer
     * @throws IOException
     */
    public static void writeWithMappedByteBuffer() throws IOException {
        File file = new File("e:/allBricks.txt");
        if (file.exists()) {
            file.delete();
        }
 
        Random rnd = new Random();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = raf.getChannel();
        int pos = 0;
        MappedByteBuffer mbb = null;
        byte[] data = null;
        long len = LEN;
        int dataChunk = DATA_CHUNK / (M);
        while (len >= DATA_CHUNK) {
            System.out.println("write a data chunk: " + dataChunk + "MB");
 
            mbb = fileChannel.map(MapMode.READ_WRITE, pos, DATA_CHUNK);
            data = new byte[DATA_CHUNK];
            
            data = randomChars(rnd, (int)DATA_CHUNK);
            
            mbb.put(data);
            data = null;
 
            len -= DATA_CHUNK;
            pos += DATA_CHUNK;
        }
 
        if (len > 0) {
            System.out.println("write rest data chunk: " + len + "B");
 
            mbb = fileChannel.map(MapMode.READ_WRITE, pos, len);
            data = randomChars(rnd, (int)len >> 1);
            mbb.put(data);
        }
 
        data = null;
        unmap(mbb);   // release MappedByteBuffer
        fileChannel.close();
        raf.close();
    }

	/** 
	* 
	*/ 
	private static byte[] randomChars(Random rnd, int len) {
		char[] chars = new char[len];
		int maxLineLength = new Random().nextInt(200);
		int lineLength = 0;
		for(int i = 0 ; i < chars.length; i++) {
			if(lineLength++ >= maxLineLength) {
				chars[i] = (char) 13; // 换行
				lineLength = 0;
				maxLineLength = new Random().nextInt(200);
			} else {
				chars[i] = (char) (32 + rnd.nextInt(95));
			}
		}
		return new String(chars).getBytes();
	}
    
    /**
     * 在MappedByteBuffer释放后再对它进行读操作的话就会引发jvm crash，在并发情况下很容易发生
     * 正在释放时另一个线程正开始读取，于是crash就发生了。所以为了系统稳定性释放前一般需要检
     * 查是否还有线程在读或写
     * @param mappedByteBuffer
     */
    public static void unmap(final MappedByteBuffer mappedByteBuffer) {
        try {
            if (mappedByteBuffer == null) {
                return;
            }
             
            mappedByteBuffer.force();
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                @SuppressWarnings("restriction")
                public Object run() {
                    try {
                        Method getCleanerMethod = mappedByteBuffer.getClass()
                                .getMethod("cleaner", new Class[0]);
                        getCleanerMethod.setAccessible(true);
                        sun.misc.Cleaner cleaner = 
                                (sun.misc.Cleaner) getCleanerMethod
                                    .invoke(mappedByteBuffer, new Object[0]);
                        cleaner.clean();
                         
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException {
    	BigFileMaker.writeWithMappedByteBuffer();
	}
}
