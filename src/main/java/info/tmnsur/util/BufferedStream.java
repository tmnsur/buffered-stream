package info.tmnsur.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class BufferedStream implements AutoCloseable {
	private static final String READ_IS_MINUS_ONE = "read is -1";

	private InputStream inputStream;
	private LinkedList<String> lines;
	private String lastChunk;

	public BufferedStream(InputStream inputStream) {
		this.inputStream = inputStream;
		this.lines = new LinkedList<>();
	}

	private void readBuffer(byte[] buffer, int limit) throws IOException {
		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(16384)) {
			bos.write(buffer, 0, limit);

			while(0 < inputStream.available()) {
				int read = inputStream.read(buffer);

				if(-1 == read) {
					throw new IOException(READ_IS_MINUS_ONE);
				}

				bos.write(buffer, 0, read);
			}

			String str = bos.toString();

			if(null != lastChunk) {
				str = lastChunk + str;
				lastChunk = null;
			}

			int index = str.indexOf('\n');
			while(-1 != index) {
				lines.add(str.substring(0, index));

				str = str.substring(index + 1);
				index = str.indexOf('\n');
			}

			if(!str.isEmpty()) {
				lastChunk = str;
			}
		}
	}

	private String readBufferAndLine() throws IOException {
		byte[] buffer = new byte[16384];

		int read = inputStream.read(buffer);

		if(-1 == read) {
			throw new IOException(READ_IS_MINUS_ONE);
		}

		lines = new LinkedList<>();

		while(lines.isEmpty()) {
			readBuffer(buffer, read);
		}

		return lines.pop();
	}

	private String consumeLines(StringBuilder builder, String topElement, int left) {
		if(left == topElement.length()) {
			lines.push("");

			builder.append(topElement);

			return builder.toString();
		}

		lines.push(topElement.substring(left));

		builder.append(topElement.substring(0, left));

		return builder.toString();

	}

	private String consumeLastChunk(int left) {
		if(left == lastChunk.length()) {
			String temp = lastChunk;

			lastChunk = null;

			return temp;
		}

		String temp = lastChunk.substring(0, left);

		lastChunk = lastChunk.substring(left + 1);

		return temp;
	}

	private String consumeStream(int left) throws IOException {
		int localLeft = left;

		byte[] buffer = new byte[localLeft];

		int read = inputStream.read(buffer);

		if(-1 == read) {
			throw new IOException(READ_IS_MINUS_ONE);
		}

		if(read == localLeft) {
			return new String(buffer);
		}

		try(ByteArrayOutputStream bos = new ByteArrayOutputStream(16384)) {
			if(read > localLeft) {
				String temp = bos.toString();
				String result = temp.substring(0, localLeft);

				lastChunk = temp.substring(localLeft + 1);

				return result;
			}

			do {
				bos.write(buffer, 0, read);

				if(read > localLeft) {
					String temp = bos.toString();
					String result = temp.substring(0, localLeft);

					lastChunk = temp.substring(localLeft + 1);

					return result;
				}

				localLeft -= read;

				read = inputStream.read(buffer);

				if(-1 == read) {
					throw new IOException(READ_IS_MINUS_ONE);
				}
			} while(0 < localLeft);

			return bos.toString();
		}
	}

	public String readLine() throws IOException {
		if(lines.isEmpty()) {
			return readBufferAndLine();
		}

		return lines.pop();
	}

	public String read(int amount) throws IOException {
		int left = amount;

		StringBuilder builder = new StringBuilder();

		while(null != lines && !lines.isEmpty()) {
			String topElement = lines.pop();

			if(left > topElement.length()) {
				builder.append(topElement);
				builder.append('\n');

				left -= topElement.length() + 1;

				if(0 == left) {
					return builder.toString();
				}
			} else {
				return consumeLines(builder, topElement, left);
			}
		}

		if(null != lastChunk) {
			if(left > lastChunk.length()) {
				builder.append(lastChunk);

				left -= lastChunk.length();

				lastChunk = null;
			} else {
				return consumeLastChunk(left);
			}
		}

		return consumeStream(left);
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}
}
