package com.poecat.inbox.controllers;

import com.poecat.inbox.email.EmailRepository;
import com.poecat.inbox.email.EmailService;
import com.poecat.inbox.folders.Folder;
import com.poecat.inbox.folders.FolderRepository;
import com.poecat.inbox.folders.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ComposeController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailRepository emailRepository;


    @GetMapping(value = "/compose")
    public String getComposePage(@RequestParam(required = false) String to, @RequestParam(required = false) String replayToEmailId, @AuthenticationPrincipal OAuth2User principal, Model model) {

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
            Map<String, Integer> folderToUnreadCounts = folderService.getUnreadCountsMap(loginId);
            model.addAttribute("folderToUnreadCounts", folderToUnreadCounts);

            return "compose-page";
        }
        return "index";
    }

    private List<String> splitIds(String to) {

        if(!StringUtils.hasText(to)) {
            return new ArrayList<String>();
        }
        String[] splitIds = to.split(",");
        List<String> uniqueToIds = Arrays.asList(splitIds)
                .stream()
                .map(id -> StringUtils.trimWhitespace(id))
                .filter(id -> StringUtils.hasText(id))
                .distinct()
                .collect(Collectors.toList());
        return uniqueToIds;
    }


    @PostMapping(value = "/sendEmail")
    public ModelAndView sendEmail(
            @RequestBody MultiValueMap<String, String> formData,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        if (principal == null || principal.getAttribute("login") == null) {
            return null;
        }
        String toIds = formData.getFirst("toIds");
        String subject = formData.getFirst("subject");
        String body = formData.getFirst("body");
        String from = principal.getAttribute("login");

        emailService.sendEmail(from, toIds, subject, body);



        return new ModelAndView("redirect:/");
    }
}
