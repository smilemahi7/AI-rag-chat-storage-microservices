package com.chatbot.storage.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * The type Pagination properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.pagination")
public class PaginationProperties {

    private int defaultPageSize = 20;
    private int maxPageSize = 100;
    private String defaultSortField = "createdAt";
    private String defaultSortDirection = "desc";
}