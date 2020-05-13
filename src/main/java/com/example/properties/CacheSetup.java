package com.example.properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;

public class CacheSetup {
	static Logger logger = Logger.getLogger(CacheSetup.class);

	
	CacheSetup() {
		CachingProvider provider = Caching.getCachingProvider();
		CacheManager cacheManager = provider.getCacheManager();
		MutableConfiguration<Long, String> configuration = 
				new MutableConfiguration<Long, String>()
					.setTypes(Long.class, String.class)
					.setStoreByValue(false)
					.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE))
					;
		Cache<Long, String> theCache = cacheManager.createCache("jCache", configuration);
		theCache.put(1L, "hello");
		String theValue = theCache.get(1L);
		logger.trace("the value retrieved from the cache is: " + theValue);
	}
	
	@Bean
	public Cache<Long, String> getCache(){
		Cache<Long, String> cacheInstance = Caching.getCache("jCache", Long.class, String.class);
		//Cache<Long, String> cacheContext = Cache.of(cacheInstance);
		return cacheInstance;
	}
}
