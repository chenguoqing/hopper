package com.hopper.session;

import java.util.UUID;

public class SessionIdGenerator {

	public static String generateSessionId() {
        //TODO:
		return UUID.randomUUID().toString();
	}
}
