/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author fernandoglatz
 */
public class SendMessageDTO implements ITextDTO {

	@SerializedName("chat_id")
	private String chatId;

	@SerializedName("text")
	private String message;

	@SerializedName("parse_mode")
	private String parseMode;

	@SerializedName("disable_web_page_preview")
	private Boolean disableWebPagePreview = false;

	@SerializedName("disable_notification")
	private Boolean disableNotification = false;

	@SerializedName("reply_to_message_id")
	private Integer replyToMessageId;

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
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

	public Boolean getDisableWebPagePreview() {
		return disableWebPagePreview;
	}

	public void setDisableWebPagePreview(Boolean disableWebPagePreview) {
		this.disableWebPagePreview = disableWebPagePreview;
	}

	public Boolean getDisableNotification() {
		return disableNotification;
	}

	public void setDisableNotification(Boolean disableNotification) {
		this.disableNotification = disableNotification;
	}

}
