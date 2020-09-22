/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.dto;

import java.io.InputStream;

import com.google.gson.annotations.SerializedName;

/**
 * @author fernandoglatz
 */
public class SendPhotoDTO implements ITextDTO {

	@SerializedName("chat_id")
	private String chatId;

	@SerializedName("photo")
	private String photo;

	@SerializedName("caption")
	private String message;

	@SerializedName("parse_mode")
	private String parseMode;

	@SerializedName("disable_notification")
	private Boolean disableNotification = false;

	@SerializedName("reply_to_message_id")
	private Integer replyToMessageId;

	private transient InputStream inputStream;
	private transient String mimeType;

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getParseMode() {
		return parseMode;
	}

	@Override
	public void setParseMode(String parseMode) {
		this.parseMode = parseMode;
	}

	public Boolean getDisableNotification() {
		return disableNotification;
	}

	public void setDisableNotification(Boolean disableNotification) {
		this.disableNotification = disableNotification;
	}

	public Integer getReplyToMessageId() {
		return replyToMessageId;
	}

	public void setReplyToMessageId(Integer replyToMessageId) {
		this.replyToMessageId = replyToMessageId;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
