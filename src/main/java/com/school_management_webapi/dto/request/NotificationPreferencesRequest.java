package com.school_management_webapi.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Every event's choice, sent whole: the screen saves what it shows. */
public record NotificationPreferencesRequest(
		@NotNull(message = "preferences is required") List<@Valid Choice> preferences) {

	public record Choice(
			@NotBlank(message = "key is required") String key,
			boolean inApp,
			boolean email) {
	}
}
