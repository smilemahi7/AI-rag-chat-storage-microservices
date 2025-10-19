package com.chatbot.storage.mapper;

import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 *
 * The interface Message mapper.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    /**
     * To response message response.
     *
     * @param message the message
     * @return the message response
     */
    @Mapping(target = "sessionId", source = "session.id")
    MessageResponse toResponse(ChatMessage message);

    /**
     * To response list list.
     *
     * @param messages the messages
     * @return the list
     */
    List<MessageResponse> toResponseList(List<ChatMessage> messages);

    /**
     * To paged response paged response.
     *
     * @param page the page
     * @return the paged response
     */
    default PagedResponse<MessageResponse> toPagedResponse(Page<ChatMessage> page) {
        return PagedResponse.<MessageResponse>builder()
                .content(toResponseList(page.getContent()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}