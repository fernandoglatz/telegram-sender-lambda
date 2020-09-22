/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;

/**
 * @author fernandoglatz
 */
public class EmailUtils {

	private static final String TEXT_PLAIN = "text/plain";
	private static final String TEXT_HTML = "text/html";
	private static final String LINE_FEED = "\r\n";

	public static String getContent(Message message) throws MessagingException, IOException {
		Set<String> stringParts = new LinkedHashSet<>();

		getContentFromPart(stringParts, message);
		StringBuilder sb = new StringBuilder();

		for (String stringPart : stringParts) {
			if (sb.length() == 0) {
				sb.append(LINE_FEED);
			}

			sb.append(stringPart);
		}

		return sb.toString();
	}

	private static void getContentFromPart(Set<String> stringParts, Part part) throws MessagingException, IOException {
		Object content = part.getContent();

		if (part.isMimeType(TEXT_PLAIN)) {
			stringParts.add(String.valueOf(content).concat(LINE_FEED));

		} else if (part.isMimeType(TEXT_HTML)) {
			getContentFromHtmlPart(stringParts, content);

		} else if (content instanceof Multipart) {
			getContentFromMultipart(stringParts, (Multipart) content);
		}
	}

	private static void getContentFromHtmlPart(Set<String> stringParts, Object content) {
		Document jsoupDoc = Jsoup.parse(String.valueOf(content));

		OutputSettings outputSettings = new OutputSettings();
		outputSettings.prettyPrint(false);
		jsoupDoc.outputSettings(outputSettings);
		jsoupDoc.select("br").before("\\r\\n");
		jsoupDoc.select("p").before("\\r\\n");

		String newStr = jsoupDoc.html().replaceAll("\\\\r\\\\n", LINE_FEED);
		String prettyContent = Jsoup.clean(newStr, StringUtils.EMPTY, Whitelist.none(), outputSettings);
		stringParts.add(String.valueOf(prettyContent));
	}

	private static void getContentFromMultipart(Set<String> stringParts, Multipart multipart) throws MessagingException, IOException {
		int count = multipart.getCount();

		for (int i = 0; i < count; i++) {
			BodyPart part = multipart.getBodyPart(i);
			getContentFromPart(stringParts, part);
		}
	}

	public static Map<String, InputStream> getAttachments(Message message) throws MessagingException, IOException {
		Map<String, InputStream> attachments = new LinkedHashMap<>();

		getAttachmentsFromPart(attachments, message);

		return attachments;
	}

	private static void getAttachmentsFromMultipart(Map<String, InputStream> attachments, Multipart multipart) throws MessagingException, IOException {
		int count = multipart.getCount();

		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = multipart.getBodyPart(i);
			getAttachmentsFromPart(attachments, bodyPart);
		}
	}

	private static void getAttachmentsFromPart(Map<String, InputStream> attachments, Part part) throws IOException, MessagingException {
		Object content = part.getContent();

		String fileName = part.getFileName();
		if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && StringUtils.isNotEmpty(fileName)) {

			try (InputStream inputStream = part.getInputStream(); //
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
				IOUtils.copy(inputStream, outputStream);
				byte[] bytes = outputStream.toByteArray();
				InputStream newInputStream = new ByteArrayInputStream(bytes);

				attachments.put(fileName, newInputStream);
			}

		} else if (content instanceof Multipart) {
			getAttachmentsFromMultipart(attachments, (Multipart) content);
		}
	}
}
