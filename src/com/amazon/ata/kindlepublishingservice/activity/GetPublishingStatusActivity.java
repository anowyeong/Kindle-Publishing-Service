package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatus;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class GetPublishingStatusActivity {

    private CatalogDao catalogDao;
    private PublishingStatusDao publishingStatusDao;

    @Inject
    public GetPublishingStatusActivity(CatalogDao catalogDao, PublishingStatusDao publishingStatusDao) {
        this.catalogDao = catalogDao;
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {

        List<PublishingStatusItem> publishingStatusItemList = publishingStatusDao.getPublishingStatuses(publishingStatusRequest.getPublishingRecordId());

        List<PublishingStatusRecord> publishingStatusRecords = new ArrayList<>();

        for (PublishingStatusItem item : publishingStatusItemList) {
            PublishingStatusRecord record = PublishingStatusRecord.builder()
                    .withBookId(item.getBookId())
                    .withStatus(item.getStatus().toString())
                    .withStatusMessage(item.getStatusMessage())
                    .build();

            publishingStatusRecords.add(record);
        }

        GetPublishingStatusResponse publishingStatusResponse = GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(publishingStatusRecords)
                .build();

        return publishingStatusResponse;
    }
}
