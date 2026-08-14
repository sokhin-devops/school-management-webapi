package com.school_management_webapi.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

	@Override
	public void sendPasswordResetEmail(String toEmail, String resetToken) {
		log.info("Password reset requested for {}. Reset token: {}", toEmail, resetToken);
	}
}
