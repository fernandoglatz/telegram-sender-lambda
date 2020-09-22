/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.dto;

/**
 * @author fernandoglatz
 */
public interface ITextDTO {

	public String getMessage();

	public void setMessage(String message);

	public String getParseMode();

	public void setParseMode(String parseMode);

}
