package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;


public class BookPublishRequestManager {
    private Queue<BookPublishRequest> bookPublishRequestsQueue;

    @Inject
    public BookPublishRequestManager() {
        bookPublishRequestsQueue = new LinkedList<>();
    }

    public void addBookPublishRequest(BookPublishRequest request) {
        bookPublishRequestsQueue.add(request);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        if (this.bookPublishRequestsQueue.isEmpty()) return null;

        BookPublishRequest nextInQueue = this.bookPublishRequestsQueue.remove();
        return nextInQueue;

    }

}
