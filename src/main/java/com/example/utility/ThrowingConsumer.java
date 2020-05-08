package com.example.utility;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
	String accept() throws E;

}
