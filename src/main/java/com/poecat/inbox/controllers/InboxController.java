package com.poecat.inbox.controllers;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.poecat.inbox.emaillist.EmailListItem;
import com.poecat.inbox.emaillist.EmailListItemRepository;
import com.poecat.inbox.folders.Folder;
import com.poecat.inbox.folders.FolderRepository;
import com.poecat.inbox.folders.FolderService;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class InboxController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailListItemRepository emailListItemRepository;


    @GetMapping(value = "/")
    public String homePage(@AuthenticationPrincipal OAuth2User principal,
                            Model model) {

        if(principal == null || !StringUtils.hasText(principal.getAttribute("login")) ) {
            return "index";
        }

        // Fetch folders
        String userId = principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);

        List<Folder> defaultFolders = folderService.fetchDefaultFolders(userId);
        model.addAttribute("defaultFolders", defaultFolders);

        // Fetch messages
        String folderLabel = "Inbox";
        List<EmailListItem> emailList = emailListItemRepository.findAllByKey_IdAndKey_Label(userId, folderLabel);

        PrettyTime p = new PrettyTime();
        emailList.stream().forEach(emailItem -> {
            UUID timeUuid = emailItem.getKey().getTimeUUID();
            Date emailDateTime = new Date(Uuids.unixTimestamp(timeUuid));
            emailItem.setAgoTimeString(p.format(emailDateTime));
        });

        model.addAttribute("emailList", emailList);

        return "inbox-page";
    }
}
