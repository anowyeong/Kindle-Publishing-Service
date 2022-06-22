package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class BookPublishRequestManager {
    private final Queue<BookPublishRequest> bookPublishRequestsQueue;

    @Inject
    public BookPublishRequestManager() {
        this.bookPublishRequestsQueue = new ConcurrentLinkedQueue<>();
    }

    public void addBookPublishRequest(BookPublishRequest request) {
        bookPublishRequestsQueue.add(request);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        if (this.bookPublishRequestsQueue.isEmpty()) return null;

        BookPublishRequest nextInQueue = this.bookPublishRequestsQueue.remove();
        return nextInQueue;

    }

    public Queue<BookPublishRequest> getBookPublishRequestsQueue() {
        return bookPublishRequestsQueue;
    }
}
