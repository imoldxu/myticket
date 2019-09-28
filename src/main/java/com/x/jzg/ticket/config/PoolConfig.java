package com.x.jzg.ticket.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PoolConfig {

	@Bean("mypool")
	public ExecutorService initPool() {
		ExecutorService pool = Executors.newFixedThreadPool(50);
		return pool;
	}
	
}
