package com.orchestranetworks.ps.util;

import java.io.*;
import java.util.*;

/**
 * This is a simple file reader that knows how to handle CSV files.
 */
public class CSVFileReader extends InputStreamReader
{
	public static final char DEFAULT_DELIMITER = ',';
	private static final int IN_STRING = 0;
	private static final int READ = 1;
	private static final int COMPLETE = 2;

	private final char delimiter;

	public CSVFileReader(InputStream istream, String charSet, char delimiter)
		throws UnsupportedEncodingException
	{
		super(istream, charSet);
		this.delimiter = delimiter;
	}

	public CSVFileReader(InputStream istream, char delimiter)
	{
		super(istream);
		this.delimiter = delimiter;
	}

	public CSVFileReader(InputStream istream)
	{
		this(istream, DEFAULT_DELIMITER);
	}

	public CSVFileReader(File file, char delimiter) throws FileNotFoundException
	{
		this(new FileInputStream(file), delimiter);
	}

	public CSVFileReader(File file, String charSet, char delimiter)
		throws FileNotFoundException, UnsupportedEncodingException
	{
		this(new FileInputStream(file), charSet, delimiter);
	}

	public CSVFileReader(File file) throws FileNotFoundException
	{
		this(file, DEFAULT_DELIMITER);
	}

	public char getDelimiter()
	{
		return delimiter;
	}

	/**
	 * Reads the next row from a CSV file, returning an array of strings or null
	 * if EOF.
	 * 
	 * @throws IOException
	 */
	public String[] readRow() throws IOException
	{
		int i = read();
		if (i == -1)
		{
			return null;
		}
		List<String> items = new ArrayList<>();
		char delim = getDelimiter();
		char c = (char) i;
		boolean[] state = new boolean[3];
		StringBuilder buffer = new StringBuilder();
		state[READ] = true;
		while (!state[COMPLETE] && i != -1)
		{
			if (state[IN_STRING] && c != '"')
			{
				buffer.append(c);
			}
			else
			{
				readChar(buffer, items, c, delim, state);
			}
			if (state[READ])
			{
				i = read();
				if (i != -1)
				{
					c = (char) i;
				}
			}
			state[READ] = true;
		}
		return items.toArray(new String[items.size()]);
	}

	private void readChar(
		StringBuilder buffer,
		List<String> items,
		char c,
		char delim,
		boolean[] state)
		throws IOException
	{
		switch (c)
		{
		case '"':
			handleQuote(buffer, items, delim, state);
			break;
		case '\r':
			break;
		case '\n':
			items.add(buffer.toString());
			state[COMPLETE] = true;
			state[READ] = false;
			break;
		default:
			if (!state[IN_STRING] && c == delim)
			{
				items.add(buffer.toString());
				buffer.setLength(0);
			}
			else
			{
				buffer.append(c);
			}
		}
	}

	private void handleQuote(StringBuilder buffer, List<String> items, char delim, boolean[] state)
		throws IOException
	{
		if (state[IN_STRING])
		{
			int i = read();
			if (i == -1)
			{
				throw new IOException("Unexpectedly reached end of file");
			}
			char c = (char) i;
			if (c == '"')
			{
				buffer.append(c);
			}
			else
			{
				state[IN_STRING] = false;
				readChar(buffer, items, c, delim, state);
			}
		}
		else
		{
			state[IN_STRING] = true;
		}
	}
}
