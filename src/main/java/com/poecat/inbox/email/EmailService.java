package com.poecat.inbox.email;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.poecat.inbox.emaillist.EmailListItem;
import com.poecat.inbox.emaillist.EmailListItemKey;
import com.poecat.inbox.emaillist.EmailListItemRepository;
import com.poecat.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    public void sendEmail(String fromUserId, String toUserIds, String subject, String body) {

        UUID timeUuid = Uuids.timeBased();

        List<String> toUserIdList = Arrays.asList(toUserIds.split(",")).stream()
                .map(id -> StringUtils.trimWhitespace(id)).filter(id -> StringUtils.hasText(id))
                .collect(Collectors.toList());

        // Add to sent items of sender
        EmailListItem sentItemEntry = prepareEmailsListEntry("Sent", fromUserId, fromUserId, toUserIdList, subject,
                timeUuid);
        sentItemEntry.setRead(true);
        emailListItemRepository.save(sentItemEntry);

        // Add to inbox of each reciever
        toUserIdList.stream().forEach(toUserId -> {
            EmailListItem inboxEntry = prepareEmailsListEntry("Inbox", toUserId, fromUserId, toUserIdList, subject,
                    timeUuid);
            inboxEntry.setRead(false);
            emailListItemRepository.save(inboxEntry);
            unreadEmailStatsRepository.incrementUnreadCount(toUserId, "Inbox");
        });

        // Save email entity
        Email email = new Email();
        email.setId(timeUuid);
        email.setFrom(fromUserId);
        email.setTo(toUserIdList);
        email.setSubject(subject);
        email.setBody(body);
        emailRepository.save(email);

    }

    private EmailListItem prepareEmailsListEntry(String folderName, String forUser, String fromUserId,
                                              List<String> toUserIds, String subject, UUID timeUuid) {

        EmailListItemKey key = new EmailListItemKey();
        key.setLabel(folderName);
        key.setId(forUser);
        key.setTimeUUID(timeUuid);
        EmailListItem emailsListEntry = new EmailListItem();
        emailsListEntry.setKey(key);
        emailsListEntry.setFrom(fromUserId);
        emailsListEntry.setTo(toUserIds);
        emailsListEntry.setSubject(subject);
        return emailsListEntry;
    }
}
