package info.tmnsur.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

public class BufferedStreamTest {
	@Test
	public void shouldRead() throws IOException {
		try(PipedInputStream pis = new PipedInputStream(65536);
			PipedOutputStream pos = new PipedOutputStream(pis);
			BufferedStream stream = new BufferedStream(pis)) {

			pos.write('A');

			assertEquals("A", stream.read(1));
		}

		try(PipedInputStream pis = new PipedInputStream(65536);
			PipedOutputStream pos = new PipedOutputStream(pis);
			BufferedStream stream = new BufferedStream(pis)) {

			pos.write("someLine\nASD\n".getBytes());

			assertEquals("someLine", stream.readLine());
			assertEquals("ASD", stream.read(3));
		}

		try(PipedInputStream pis = new PipedInputStream(65536);
			PipedOutputStream pos = new PipedOutputStream(pis);
			BufferedStream stream = new BufferedStream(pis)) {

			pos.write("someLine\nASD\n".getBytes());

			assertEquals("someLine", stream.readLine());
			assertEquals("ASD\n", stream.read(4));
		}

		try(PipedInputStream pis = new PipedInputStream(65536);
			PipedOutputStream pos = new PipedOutputStream(pis);
			BufferedStream stream = new BufferedStream(pis)) {

			pos.write("someLine\nASDF\n".getBytes());

			assertEquals("someLine", stream.readLine());
			assertEquals("ASD", stream.read(3));
			assertEquals("F", stream.readLine());
		}

		try(PipedInputStream pis = new PipedInputStream(65536);
			PipedOutputStream pos = new PipedOutputStream(pis);
			BufferedStream stream = new BufferedStream(pis)) {

			pos.write("someLine\nASDF\nGHJK\n".getBytes());

			assertEquals("someLine", stream.readLine());
			assertEquals("ASDF\nG", stream.read(6));
			assertEquals("HJK", stream.readLine());
		}
	}

	@Test(expected = IOException.class)
	public void shouldNotReadEmptyStream() throws IOException {
		try(InputStream inputStream = mock(InputStream.class);
			BufferedStream stream = new BufferedStream(inputStream)) {

			when(inputStream.read(any())).thenReturn(-1);

			stream.read(1);
		}
	}
}
