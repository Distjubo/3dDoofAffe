package de.distjubo.doofaffe;

import static de.distjubo.doofaffe.Common.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class provides methods for writing to DoofAffe-archives. Do note that
 * this class is not thread-safe.
 *
 * @author Christian Romberg
 */
public class DoofAffeOutput implements Closeable
{
	/**
	 * The underlying {@link OutputStream} enties will be written to
	 */
	protected OutputStream os;
	/**
	 * whether an error has occured
	 */
	protected boolean error;
	/**
	 * whether the headers have been read
	 */
	protected boolean initialized;
	/**
	 * A buffer used for reading five bytes at once
	 */
	protected final byte[] fourByteBuffer = new byte[4];
	private boolean closed;

	/**
	 * Creates a new {@link DoofAffeInput} with the given {@link OutputStream}
	 *
	 * @param os
	 *            the {@link OutputStream} entries will be written to
	 */
	public DoofAffeOutput(OutputStream os)
	{
		this.os = os;
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
		this.os.close();
	}

	/**
	 * Writes a {@link List} of {@link Entry Entries} to the underlying
	 * {@link OutputStream}. The underlying {@link OutputStream} and this
	 * {@link DoofAffeOutput} are closed when the writing has finished.
	 *
	 * @param entries
	 *            The {@link Entry Entries} that will be written
	 * @throws IOException
	 *             if an {@link IOException} occurs while writing
	 */
	public void writeEntries(List<Entry<String, byte[]>> entries) throws IOException
	{
		this.writeEntries(entries, true);
	}

	/**
	 * Writes a {@link List} of {@link Entry Entries} to the underlying
	 * {@link OutputStream}.
	 *
	 * @param entries
	 *            The {@link Entry Entries} that will be written
	 * @param close
	 *            whether to close the underlying {@link OutputStream} and this
	 *            {@link DoofAffeOutput} after the writing has finished
	 * @throws IOException
	 *             if an {@link IOException} occurs while writing
	 */
	public void writeEntries(List<Entry<String, byte[]>> entries, boolean close) throws IOException
	{
		try
		{
			for (int i = 0; i < entries.size(); i++)
			{
				Entry<String, byte[]> entry = entries.get(i);
				this.writeEntry(entry.getKey(), entry.getValue());
			}
		}
		finally
		{
			if (close)
			{
				this.close();
			}
		}
	}

	/**
	 * Writes a single entry to the underlying {@link OutputStream}.
	 *
	 * @param descriptor
	 *            the descriptor of the entry (e.g. MIME-type)
	 * @param content
	 *            the content of the entry that will be written
	 * @throws IOException
	 *             if an {@link IOException} occurs while writing
	 */
	public void writeEntry(String descriptor, byte[] content) throws IOException
	{
		if (!this.initialized)
		{
			this.writeHead();
		}
		this.checkWrite();
		byte[] mimeBytes = descriptor.getBytes(CHARSET);
		this.os.write(intToByteArray(mimeBytes.length, this.fourByteBuffer));
		this.os.write(mimeBytes);
		this.os.write(intToByteArray(getChecksum(content), this.fourByteBuffer));
		this.os.write(intToByteArray(content.length, this.fourByteBuffer));
		this.os.write(content);
	}

	/**
	 * Writes the DoofAffe Magic Bytes (0x3dD00fAffe) and the file version byte
	 * to the underlying {@link OutputStream}.
	 *
	 * @throws IOException
	 *             if an {@link IOException} occurs while writing
	 */
	public void writeHead() throws IOException
	{
		if (this.initialized)
		{
			return;
		}
		this.checkWrite();
		try
		{
			this.os.write(MAGIC_BYTES);
			this.os.write(VERSION_BYTE);
			this.initialized = true;
		}
		catch (IOException e)
		{
			this.error = true;
			throw e;
		}
	}
}