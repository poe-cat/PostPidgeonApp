package com.poecat;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.poecat.inbox.DataStaxAstraProperties;
import com.poecat.inbox.email.Email;
import com.poecat.inbox.email.EmailRepository;
import com.poecat.inbox.emaillist.EmailListItem;
import com.poecat.inbox.emaillist.EmailListItemKey;
import com.poecat.inbox.emaillist.EmailListItemRepository;
import com.poecat.inbox.folders.Folder;
import com.poecat.inbox.folders.FolderRepository;
import com.poecat.inbox.folders.UnreadEmailStatsRepository;
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
	EmailListItemRepository emailListItemRepository;
	@Autowired
	EmailRepository emailRepository;
	@Autowired
	UnreadEmailStatsRepository unreadEmailStatsRepository;

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
		folderRepository.save(new Folder("poe-cat", "Inbox", "blue"));
		folderRepository.save(new Folder("poe-cat", "Sent", "green"));
		folderRepository.save(new Folder("poe-cat", "Important", "yellow"));

		unreadEmailStatsRepository.incrementUnreadCount("poe-cat", "Inbox");
		unreadEmailStatsRepository.incrementUnreadCount("poe-cat", "Inbox");
		unreadEmailStatsRepository.incrementUnreadCount("poe-cat", "Inbox");

		for(int i = 0; i < 10; i++) {
			EmailListItemKey key = new EmailListItemKey();
			key.setId("poe-cat");
			key.setLabel("Inbox");
			key.setTimeUUID(Uuids.timeBased());

			EmailListItem item = new EmailListItem();
			item.setKey(key);
			item.setTo(Arrays.asList("poe-cat", "abc", "def"));
			item.setSubject("Subject " + i);
			item.setUnread(true);

			emailListItemRepository.save(item);

			Email email = new Email();
			email.setId(key.getTimeUUID());
			email.setFrom("poe-cat");
			email.setSubject(item.getSubject());
			email.setBody("Body " + i);
			email.setTo(item.getTo());

			emailRepository.save(email);
		}
	}
}
