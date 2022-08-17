package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    /**
     *
     * @param bookId
     * @return
     */
    public CatalogItemVersion deleteBookSoft(String bookId) {
        // Retrieve book from Database
        CatalogItemVersion book = this.getBookFromCatalog(bookId);
        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        // Set found book to inactive
        book.setInactive(true);
        // Save to database
        dynamoDbMapper.save(book);
        return book;
    }

    /**
     * This method checks if a book is in the database even if inactive.
     * @param bookId id of book
     * @return CatalogItemVersion of the book in the database
     */
    public CatalogItemVersion getBook(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null ) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    public CatalogItemVersion createdOrUpdateBook(KindleFormattedBook formattedBook) {
        CatalogItemVersion book;
        String bookId = formattedBook.getBookId() == null ? KindlePublishingUtils.generateBookId() : formattedBook.getBookId();

        try {
            book = getBook(bookId);

            // updating book
            deleteBookSoft(book.getBookId());

            CatalogItemVersion newBook = save(formattedBook, bookId,false, book.getVersion() + 1);
            return newBook;
        } catch (BookNotFoundException e) {
            // adding new book

            CatalogItemVersion newBook = save(formattedBook, bookId,false, 1);
            return newBook;
        }

    }

    public CatalogItemVersion save(KindleFormattedBook book, String bookId, Boolean isInactive, int version) {
        CatalogItemVersion newBook = new CatalogItemVersion();
        newBook.setTitle(book.getTitle());
        newBook.setAuthor(book.getAuthor());
        newBook.setText(book.getText());
        newBook.setInactive(isInactive);
        newBook.setVersion(version);
        newBook.setGenre(book.getGenre());
        newBook.setBookId(bookId);
        dynamoDbMapper.save(newBook);
        return newBook;
    }

}
