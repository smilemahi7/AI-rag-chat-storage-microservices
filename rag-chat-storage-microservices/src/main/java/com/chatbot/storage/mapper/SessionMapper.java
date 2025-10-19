package com.chatbot.storage.mapper;

import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 *
 * The interface Session mapper.
 */
@Mapper(componentModel = "spring")
public interface SessionMapper {

    /**
     * To response session response.
     *
     * @param session the session
     * @return the session response
     */
    @Mapping(target = "messageCount", expression = "java(Long.valueOf(session.getMessages().size()))")
    SessionResponse toResponse(ChatSession session);

    /**
     * To response list list.
     *
     * @param sessions the sessions
     * @return the list
     */
    List<SessionResponse> toResponseList(List<ChatSession> sessions);

    /**
     * To paged response paged response.
     *
     * @param page the page
     * @return the paged response
     */
    default PagedResponse<SessionResponse> toPagedResponse(Page<ChatSession> page) {
        return PagedResponse.<SessionResponse>builder()
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