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

import com.fernandoglatz.telegramsender.builder.MultipartBuilder;
import com.fernandoglatz.telegramsender.dto.ITextDTO;
import com.fernandoglatz.telegramsender.dto.SendMessageDTO;
import com.fernandoglatz.telegramsender.dto.SendPhotoDTO;
import com.fernandoglatz.telegramsender.dto.TelegramResponseDTO;
import com.google.gson.Gson;

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

	private static final String PARSE_MODE_DEFAULT = "MarkdownV2";
	private static final String TELEGRAM_BOT_URL = "https://api.telegram.org/bot";

	private static final String SEND_MESSAGE = "/sendMessage";
	private static final String SEND_PHOTO = "/sendPhoto";

	private final String botToken;

	public TelegramApi(String botToken) {
		this.botToken = botToken;
	}

	public TelegramResponseDTO sendMessage(SendMessageDTO dto) throws IOException {
		setParseMode(dto);

		Gson gson = new Gson();
		String json = gson.toJson(dto);
		String jsonResponse = sendRequest(TELEGRAM_BOT_URL + getBotToken() + SEND_MESSAGE, json);

		return gson.fromJson(jsonResponse, TelegramResponseDTO.class);
	}

	public TelegramResponseDTO sendPhoto(SendPhotoDTO dto) throws IOException {
		setParseMode(dto);

		Gson gson = new Gson();
		String jsonResponse = null;
		InputStream inputStream = dto.getInputStream();
		String message = dto.getMessage();

		if (inputStream != null) {

			MultipartBuilder builder = new MultipartBuilder();
			builder.addText("chat_id", dto.getChatId());
			if (StringUtils.isNotEmpty(message)) {
				builder.addText("caption", message);
			}
			builder.addText("parse_mode", dto.getParseMode());
			builder.addText("disable_notification", String.valueOf(dto.getDisableNotification()));
			builder.addText("reply_to_message_id", String.valueOf(dto.getReplyToMessageId()));
			builder.addFile("photo", dto.getPhoto(), dto.getMimeType(), inputStream);

			jsonResponse = sendRequest(TELEGRAM_BOT_URL + getBotToken() + SEND_PHOTO, builder);
		} else {
			String json = gson.toJson(dto);
			jsonResponse = sendRequest(TELEGRAM_BOT_URL + getBotToken() + SEND_PHOTO, json);
		}

		return gson.fromJson(jsonResponse, TelegramResponseDTO.class);
	}

	private void setParseMode(ITextDTO dto) {
		if (StringUtils.isEmpty(dto.getParseMode())) {
			dto.setParseMode(PARSE_MODE_DEFAULT);
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

			try (InputStream inputStream = connection.getInputStream()) {
				response = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
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
