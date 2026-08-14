package com.school_management_webapi.exception;

public class PasswordMismatchException extends RuntimeException {

	public PasswordMismatchException(String message) {
		super(message);
	}
}
