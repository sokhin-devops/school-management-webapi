package com.school_management_webapi.service;

import java.util.UUID;

import com.school_management_webapi.dto.request.ForgotPasswordRequest;
import com.school_management_webapi.dto.request.LoginRequest;
import com.school_management_webapi.dto.request.TwoFactorRequest;
import com.school_management_webapi.dto.request.RegisterRequest;
import com.school_management_webapi.dto.request.ResetPasswordRequest;
import com.school_management_webapi.dto.response.AuthResponse;
import com.school_management_webapi.dto.response.ForgotPasswordResponse;
import com.school_management_webapi.dto.response.UserResponse;

public interface AuthService {

	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	/** The second step for an account with two-factor on. */
	AuthResponse loginWithTwoFactor(TwoFactorRequest.SignIn request);

	void logout(String refreshToken);

	AuthResponse refreshToken(String refreshToken);

	ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request);

	void resetPassword(ResetPasswordRequest request);

	UserResponse getCurrentUser(UUID userId);
}
