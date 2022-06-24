package com.poecat;

import com.poecat.inbox.DataStaxAstraProperties;
import com.poecat.inbox.email.EmailService;
import com.poecat.inbox.folders.Folder;
import com.poecat.inbox.folders.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Arrays;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class PostPidgeonApp {

	@Autowired
	FolderRepository folderRepository;
	@Autowired
	EmailService emailService;

	public static void main(String[] args) {
		SpringApplication.run(PostPidgeonApp.class, args);
	}

	@RequestMapping("/user")
	public String user(@AuthenticationPrincipal OAuth2User principal) {
		System.out.println(principal);
		return principal.getAttribute("name");
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	@PostConstruct
	public void init() {
		folderRepository.save(new Folder("poe-cat", "Work", "blue"));
		folderRepository.save(new Folder("poe-cat", "Entertainment", "green"));
		folderRepository.save(new Folder("poe-cat", "Family", "yellow"));

		for(int i = 0; i < 10; i++) {
			emailService.sendEmail("poe-cat", Arrays.asList("poe-cat", "abc"), "Hello " + i, "body");
		}
	}
}
