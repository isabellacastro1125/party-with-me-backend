package com.icas.party_with_me.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.icas.party_with_me.data.DAO.User;
import com.icas.party_with_me.security.JWTUtil;
import com.icas.party_with_me.data.Role;
import com.icas.party_with_me.data.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("api/auth")
public class GoogleAuthController {

	private static final String CLIENT_ID = "1064503684322-91bk41qv8p00bi5l3v0muci308dl7jd0.apps.googleusercontent.com";
	@Value("${google.client.secret}")
	private String CLIENT_SECRET; // Injected from application.properties
	private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private final UserRepository userRepository;
	private final JWTUtil jwtUtil;

	public GoogleAuthController(UserRepository userRepository, JWTUtil jwtUtil) {
		this.userRepository = userRepository;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/google")
	public ResponseEntity<?> authenticateGoogleToken(@RequestBody TokenRequest tokenRequest) throws Exception {
		System.out.println("TOKEN: " + tokenRequest.getToken());
		// Exchange authorization code for tokens
		RestTemplate restTemplate = new RestTemplate();

		// Use MultiValueMap for form-urlencoded data
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("code", tokenRequest.getToken());
		params.add("client_id", CLIENT_ID);
		params.add("client_secret", CLIENT_SECRET); // Use the injected client secret
		params.add("redirect_uri", "http://localhost:4200/assets/close.html");
		params.add("grant_type", "authorization_code");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		// Make the POST request
		ResponseEntity<Map> response = restTemplate.postForEntity(TOKEN_ENDPOINT, request, Map.class);

		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			Map<String, Object> responseBody = response.getBody();

			// Extract the ID token
			String idTokenString = (String) responseBody.get("id_token");

			// Verify ID token
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JSON_FACTORY)
					.setAudience(Collections.singletonList(CLIENT_ID)).build();

			GoogleIdToken idToken = verifier.verify(idTokenString);
			if (idToken != null) {
				GoogleIdToken.Payload payload = idToken.getPayload();

				// Extract user information
				String email = payload.getEmail();
				String name = (String) payload.get("name");

				// Check if the user exists
				Optional<User> existingUser = userRepository.findByEmail(email);
				User user;
				if (existingUser.isPresent()) {
					user = existingUser.get();
				} else {
					// Create a new user
					user = new User();
					user.setEmail(email);
					user.setName(name);
					user.setPassword("password"); // No password for OAuth users
					user.setRole(Role.ADMIN); // Default role
					userRepository.save(user);
				}

				// Generate JWT Token
				String jwtToken = jwtUtil.generateToken(user.getEmail());

				// Prepare response
				Map<String, String> authResponse = new HashMap<>();
				authResponse.put("token", jwtToken);
				authResponse.put("email", user.getEmail());
				authResponse.put("name", user.getName());
				
				System.out.println("JWT: "+authResponse);
				
				return ResponseEntity.ok(authResponse);
			} else {
				return ResponseEntity.badRequest().body("Invalid Google ID token.");
			}
		} else {
			return ResponseEntity.status(response.getStatusCode()).body("Failed to exchange code for token.");
		}
	}

	static class TokenRequest {
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}
}
