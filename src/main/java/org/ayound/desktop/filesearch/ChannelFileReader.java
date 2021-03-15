package org.ayound.desktop.filesearch;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChannelFileReader {

	public static final Logger logger = LogManager.getLogger(ChannelFileReader.class);

	private FileInputStream fileIn;
	private ByteBuffer byteBuf;
	private long fileLength;
	private int arraySize;
	private byte[] array;

	public ChannelFileReader(String fileName, int arraySize) throws IOException {
		this.fileIn = new FileInputStream(fileName);
		this.fileLength = fileIn.getChannel().size();
		this.arraySize = arraySize;
		this.byteBuf = ByteBuffer.allocate(this.arraySize);
	}

	public int read() throws IOException {
		FileChannel fileChannel = fileIn.getChannel();
		int bytes = fileChannel.read(byteBuf);// 读取到ByteBuffer中
		if (bytes != -1) {
			array = new byte[bytes];// 字节数组长度为已读取长度
			byteBuf.flip();
			byteBuf.get(array);// 从ByteBuffer中得到字节数组
			byteBuf.clear();
			return bytes;
		}
		return -1;
	}

	public void close() throws IOException {
		fileIn.close();
		array = null;
	}

	public byte[] getArray() {
		return array;
	}

	public long getFileLength() {
		return fileLength;
	}

}