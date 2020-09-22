/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

/**
 * @author fernandoglatz
 */
public class MultipartBuilder {

	private static final String LINE_FEED = "\r\n";

	private final String boundary;
	private final ByteArrayOutputStream outputStream;
	private final PrintWriter writer;

	public MultipartBuilder() {
		boundary = "===" + System.currentTimeMillis() + "===";
		outputStream = new ByteArrayOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
	}

	public String getBoundary() {
		return boundary;
	}

	public ByteArrayOutputStream getOutputStream() {
		return outputStream;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public MultipartBuilder addFile(String name, String fileName, String mediaType, InputStream inputStream) throws IOException {
		if (inputStream != null) {
			getWriter().append("--" + boundary).append(LINE_FEED);
			getWriter().append("Content-Disposition: form-data; name=\"").append(name + "\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
			getWriter().append("Content-Type: ").append(mediaType).append(LINE_FEED);
			getWriter().append("Content-Transfer-Encoding: binary").append(LINE_FEED).append(LINE_FEED);
			getWriter().flush();

			IOUtils.copy(inputStream, getOutputStream());

			getWriter().append(LINE_FEED);
			getWriter().flush();
		}

		return this;
	}

	public MultipartBuilder addText(String name, String text) {
		if (text != null) {
			getWriter().append("--" + boundary).append(LINE_FEED);
			getWriter().append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
			getWriter().append("Content-Type: text/plain; charset=").append(StandardCharsets.UTF_8.name()).append(LINE_FEED).append(LINE_FEED);

			getWriter().append(text);

			getWriter().append(LINE_FEED);
			getWriter().flush();
		}

		return this;
	}

	public InputStream build() throws IOException {
		getWriter().append(LINE_FEED).append("--" + getBoundary() + "--").append(LINE_FEED);
		getWriter().flush();

		IOUtils.close(getWriter());
		byte[] byteArray = getOutputStream().toByteArray();
		IOUtils.close(getOutputStream());

		return new ByteArrayInputStream(byteArray);
	}
}
