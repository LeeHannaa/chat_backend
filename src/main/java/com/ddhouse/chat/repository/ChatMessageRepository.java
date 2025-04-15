package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.ChatMessage;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends ReactiveCassandraRepository<ChatMessage, UUID> {
    @AllowFiltering
    Mono<ChatMessage> findById(UUID id);
}
