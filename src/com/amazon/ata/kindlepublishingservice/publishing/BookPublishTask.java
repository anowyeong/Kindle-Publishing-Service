package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;


public class BookPublishTask implements Runnable {

    private final BookPublishRequestManager manager;
    private final CatalogDao catalogDao;
    private final PublishingStatusDao publishingStatusDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager manager, CatalogDao catalogDao, PublishingStatusDao publishingStatusDao) {
        this.manager = manager;
        this.catalogDao = catalogDao;
        this.publishingStatusDao = publishingStatusDao;
    }


    @Override
    public void run() {

        BookPublishRequest request = manager.getBookPublishRequestToProcess();
        KindleFormattedBook formattedBook;

//        System.out.println("^^^^^^^^^^^\n"+request+"\n^^^^^^^^^^^\n");

        if (request != null) {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());
            formattedBook = KindleFormatConverter.format(request);
//            String bookId = request.getBookId() == null ? KindlePublishingUtils.generateBookId() : request.getBookId();

            try {
                CatalogItemVersion book = catalogDao.createdOrUpdateBook(formattedBook);
                PublishingStatusItem item = publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, book.getBookId());
            } catch (Exception e) {
                publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId(), e.getMessage());
            }
        } else {
            return;
        }

    }
}
