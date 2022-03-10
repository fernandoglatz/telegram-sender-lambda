/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoglatz.telegramsender.builder.MultipartBuilder;
import com.fernandoglatz.telegramsender.dto.ITextDTO;
import com.fernandoglatz.telegramsender.dto.SendMessageDTO;
import com.fernandoglatz.telegramsender.dto.SendPhotoDTO;
import com.fernandoglatz.telegramsender.dto.TelegramResponseDTO;

/**
 * @author fernandoglatz
 */
public class TelegramApi {

	private static final String POST = "POST";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String MULTIPART_FORM_DATA = "multipart/form-data";
	private static final String APPLICATION_JSON = "application/json";

	private static final Integer CONNECTION_TIMEOUT = 600000; //10 minutes
	private static final Integer MESSAGE_LENGTH_LIMIT = 4096;
	private static final Integer PHOTO_MESSAGE_LENGTH_LIMIT = 200;

	private static final Integer START_HTTP_2XX = 200;
	private static final Integer START_HTTP_3XX = 300;
	private static final Integer END_HTTP_3XX = 399;

	private static final String ESCAPE = "\\";
	private static final String DOT = ".";
	private static final String HYPHEN = "-";
	private static final String LEFT_BRACKET = "(";
	private static final String RIGHT_BRACKET = ")";

	private static final String PARSE_MODE_MARKDOWN_V2 = "MarkdownV2";
	private static final String TELEGRAM_BOT_URL = "https://api.telegram.org/bot";

	private static final String SEND_MESSAGE = "/sendMessage";
	private static final String SEND_PHOTO = "/sendPhoto";

	private final String botToken;

	public TelegramApi(String botToken) {
		this.botToken = botToken;
	}

	public TelegramResponseDTO sendMessage(SendMessageDTO dto) throws IOException {
		setParseMode(dto);
		escapeMessage(dto);
		truncateMessage(dto, MESSAGE_LENGTH_LIMIT);

		ObjectMapper objectMapper = JsonUtils.getMapper();
		String json = objectMapper.writeValueAsString(dto);
		String jsonResponse = sendRequest(TELEGRAM_BOT_URL + getBotToken() + SEND_MESSAGE, json);

		return objectMapper.readValue(jsonResponse, TelegramResponseDTO.class);
	}

	public TelegramResponseDTO sendPhoto(SendPhotoDTO dto) throws IOException {
		escapeMessage(dto);
		truncateMessage(dto, PHOTO_MESSAGE_LENGTH_LIMIT);

		String jsonResponse = null;
		ObjectMapper objectMapper = JsonUtils.getMapper();
		InputStream inputStream = dto.getInputStream();

		if (inputStream != null) {
			MultipartBuilder builder = new MultipartBuilder();
			builder.addText("chat_id", dto.getChatId());
			builder.addText("caption", dto.getMessage());
			builder.addText("parse_mode", dto.getParseMode());
			builder.addText("disable_notification", String.valueOf(dto.getDisableNotification()));
			builder.addText("reply_to_message_id", String.valueOf(dto.getReplyToMessageId()));
			builder.addFile("photo", dto.getPhoto(), dto.getMimeType(), inputStream);

			jsonResponse = sendRequest(TELEGRAM_BOT_URL + getBotToken() + SEND_PHOTO, builder);
		} else {
			String json = objectMapper.writeValueAsString(dto);
			jsonResponse = sendRequest(TELEGRAM_BOT_URL + getBotToken() + SEND_PHOTO, json);
		}

		return objectMapper.readValue(jsonResponse, TelegramResponseDTO.class);
	}

	private void setParseMode(ITextDTO dto) {
		String parseMode = dto.getParseMode();

		if (StringUtils.isEmpty(parseMode)) {
			dto.setParseMode(PARSE_MODE_MARKDOWN_V2);
		}
	}

	private void escapeMessage(ITextDTO dto) {
		String parseMode = dto.getParseMode();
		String message = dto.getMessage();

		if (PARSE_MODE_MARKDOWN_V2.equals(parseMode) && StringUtils.isNotEmpty(message)) {
			String newMessage = message.replace(DOT, ESCAPE + DOT);
			newMessage = newMessage.replace(LEFT_BRACKET, ESCAPE + LEFT_BRACKET);
			newMessage = newMessage.replace(RIGHT_BRACKET, ESCAPE + RIGHT_BRACKET);
			newMessage = newMessage.replace(HYPHEN, ESCAPE + HYPHEN);
			dto.setMessage(newMessage);
		}
	}

	private void truncateMessage(ITextDTO dto, Integer length) {
		String message = dto.getMessage();
		if (StringUtils.isNotEmpty(message)) {
			String newMessage = StringUtils.truncate(message, length);
			dto.setMessage(newMessage);
		}
	}

	private String sendRequest(String urlStr, String json) throws IOException {
		try (InputStream body = IOUtils.toInputStream(json, StandardCharsets.UTF_8)) {
			return sendRequest(urlStr, body, APPLICATION_JSON);
		}
	}

	private String sendRequest(String urlStr, MultipartBuilder builder) throws IOException {
		try (InputStream body = builder.build()) {
			String boundary = builder.getBoundary();
			String contentType = MULTIPART_FORM_DATA + "; boundary=" + boundary;

			return sendRequest(urlStr, body, contentType);
		}
	}

	private String sendRequest(String urlStr, InputStream body, String contentType) throws IOException {
		String response;
		URL url = new URL(urlStr);
		HttpURLConnection connection = null;

		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(POST);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty(CONTENT_TYPE, contentType);
			connection.setRequestProperty(CONTENT_LENGTH, String.valueOf(body.available()));

			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(CONNECTION_TIMEOUT);

			try (OutputStream outputStream = connection.getOutputStream()) {
				IOUtils.copy(body, outputStream);
				outputStream.flush();
			}

			Integer responseCode = connection.getResponseCode();

			if (responseCode >= START_HTTP_2XX && responseCode <= END_HTTP_3XX) {
				try (InputStream inputStream = connection.getInputStream()) {
					response = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				}
			} else {
				try (InputStream inputStream = connection.getErrorStream()) {
					response = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
				}
			}

			if (responseCode >= START_HTTP_3XX && responseCode <= END_HTTP_3XX) {
				response = sendRequest(response, body, contentType); //redirect
			}
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return response;
	}

	public String getBotToken() {
		return botToken;
	}
}
