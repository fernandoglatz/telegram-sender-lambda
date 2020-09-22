/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3BucketEntity;
import com.amazonaws.services.s3.event.S3EventNotification.S3Entity;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.event.S3EventNotification.S3ObjectEntity;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fernandoglatz.telegramsender.dto.SendMessageDTO;
import com.fernandoglatz.telegramsender.dto.SendPhotoDTO;
import com.fernandoglatz.telegramsender.dto.TelegramResponseDTO;
import com.fernandoglatz.telegramsender.util.EmailUtils;
import com.fernandoglatz.telegramsender.util.TelegramApi;

/**
 * @author fernandoglatz
 */
public class SNSHandler extends AbstractRequestHandler<SNSEvent, String> {

	private static final String BOT_TOKEN = "bot_token";
	private static final String CHAT_ID = "chat_id";
	private static final String PARSE_MODE = "parse_mode";
	private static final String SUBJECT = "subject";
	private static final String CONTENT = "content";
	private static final String MESSAGE = "message";
	private static final String SEPARATOR = ";";

	@Override
	public String handleRequest(SNSEvent event, Context context) {
		try {
			List<SNSRecord> records = event.getRecords();
			for (SNSRecord record : records) {
				SNS sns = record.getSNS();
				String message = sns.getMessage();

				S3EventNotification s3Event = S3EventNotification.parseJson(message);
				List<S3EventNotificationRecord> s3Records = s3Event.getRecords();

				for (S3EventNotificationRecord s3record : s3Records) {
					S3Entity s3 = s3record.getS3();
					S3BucketEntity bucket = s3.getBucket();
					S3ObjectEntity object = s3.getObject();

					String bucketName = bucket.getName();
					String objectKey = object.getKey();

					AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
					S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));

					try (InputStream inputStream = s3Object.getObjectContent()) {
						processEmail(inputStream);
					}
				}
			}
		} catch (Exception e) {
			logError(e);
			return "Error on execution";
		}

		return "Executed";
	}

	private void processEmail(InputStream inputStream) throws MessagingException, IOException {
		Map<String, String> env = System.getenv();
		String botToken = env.get(BOT_TOKEN);
		String chatId = env.get(CHAT_ID);
		String parseMode = env.get(PARSE_MODE);
		String subject = env.get(SUBJECT);
		String content = env.get(CONTENT);
		String message = env.get(MESSAGE);
		String[] chatIds = StringUtils.trimToEmpty(chatId).split(SEPARATOR);

		MimeMessage mimeMessage = new MimeMessage(null, inputStream);
		String emailSubject = mimeMessage.getSubject();
		String emailContent = EmailUtils.getContent(mimeMessage);

		logInfo("Subject: " + subject);
		//logInfo("Content: " + emailContent);

		if (StringUtils.isEmpty(message)) {
			message = emailContent;
		}

		boolean subjectMatch = StringUtils.isNotEmpty(subject) ? subject.equalsIgnoreCase(emailSubject) : true;
		boolean contentMatch = StringUtils.isNotEmpty(content) ? StringUtils.containsIgnoreCase(emailContent, content) : true;

		if (subjectMatch && contentMatch) {
			TelegramApi telegramApi = new TelegramApi(botToken);
			Map<String, InputStream> attachments = EmailUtils.getAttachments(mimeMessage);
			Set<Entry<String, InputStream>> entries = attachments.entrySet();

			try {
				for (String id : chatIds) {
					sendMessage(telegramApi, id, message, parseMode);
					for (Entry<String, InputStream> entry : entries) {
						String fileName = entry.getKey();
						InputStream fileInputStream = entry.getValue();
						fileInputStream.reset(); //ByteArrayInputStream

						sendPhoto(telegramApi, chatId, fileName, fileInputStream);
					}
				}
			} finally {
				for (Entry<String, InputStream> entry : entries) {
					IOUtils.close(entry.getValue());
				}
			}
		} else {
			logInfo("Ignoring email...");
		}
	}

	private void sendMessage(TelegramApi telegramApi, String id, String message, String parseMode) throws IOException {
		SendMessageDTO dto = new SendMessageDTO();
		dto.setChatId(id);
		dto.setMessage(message);
		dto.setParseMode(parseMode);

		logInfo("Sending to " + id + " message " + message);

		TelegramResponseDTO dtoResponse = telegramApi.sendMessage(dto);
		Boolean completed = dtoResponse.getCompleted();

		logInfo("Telegram response: " + completed);
	}

	private void sendPhoto(TelegramApi telegramApi, String id, String fileName, InputStream fileInputStream) throws IOException {
		String mimeType = URLConnection.guessContentTypeFromName(fileName);

		SendPhotoDTO dto = new SendPhotoDTO();
		dto.setChatId(id);
		dto.setMessage(fileName);
		dto.setPhoto(fileName);
		dto.setInputStream(fileInputStream);
		dto.setMimeType(mimeType);

		logInfo("Sending to " + id + " photo " + fileName);

		TelegramResponseDTO dtoResponse = telegramApi.sendPhoto(dto);
		Boolean completed = dtoResponse.getCompleted();

		logInfo("Telegram response: " + completed);
	}
}
