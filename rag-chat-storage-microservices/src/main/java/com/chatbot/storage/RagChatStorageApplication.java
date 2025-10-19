package com.chatbot.storage;

import com.chatbot.storage.llm.config.LLMConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * The Rag Chat storage application.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableTransactionManagement
@EnableConfigurationProperties(LLMConfig.class)
public class RagChatStorageApplication {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RagChatStorageApplication.class, args);
    }
}