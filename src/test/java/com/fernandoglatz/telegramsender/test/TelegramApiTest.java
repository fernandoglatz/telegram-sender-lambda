/*
 * Copyright 2020 Fernando Glatz. All Rights Reserved.
 */
package com.fernandoglatz.telegramsender.test;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fernandoglatz.telegramsender.dto.SendMessageDTO;
import com.fernandoglatz.telegramsender.dto.SendPhotoDTO;

/**
 * @author fernandoglatz
 */
public class TelegramApiTest {

	@Test
	public void sendMessageTest() throws IOException {
		SendMessageDTO dto = new SendMessageDTO();
		dto.setChatId("*****");
		dto.setMessage("Test message");

		//		TelegramApi telegramApi = new TelegramApi("*****");
		//		TelegramResponseDTO dtoResponse = telegramApi.sendMessage(dto);
		//		Boolean completed = dtoResponse.getCompleted();
		//		System.out.println(completed);
	}

	@Test
	public void sendPhotoTest() throws IOException {
		try (FileInputStream inputStream = new FileInputStream("/tmp/GitHub-Mark.png")) {

			SendPhotoDTO dto = new SendPhotoDTO();
			dto.setChatId("***");
			dto.setPhoto("teste.png");
			dto.setInputStream(inputStream);
			dto.setMimeType("image/png");

			//			TelegramApi telegramApi = new TelegramApi("******");
			//			TelegramResponseDTO dtoResponse = telegramApi.sendPhoto(dto);
			//			Boolean completed = dtoResponse.getCompleted();
			//			System.out.println(completed);
		}
	}
}
