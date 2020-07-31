package com.aobuchow.sample.commander.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.goxr3plus.streamplayer.tools.TimeTool;

public class TestJavaStreamPlayerApiAccess {

	@Test
	void canAccessApi() {
		String timeEdited = TimeTool.getTimeEdited(60);
		assertEquals("01m:00", timeEdited);
	}

}
