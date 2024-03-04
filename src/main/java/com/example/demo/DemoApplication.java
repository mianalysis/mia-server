package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.imagej.patcher.LegacyInjector;

@SpringBootApplication
public class DemoApplication {
	static {
        LegacyInjector.preinit();
    }
	
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
