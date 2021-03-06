package com.wangluoguimi.chat.controller;


import com.wangluoguimi.chat.model.ChatMessage;
import com.wangluoguimi.chat.model.ChatNotification;
import com.wangluoguimi.chat.service.ChatMessageService;
import com.wangluoguimi.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;
  private final ChatMessageService chatMessageService;
  private final ChatRoomService chatRoomService;

  @Autowired
  public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageService chatMessageService, ChatRoomService chatRoomService) {
    this.messagingTemplate = messagingTemplate;
    this.chatMessageService = chatMessageService;
    this.chatRoomService = chatRoomService;
  }

  @MessageMapping("/chat")
  public void processMessage(@Payload ChatMessage chatMessage){
    Optional<String> chatId = chatRoomService.getChatId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true);
    chatMessage.setChatId(chatId.get());
    ChatMessage saved = chatMessageService.save(chatMessage);
    messagingTemplate.convertAndSendToUser(
        chatMessage.getRecipientId(),
        "/queue/messages",
        new ChatNotification(saved.getId(), saved.getSenderId(), saved.getSenderName()));
  }

  @GetMapping("/messages/{senderId}/{recipientId}/cound")
  public ResponseEntity<Long> countNewMessages(
      @PathVariable String senderId,
      @PathVariable String recipientId){
    return ResponseEntity.ok(chatMessageService.countNewMessages(senderId, recipientId));
  }

  @GetMapping("/messages/{senderId}/{recipientId}")
  public ResponseEntity<?> findChatMessages(@PathVariable String senderId, @PathVariable String recipientId){
    return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
  }

  @GetMapping("/messages/{id}")
  public ResponseEntity<?> findMessage(@PathVariable String id){
    return ResponseEntity.ok(chatMessageService.findById(id));
  }

}
