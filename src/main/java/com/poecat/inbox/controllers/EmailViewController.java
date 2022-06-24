package com.poecat.inbox.controllers;

import com.poecat.inbox.email.Email;
import com.poecat.inbox.email.EmailRepository;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailViewController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired
    private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;


    @GetMapping(value = "/email/{id}")
    public String emailView(@PathVariable UUID id,  Model model,
                            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }

        // Fetch folders
        String userId = principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);

        List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
        model.addAttribute("defaultFolders", defaultFolders);
        model.addAttribute("userName", principal.getAttribute("name"));

        Optional<Email> optionalEmail = emailRepository.findById(id);
        if(!optionalEmail.isPresent()) {
            return "inbox-page";
        }
        Email email = optionalEmail.get();
        String toIds = String.join(", ", email.getTo());

        // Check if user is allowed to see the email
        if(!userId.equals(email.getFrom()) && !email.getTo().contains(userId)) {
            return "redirect:/";
        }
        model.addAttribute("email", email);
        model.addAttribute("toIds", toIds);

//        EmailListItemKey key = new EmailListItemKey();
//        key.setId(userId);
//        key.setLabel(folder);
//        key.setTimeUUID(email.getId());

//        Optional<EmailListItem> optionalEmailListItem = emailListItemRepository.findById(key);
//        if(optionalEmailListItem.isPresent()) {
//            EmailListItem emailListItem = optionalEmailListItem.get();
//            if(emailListItem.isUnread()) {
//                emailListItem.setUnread(false);
//                emailListItemRepository.save(emailListItem);
////                unreadEmailStatsRepository.decrementUnreadCount(userId, folder);
//            }
//        }
        model.addAttribute("stats", folderService.mapCountToLabels(userId));

        return "email-page";
    }
}
