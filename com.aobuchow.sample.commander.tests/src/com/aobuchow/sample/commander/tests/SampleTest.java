package com.aobuchow.sample.commander.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SampleTest {

	String x;

	@BeforeEach
	void prep() {
		x = "hello";
	}

	@Test
	void xisHello() {
		assertEquals("hello", x);
	}

}
