package com.school_management_webapi.service;

public interface EmailService {

	void sendPasswordResetEmail(String toEmail, String resetToken);
}
