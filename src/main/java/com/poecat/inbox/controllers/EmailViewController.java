package com.poecat.inbox.controllers;

import com.poecat.inbox.email.Email;
import com.poecat.inbox.email.EmailRepository;
import com.poecat.inbox.email.EmailService;
import com.poecat.inbox.emaillist.EmailListItem;
import com.poecat.inbox.emaillist.EmailListItemKey;
import com.poecat.inbox.emaillist.EmailListItemRepository;
import com.poecat.inbox.folders.Folder;
import com.poecat.inbox.folders.FolderRepository;
import com.poecat.inbox.folders.FolderService;
import com.poecat.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailViewController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;


    @GetMapping(value = "/email/{id}")
    public String getEmailPage(@PathVariable String id, @RequestParam String folder, @AuthenticationPrincipal OAuth2User principal, Model model) {

        if (principal != null && principal.getAttribute("login") != null) {
            String loginId = principal.getAttribute("login");
            List<Folder> folders = folderRepository.findAllById(loginId);
            List<Folder> initFolders = folderService.init(loginId);
            // initFolders.stream().forEach(folderRepository::save);
            model.addAttribute("defaultFolders", initFolders);
            model.addAttribute("userName", principal.getAttribute("name"));

            if (folders.size() > 0) {
                model.addAttribute("userFolders", folders);
            }

            try {
                UUID uuid = UUID.fromString(id);
                Optional<Email> optionalEmail = emailRepository.findById(uuid);
                if (optionalEmail.isPresent()) {
                    Email email = optionalEmail.get();
                    String toIds = String.join(",", email.getTo());
                    model.addAttribute("email", optionalEmail.get());
                    model.addAttribute("toIds", toIds);
                    EmailListItemKey key = new EmailListItemKey();
                    key.setId(loginId);
                    key.setLabel(folder);
                    key.setTimeUUID(email.getId());
                    Optional<EmailListItem> optionalEmailListItem = emailListItemRepository.findById(key);
                    if (!optionalEmailListItem.isPresent()) throw new IllegalArgumentException();
                    EmailListItem emailListItem = optionalEmailListItem.get();
                    if (!emailListItem.isRead()) {
                        unreadEmailStatsRepository.decrementUnreadCount(loginId, folder);
                    }
                    emailListItem.setRead(true);
                    emailListItemRepository.save(emailListItem);
                    Map<String, Integer> folderToUnreadCounts = folderService.getUnreadCountsMap(loginId);
                    model.addAttribute("folderToUnreadCounts", folderToUnreadCounts);

                    return "email-page";
                }
            } catch (IllegalArgumentException e) {
                return "inbox-page";
            }
        }
        return "index";

    }
}
