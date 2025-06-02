package com.ddhouse.chat.service;

import com.ddhouse.chat.dto.SaveMessageDto;
import com.ddhouse.chat.repository.ChatRoomMessageRepository;
import com.ddhouse.chat.repository.ChatRoomRepository;
import com.ddhouse.chat.repository.UserRepository;
import com.ddhouse.chat.vo.ChatRoom;
import com.ddhouse.chat.vo.ChatRoomMessage;
import com.ddhouse.chat.vo.MessageType;
import com.ddhouse.chat.vo.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatRoomMessageService {
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    public ChatRoomMessage findById(Long chatRoomMessageId){
        return chatRoomMessageRepository.findById(chatRoomMessageId);
    }

    public ChatRoomMessage saveChatRoomMessage(SaveMessageDto saveMessageDto){
        // TODO : 비회원인 경우 userId -> null
        User user = null;
        if(saveMessageDto.getWriterId() != null) {
            user = userRepository.findById(saveMessageDto.getWriterId());
        }
        ChatRoom chatRoom = chatRoomRepository.findById(saveMessageDto.getRoomId());
        ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(saveMessageDto.getMsg(), user, chatRoom, MessageType.TEXT);
        return chatRoomMessageRepository.save(chatRoomMessage);
    }

    @Transactional
    public void deleteChatMessageOnlyMe(Long msgId, Long myId){
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findById(msgId);
        chatRoomMessage.addDeleteMssUser(myId); // 개인 기기에서 해당 메시지를 지운 유저 아이디 추가
        chatRoomMessageRepository.save(chatRoomMessage);
    }

    @Transactional
    public Long deleteChatMessageAll(Long msgId, Long myId){
        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findById(msgId);
        if (!chatRoomMessage.getUser().getId().equals(myId)) {
            throw new IllegalArgumentException("사용자가 작성한 메시지가 아니므로 지울 수 없습니다.");
        } else{
            chatRoomMessage.deleteMessageAll();
            chatRoomMessageRepository.save(chatRoomMessage);
            return chatRoomMessage.getChatRoom().getId();
        }
    }

}
