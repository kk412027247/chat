package com.wangluoguimi.chat.service;

import com.wangluoguimi.chat.exception.ResourceNotFoundException;
import com.wangluoguimi.chat.model.ChatMessage;
import com.wangluoguimi.chat.model.MessageStatus;
import com.wangluoguimi.chat.repository.ChatMessageRepository;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class ChatMessageService {

  final private ChatMessageRepository repository;
  final private ChatRoomService chatRoomService;
  final private MongoOperations mongoOperations;

  @Autowired
  public ChatMessageService(ChatMessageRepository repository, ChatRoomService chatRoomService, MongoOperations mongoOperations) {
    this.repository = repository;
    this.chatRoomService = chatRoomService;
    this.mongoOperations = mongoOperations;
  }

  public ChatMessage save(ChatMessage chatMessage){
    chatMessage.setStatus(MessageStatus.RECEIVED);
    repository.save(chatMessage);
    return chatMessage;
  }

  public long countNewMessages(String senderId, String recipientId){
    return repository.countBySenderIdAndRecipientIdAndStatus(senderId, recipientId, MessageStatus.RECEIVED);
  }

  public List<ChatMessage> findChatMessages(String senderId, String recipientId){

    Optional<String> chatId = chatRoomService.getChatId(senderId, recipientId, false);

    List<ChatMessage> messages = chatId.map(repository::findByChatId).orElse(new ArrayList<>());

    if (messages.size() > 0){
      updateStatuses(senderId, recipientId, MessageStatus.DELIVERED);
    }

    return messages;
  }

  public void updateStatuses  (String senderId, String recipientId, MessageStatus status){
    Query query = new Query(
        Criteria.where("senderId").is(senderId)
        .and("recipientId").is(recipientId));
    Update update = Update.update("status", status);
    mongoOperations.updateMulti(query, update, ChatMessage.class);
  }

  public ChatMessage findById(String id){
    return repository.findById(id)
        .map(chatMessage -> {
          chatMessage.setStatus(MessageStatus.DELIVERED);
          return repository.save(chatMessage);
        }).orElseThrow(()->
            new ResourceNotFoundException("can't find message (" + id +")"));
  }


}
