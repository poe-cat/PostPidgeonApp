package com.poecat.inbox.folders;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FolderService {

    public List<Folder> fetchDefaultFolders(String userId) {
        return Arrays.asList(
                new Folder(userId, "Inbox", "blue"),
                new Folder(userId, "Sent items", "green"),
                new Folder(userId, "Important", "red")
        );
    }
}
