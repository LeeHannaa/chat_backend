package com.ddhouse.chat.service;


import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.dto.request.group.GroupChatRoomCreateDto;
import com.ddhouse.chat.dto.request.group.InviteGroupRequestDto;
import com.ddhouse.chat.dto.response.message.ChatMessageResponseToChatRoomDto;
import com.ddhouse.chat.dto.response.chatRoom.ChatRoomListResponseDto;
import com.ddhouse.chat.repository.*;
import com.ddhouse.chat.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatRoomMessageService chatRoomMessageService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MessageUnreadService messageUnreadService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final SimpMessageSendingOperations template;


    public UserChatRoom createChatRoom(ChatRoomDto chatRoomDto, ChatRoomDto myChatRoomDto) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(chatRoomDto));
        userChatRoomRepository.save(UserChatRoom.from(myChatRoomDto, chatRoom));
        return userChatRoomRepository.save(UserChatRoom.from(chatRoomDto, chatRoom));
    }

    public UserChatRoom createChatRoomByConnecting(Long userId, Long myId) {
        User other = userService.findByUserId(userId);
        User me = userService.findByUserId(myId);
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(ChatRoomDto.from()));
        userChatRoomRepository.save(UserChatRoom.person(ChatRoomDto.person(other), chatRoom));
        return userChatRoomRepository.save(UserChatRoom.from(ChatRoomDto.person(me), chatRoom));
    }

    public UserChatRoom createChatRoomByGuest(ChatRoomCreateDto chatRoomCreateDto) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(chatRoomCreateDto));
        return userChatRoomRepository.save(UserChatRoom.from(chatRoomCreateDto, chatRoom));
    }

    public ChatRoom createGroupChatRoom(GroupChatRoomCreateDto groupChatRoomCreateDto){
        // 채팅방 저장
        ChatRoom chatRoom = ChatRoom.group(groupChatRoomCreateDto);
        chatRoomRepository.save(chatRoom);

        List<UserChatRoom> userChatRooms = groupChatRoomCreateDto.getUserIds().stream()
                .map(userId -> UserChatRoom.group(chatRoom, userRepository.findByIdx(userId)))
                .collect(Collectors.toList());
        userChatRoomRepository.saveAll(userChatRooms);
        return chatRoom;
    }
    @Transactional
    public UserChatRoom inviteGroupChatRoom(InviteGroupRequestDto inviteGroupRequestDto){
        // 채팅방 유저 초대
        User user = userService.findByUserId(inviteGroupRequestDto.getUserId());
        ChatRoom chatRoom = findChatRoomByRoomId(inviteGroupRequestDto.getRoomId());
        boolean exists = userChatRoomRepository.existsByUserAndChatRoom(user, chatRoom);
        if (exists) {
            throw new IllegalStateException("해당 채팅방에 유저가 존재합니다.");
        } else{
            // 채팅방 인원 증가
            chatRoom.increaseMemberNum();
            chatRoomRepository.memberNumUpdate(chatRoom);
            String inviteMsg = user.getUserId() + "님이 초대되었습니다.";
            ChatRoomMessage beforeChatRoomMessage = chatRoomMessageService.findById(inviteGroupRequestDto.getMsgId());
            beforeChatRoomMessage.updateInvite(Boolean.TRUE);
            chatRoomMessageRepository.update(beforeChatRoomMessage);
                        ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(inviteMsg, user, chatRoom, MessageType.SYSTEM);
                        ChatMessageResponseToChatRoomDto chatMessageResponseToChatRoomDto = ChatMessageResponseToChatRoomDto.deleteInviteFrom(chatRoomMessage, inviteMsg, beforeChatRoomMessage.getIdx());
                        Map<String, Object> inviteUser = Map.of(
                                "type", "INVITE",
                                "message", chatMessageResponseToChatRoomDto
                                );
                        template.convertAndSend("/topic/chatroom/" + chatRoom.getIdx(), inviteUser);
        }
        return userChatRoomRepository.save(UserChatRoom.group(chatRoom, user));
    }

    public List<ChatRoomListResponseDto> findMyChatRoomList(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(myId);
        return userChatRooms.stream()
                .filter(UserChatRoom::getIsInRoom) // 나가기 하지 않은 채팅방만
                .map(UserChatRoom::getChatRoom)
                .filter(chatRoom ->
                    chatRoomMessageRepository.existsByChatRoomId(chatRoom.getIdx())) // 메시지가 존재하는 경우만 필터링
                .map(chatRoom -> {
//                    System.out.println("chatRoom 정보 : " + chatRoom.getId());
                    if(chatRoom.getIsGroup()){
                        // 단톡이었으면 -> 채팅방 이름으로
                        return ChatRoomListResponseDto.group(chatRoom);
                    } else if (chatRoom.getPhoneNumber() != null) {
                        // 비회원 문의 톡
                        return ChatRoomListResponseDto.one(chatRoom, chatRoom.getPhoneNumber());
                    } else{
                        // 개인톡 -> 상대방 이름으로
                        UserChatRoom opponent = userChatRoomRepository.findOpponent(myId, chatRoom.getIdx());
                        String chatName = "알 수 없음";

                        if (opponent != null && opponent.getUser() != null && "I".equals(opponent.getUser().getSts())) {
                            // I인 상태만 쿼리에서 가져오면 마지막 조건 제거 -> 일단 sql에도 넣음
                            chatName = opponent.getUser().getUserId();
                        }

                        return ChatRoomListResponseDto.one(chatRoom, chatName);
                    }
                })
                .collect(Collectors.toList());
    }

    public Tuple3<String, LocalDateTime, Long> getLastMessageWithUnreadCount(Long roomId, Long myId) {
        // entry_time 입장 시간 보고 라스트 채팅 가져오기
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId);
        LocalDateTime entryTime = userChatRoom.getEntryTime();

        List<ChatRoomMessage> chatRoomMessages = chatRoomMessageRepository.findRecentMessages(roomId, entryTime);
        Optional<ChatRoomMessage> lastMessageOpt = Optional.empty();
        for (ChatRoomMessage message : chatRoomMessages) {
            String deleteUsers = message.getDeleteUsers();
            // deleteUsers가 null이거나 비어있으면 바로 리턴 + 메시지 전체 삭제 아닌 경우
            // ** 전체 삭제일 경우 삭제메시지 없이 채팅 삭제 피드백 반영
            if ((deleteUsers == null || deleteUsers.isBlank()) && !message.getIsDelete()) {
                lastMessageOpt = Optional.of(message);
                break;
            }
            List<Long> deleteUserList = message.getDeleteUserList();
            // ** 메시지 전체 삭제 아닌 경우 마지막 메시지로 pick!
            if (deleteUserList!= null && !deleteUserList.contains(myId) && !message.getIsDelete()) {
                lastMessageOpt = Optional.of(message);
                break;
            }
        }
        ChatRoomMessage lastMessage = lastMessageOpt.orElse(null);
        if(lastMessage == null){
            // 전체를 빈값으로 전달, 날짜는 받아옴 (단톡아니면 메시지 없으면 리스트에서 안보임)
            return Tuples.of("채팅방에 초대되었습니다.", entryTime, 0L);
        }
        Tuple2<String, LocalDateTime> lastMessageTuple2 = Tuples.of(lastMessage.getMsg(), lastMessage.getCdate());


        Long unreadCount = messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString());
        System.out.println("안읽은 메시지 수 확인해보기 :" + unreadCount);
        return Tuples.of(
                lastMessageTuple2.getT1(),  // msg
                lastMessageTuple2.getT2(),  // createdDate
                unreadCount                 // unreadCount
        );
    }

    public ChatRoom findChatRoomByRoomId(Long roomId) {
        return chatRoomRepository.findByIdx(roomId);
    }

    public List<ChatRoom> findChatRoomsByPhoneNumber(String phoneNumber) {
        return chatRoomRepository.findByPhoneNumber(phoneNumber);
    }

    @Transactional
    public void increaseNumberInChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findByIdx(roomId);
        chatRoom.increaseMemberNum();
        chatRoomRepository.memberNumUpdate(chatRoom);
    }

    @Transactional
    public void deleteChatRoom(Long roomId, Long myId){
        // 채팅 이용자가 0명인 경우 전체 대화 삭제
        ChatRoom chatRoom = chatRoomRepository.findByIdx(roomId);
        if(chatRoom != null) {
                chatRoom.decreaseMemberNum(); // memberNum 감소
                if (chatRoom.getMemberNum() == 0) {
                    // roomId를 가진 UserChatRoom 데이터 전체 삭제 & ChatRoomMessage 데이터 전체 삭제
                    userChatRoomRepository.deleteByChatRoomId(roomId);
                    chatRoomMessageRepository.deleteByChatRoomId(roomId);
                    chatRoomRepository.deleteByIdx(roomId);
                } else {
                    UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId);
                    if (chatRoom.getIsGroup()) {
                        userChatRoomRepository.deleteById(userChatRoom.getIdx());
                        User user = userService.findByUserId(myId);
                        String deleteMsg = user.getUserId() + "님이 채팅방을 나갔습니다.";
                        ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(deleteMsg, user, chatRoom, MessageType.SYSTEM);
                        ChatMessageResponseToChatRoomDto chatMessageResponseToChatRoomDto = ChatMessageResponseToChatRoomDto.deleteFrom(chatRoomMessage, deleteMsg);
                        Map<String, Object> leaveUser = Map.of(
                                "type", "LEAVE",
                                "message", chatMessageResponseToChatRoomDto,
                                "msgToReadCount", messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString())
                        );
                        template.convertAndSend("/topic/chatroom/" + roomId, leaveUser);
                        System.out.println("채팅방에 유저가 나감 : " + deleteMsg);
                        messageUnreadService.removeUnread(roomId.toString(), myId.toString());
                        chatRoomMessageRepository.save(chatRoomMessage);
                    } else {
                        // 1대1 채팅방일 경우 -> UserChatRoom에 leaveTheChatRoom 실행 (isInRoom을 F로, entryTime 시간 업데이트)
                        userChatRoom.leaveTheChatRoom();
                        userChatRoomRepository.update(userChatRoom); // 변경사항 저장
                    }
                    chatRoomRepository.memberNumUpdate(chatRoom);
                }
        }
    }
}
