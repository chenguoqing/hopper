package com.hopper.session;

import java.util.UUID;

public class SessionIdGenerator {

	public static String generateSessionId() {
		return UUID.randomUUID().toString();
	}
}
