package io.github.mianalysis.miaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.mianalysis.mia.module.AvailableModules;
import io.github.mianalysis.miaserver.modules.AddPathsToMetadata;
import io.github.mianalysis.miaserver.modules.DisplayGraph;
import io.github.mianalysis.miaserver.modules.DisplayImage;
import io.github.mianalysis.miaserver.modules.DisplayMessage;
import net.imagej.patcher.LegacyInjector;

@SpringBootApplication
public class MIAServer {
	static {
        LegacyInjector.preinit();
    }
	
	public static void main(String[] args) {
		AvailableModules.addModuleName(AddPathsToMetadata.class);
		AvailableModules.addModuleName(DisplayGraph.class);
		AvailableModules.addModuleName(DisplayImage.class);
		AvailableModules.addModuleName(DisplayMessage.class);
		
		SpringApplication.run(MIAServer.class, args);
	}
}
