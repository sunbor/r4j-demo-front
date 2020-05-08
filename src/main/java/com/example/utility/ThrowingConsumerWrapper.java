package com.example.utility;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ThrowingConsumerWrapper {
	public static <T> Supplier<String> wrapFunction(ThrowingConsumer<T, Exception> throwingConsumer) {
			  
			    return () -> {
			        try {
			            return throwingConsumer.accept();
			        } catch (Exception ex) {
			            throw new RuntimeException(ex);
			        }
					//return (T) "oh no";
			    };
			}
}
