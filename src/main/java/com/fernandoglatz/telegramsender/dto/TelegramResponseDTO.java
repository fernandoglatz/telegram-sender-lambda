/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author fernandoglatz
 */
public class TelegramResponseDTO {

	@SerializedName("ok")
	private Boolean completed;

	@SerializedName("description")
	private String message;

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
