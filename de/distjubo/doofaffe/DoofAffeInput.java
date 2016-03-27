package de.distjubo.doofaffe;

import static de.distjubo.doofaffe.Common.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class provides methods for reading from DoofAffe-archives. Do note that
 * this class is not thread-safe.
 *
 * @author Christian Romberg
 */
public class DoofAffeInput implements Closeable
{
	/**
	 * The underlying {@link InputStream} that is being read from
	 */
	protected InputStream is;
	/**
	 * A buffer used for reading a single byte
	 */
	protected final byte[] oneByteBuffer = new byte[1];
	/**
	 * A buffer used for reading four bytes at once
	 */
	protected final byte[] fourByteBuffer = new byte[4];
	/**
	 * A buffer used for reading five bytes at once
	 */
	protected final byte[] fiveByteBuffer = new byte[5];
	/**
	 * whether an error has occured
	 */
	protected boolean error;
	/**
	 * whether the headers have been read
	 */
	protected boolean initialized;
	private boolean closed;

	/**
	 * Creates a new {@link DoofAffeInput} with the given {@link InputStream}
	 *
	 * @param is
	 *            the input stream to read from
	 */
	public DoofAffeInput(InputStream is)
	{
		this.is = is;
	}

	private void checkWrite() throws IOException
	{
		if (this.error)
		{
			throw new RuntimeException("An error occured");
		}
		if (this.closed)
		{
			throw new IOException("Already closed!");
		}
	}

	@Override
	public void close() throws IOException
	{
		this.closed = true;
		this.is.close();
	}

	/**
	 * Reads a single entry from the DoofAffe archive.
	 *
	 * @return the read entry
	 * @throws IOException
	 *             if an {@link IOException} occurs during reading, or EOF has
	 *             been reached before the entry has been read
	 */
	public Entry<String, byte[]> readEntry() throws IOException
	{
		this.checkWrite();
		if (!this.initialized)
		{
			this.readHead();
		}
		int descriptorLength = this.readInt(this.is);
		byte[] descriptorBytes = new byte[descriptorLength];
		if (this.is.read(descriptorBytes) != descriptorLength)
		{
			this.error = true;
			throw new IOException("Reached EOF");
		}
		String descriptorString = new String(descriptorBytes, CHARSET);
		int checksum = this.readInt(this.is);
		int contentLength = this.readInt(this.is);
		byte[] contentBytes = new byte[contentLength];
		if (this.is.read(contentBytes) != contentLength)
		{
			this.error = true;
			throw new IOException("Reached EOF");
		}
		if (getChecksum(contentBytes) != checksum)
		{
			this.error = true;
			throw new IOException("Checksum mismatch");
		}
		return new AbstractMap.SimpleEntry<String, byte[]>(descriptorString, contentBytes);
	}

	/**
	 * Reads the file header of the DoofAffe archive
	 *
	 * @throws IOException
	 *             if an {@link IOException} occurs while reading, or the Magic
	 *             number or the version does not match
	 */
	public void readHead() throws IOException
	{
		if (this.initialized)
		{
			return;
		}
		this.checkWrite();
		this.is.read(this.fiveByteBuffer);
		if (!Arrays.equals(this.fiveByteBuffer, MAGIC_BYTES))
		{
			this.error = true;
			throw new IOException("Not a DoofAffe file!");
		}
		this.is.read(this.oneByteBuffer);
		if (!Arrays.equals(this.oneByteBuffer, VERSION_BYTE))
		{
			this.error = true;
			throw new IOException("Unsupported DoofAffe-version");
		}
		this.initialized = true;
	}

	private int readInt(InputStream is) throws IOException
	{
		if (is.read(this.fourByteBuffer) != 4)
		{
			this.error = true;
			throw new IOException("Reached EOF");
		}
		return byteArrayToInt(this.fourByteBuffer);
	}

	/**
	 * Reads all remaining entries, until EOF is reached. This method closes the
	 * {@link InputStream} and the {@link DoofAffeInput} when done.
	 *
	 * @return The read entries.
	 * @throws IOException
	 *             if an {@link IOException} occurs while reading, or EOF has
	 *             been reached while reading an entry
	 */
	public List<Entry<String, byte[]>> readRemainingEntries() throws IOException
	{
		return this.readRemainingEntries(true);
	}

	/**
	 * Reads all remaining entries, until EOF is reached. If close is true, the
	 * {@link DoofAffeInput} and the underlying {@link InputStream} are closed.
	 *
	 * @param close
	 *            whether to close the {@link DoofAffeInput}
	 * @return the {@link List} of {@link Entry Entries} that have been read
	 * @throws IOException
	 *             if an {@link IOException} occurs while reading, or EOF has
	 *             been reached while reading an entry
	 */
	public List<Entry<String, byte[]>> readRemainingEntries(boolean close) throws IOException
	{
		List<Entry<String, byte[]>> result = new ArrayList<>();
		try
		{
			while (this.is.available() != 0)
			{
				result.add(this.readEntry());
			}
		}
		finally
		{
			if (close)
			{
				this.is.close();
			}
		}
		return result;
	}
}