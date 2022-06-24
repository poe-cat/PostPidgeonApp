package com.poecat.inbox.controllers;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import com.poecat.inbox.emaillist.EmailListItem;
import com.poecat.inbox.emaillist.EmailListItemRepository;
import com.poecat.inbox.folders.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class InboxController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailListItemRepository emailListItemRepository;


    private PrettyTime prettyTime = new PrettyTime();

    @GetMapping(value = "/")
    public String getHomePage(@RequestParam(required = false) String folder,
                              @AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null && principal.getAttribute("login") != null) {
            String loginId = principal.getAttribute("login");
            List<Folder> folders = folderRepository.findAllById(loginId);
            List<Folder> initFolders = folderService.init(loginId);
            // initFolders.stream().forEach(folderRepository::save);
            model.addAttribute("defaultFolders", initFolders);
            if (folders.size() > 0) {
                model.addAttribute("userFolders", folders);
            }
            if (StringUtils.isBlank(folder)) {
                folder = "Inbox";
            }
            model.addAttribute("currentFolder", folder);
            Map<String, Integer> folderToUnreadCounts = folderService.getUnreadCountsMap(loginId);
            model.addAttribute("folderToUnreadCounts", folderToUnreadCounts);
            List<EmailListItem> emails = emailListItemRepository.findAllByKey_IdAndKey_Label(loginId, folder);
            emails.stream().forEach(email -> {
                Date emailDate = new Date(Uuids.unixTimestamp(email.getKey().getTimeUUID()));
                email.setAgoTimeString(prettyTime.format(emailDate));
            });
            model.addAttribute("folderEmails", emails);
            model.addAttribute("userName", principal.getAttribute("name"));

            return "inbox-page";
        }
        return "index";

    }
}
