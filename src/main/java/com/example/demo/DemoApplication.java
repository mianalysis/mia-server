package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.modules.DisplayImage;
import com.example.demo.modules.DisplayMessage;

import io.github.mianalysis.mia.module.AvailableModules;
import net.imagej.patcher.LegacyInjector;

@SpringBootApplication
public class DemoApplication {
	static {
        LegacyInjector.preinit();
    }
	
	public static void main(String[] args) {
		AvailableModules.addModuleName(DisplayMessage.class);
		AvailableModules.addModuleName(DisplayImage.class);
		SpringApplication.run(DemoApplication.class, args);
	}
}
