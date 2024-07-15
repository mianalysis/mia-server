package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.demo.modules.AddPathsToMetadata;
import com.example.demo.modules.DisplayImage;
import com.example.demo.modules.DisplayMessage;

import io.github.mianalysis.mia.module.AvailableModules;
import net.imagej.patcher.LegacyInjector;

@SpringBootApplication
public class MIAServer {
	static {
        LegacyInjector.preinit();
    }
	
	public static void main(String[] args) {
		AvailableModules.addModuleName(AddPathsToMetadata.class);
		AvailableModules.addModuleName(DisplayImage.class);
		AvailableModules.addModuleName(DisplayMessage.class);
		
		SpringApplication.run(MIAServer.class, args);
	}
}
