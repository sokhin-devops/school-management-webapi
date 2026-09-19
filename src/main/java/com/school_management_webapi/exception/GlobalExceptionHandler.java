package com.school_management_webapi.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.school_management_webapi.dto.response.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null, "RESOURCE_NOT_FOUND");
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request, null, "DUPLICATE_RESOURCE");
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null, "INVALID_CREDENTIALS");
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null, "INVALID_TOKEN");
	}

	@ExceptionHandler(PasswordMismatchException.class)
	public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex, WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null, "PASSWORD_MISMATCH");
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
		return build(ex.getStatus(), ex.getMessage(), request, null, ex.getErrorCode());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(
				error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
		ex.getBindingResult().getGlobalErrors().forEach(
				error -> fieldErrors.put(error.getObjectName(), error.getDefaultMessage()));
		return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors, "VALIDATION_FAILED");
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			WebRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		ex.getConstraintViolations().forEach(
				violation -> fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage()));
		return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors, "VALIDATION_FAILED");
	}

	/**
	 * Unparsable body: malformed JSON, an unknown enum constant, or a date that is
	 * not ISO-8601. Jackson's raw message leaks type names, so it is not echoed.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, WebRequest request) {
		log.debug("Unreadable request body", ex);
		return build(HttpStatus.BAD_REQUEST,
				"Request body is missing or malformed. Check field types, enum values and date formats.",
				request, null, "MALFORMED_REQUEST");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
			WebRequest request) {
		String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "the expected type";
		return build(HttpStatus.BAD_REQUEST,
				"Parameter '" + ex.getName() + "' is not a valid " + expected + ".",
				request, Map.of(ex.getName(), "invalid value"), "INVALID_PARAMETER");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
			WebRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Required parameter '" + ex.getParameterName() + "' is missing.",
				request, Map.of(ex.getParameterName(), "required"), "MISSING_PARAMETER");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
		log.warn("Data integrity violation on {}", path(request), ex);
		return build(HttpStatus.CONFLICT,
				"The request conflicts with existing data or violates a database constraint.",
				request, null, "DATA_INTEGRITY_VIOLATION");
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
		return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.", request, null,
				"FORBIDDEN");
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, WebRequest request) {
		return build(HttpStatus.NOT_FOUND, "No endpoint found for this request.", request, null, "ENDPOINT_NOT_FOUND");
	}

	/**
	 * Last-resort handler so an unexpected failure still returns the standard error
	 * envelope instead of a bare container error page. The cause is logged, never
	 * returned, so internals are not exposed to callers.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
		log.error("Unhandled exception on {}", path(request), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
				request, null, "INTERNAL_ERROR");
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request,
			Map<String, String> fieldErrors, String errorCode) {
		ErrorResponse body = new ErrorResponse(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path(request),
				fieldErrors,
				errorCode);
		return ResponseEntity.status(status).body(body);
	}

	private String path(WebRequest request) {
		return request.getDescription(false).replace("uri=", "");
	}
}
