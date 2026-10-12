package net.j7.commons.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.apache.commons.io.input.XmlStreamReader;

public class XmlParser {


	private static final IOException END_OF_STREAM = new IOException("Reached end of a stream");

	private final static int BUF_SIZE = 512;

	private char[] buf = new char[BUF_SIZE];

	int left = 0, pos ;

	private Stack<Character> stack = new Stack<Character>();

	private XmlStreamReader is;
	public XmlParser(InputStream is) throws IOException {
		this.is = new XmlStreamReader(is);
	}

	private char next() throws IOException {
		if (left > 0) {
			left--;
			return buf[pos++];
		}
		left = is.read(buf);
		pos = 0;
		if (left <= 0) throw END_OF_STREAM;
		return next();
	}


	public void parse() {
		try {
			char ch;
			while (true) {
				ch = next();

				switch (ch) {
				case '<':

					break;
				case '>':

					break;

				case '/':
					break;

				default:
					break;
				}

			}


		} catch (IOException e) {
			System.out.println("End of a stream");
		}
	}

	public static void main(String[] args) throws IOException {
		InputStream is = new FileInputStream(new File("I:\\fb2\\examples\\litru\\09449332.fb2"));
		XmlParser p = new XmlParser(is);
		p.parse();
	}


}
