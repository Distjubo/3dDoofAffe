/**
 *
 */
package de.distjubo.doofaffe;

import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Christian Romberg
 *
 */
class Common
{
	// This is why its called "DoofAffe"
	public static final byte[] MAGIC_BYTES = new byte[] { (byte) 0x3d, (byte) 0xD0, (byte) 0x0f, (byte) 0xAf, (byte) 0xfe };
	public static final byte[] VERSION_BYTE = new byte[] { (byte) 0 };
	public static final Charset CHARSET = Charset.forName("UTF-8");

	public static int byteArrayToInt(byte[] buf)
	{
		assert buf.length == 4;
		int b0 = (buf[0] & 0xFF) << 24;
		int b1 = (buf[1] & 0xFF) << 16;
		int b2 = (buf[2] & 0xFF) << 8;
		int b3 = (buf[3] & 0xFF) << 0;
		return b0 | b1 | b2 | b3;
	}

	public static int getChecksum(byte[] bytes)
	{
		Checksum checksum = new CRC32();
		checksum.update(bytes, 0, bytes.length);
		long val = checksum.getValue();
		return (int) val;
	}

	public static byte[] intToByteArray(int val, byte[] buf)
	{
		assert buf.length == 4;
		buf[3] = (byte) (val >> 0 & 0xFF);
		buf[2] = (byte) (val >> 8 & 0xFF);
		buf[1] = (byte) (val >> 16 & 0xFF);
		buf[0] = (byte) (val >> 24 & 0xFF);
		return buf;
	}
}