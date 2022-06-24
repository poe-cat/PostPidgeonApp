package com.poecat.inbox.email;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.poecat.inbox.emaillist.EmailListItem;
import com.poecat.inbox.emaillist.EmailListItemKey;
import com.poecat.inbox.emaillist.EmailListItemRepository;
import com.poecat.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    public void sendEmail(String from, List<String> to, String subject, String body) {

        Email email = new Email();
        email.setTo(to);
        email.setFrom(from);
        email.setSubject(subject);
        email.setBody(body);
        email.setId(Uuids.timeBased());
        emailRepository.save(email);

        to.forEach(toId -> {
            EmailListItem item = createEmailListItem(to, subject, email, toId, "Inbox");
            emailListItemRepository.save(item);
            unreadEmailStatsRepository.incrementUnreadCount(toId, "Inbox");
        });

        EmailListItem sentItemsEntry = createEmailListItem(to, subject, email, from, "Sent items");
        sentItemsEntry.setUnread(false);
        emailListItemRepository.save(sentItemsEntry);
    }


    private EmailListItem createEmailListItem(List<String> to, String subject,
                                              Email email, String itemOwner, String folderName) {

        EmailListItemKey key = new EmailListItemKey();
        key.setId(itemOwner);
        key.setLabel(folderName);
        key.setTimeUUID(email.getId());

        EmailListItem item = new EmailListItem();
        item.setKey(key);
        item.setTo(to);
        item.setSubject(subject);
        item.setUnread(true);
        return item;
    }
}
