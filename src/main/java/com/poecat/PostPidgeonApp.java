package com.poecat;

import com.poecat.inbox.DataStaxAstraProperties;
import com.poecat.inbox.folders.Folder;
import com.poecat.inbox.folders.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class PostPidgeonApp {

	@Autowired
	FolderRepository folderRepository;

	public static void main(String[] args) {
		SpringApplication.run(PostPidgeonApp.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void init() {
		folderRepository.save(new Folder("PoeCat", "Inbox", "blue"));
		folderRepository.save(new Folder("PoeCat", "Sent", "green"));
		folderRepository.save(new Folder("PoeCat", "Important", "yellow"));
	}
}
