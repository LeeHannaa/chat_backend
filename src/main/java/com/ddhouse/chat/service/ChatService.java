package com.ddhouse.chat.service;


import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.ChatRoomCreateDto;
import com.ddhouse.chat.dto.ChatRoomDto;
import com.ddhouse.chat.dto.ChatRoomForAptDto;
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
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
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
        ChatRoom chatRoom = ChatRoom.group(groupChatRoomCreateDto.getChatRoomName(), groupChatRoomCreateDto.getUserIds().size());
        chatRoomRepository.save(chatRoom);

        List<UserChatRoom> userChatRooms = groupChatRoomCreateDto.getUserIds().stream()
                .map(userId -> UserChatRoom.group(chatRoom, userRepository.findById(userId)))
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
            chatRoomRepository.save(chatRoom);
            String inviteMsg = user.getName() + "님이 초대되었습니다.";
            // TODO G : 채팅방을 나갔다는 메시지의 id를 받아와서 해당 메시지의 isDelete를 true로 저장
            ChatRoomMessage beforeChatRoomMessage = chatRoomMessageService.findByMessageId(inviteGroupRequestDto.getMsgId());
            beforeChatRoomMessage.updateInvite(Boolean.TRUE);
            chatRoomMessageRepository.save(beforeChatRoomMessage);
            // TODO G : 채팅방에 유저 초대하면 누가 누구를 초대했는지 시스템타입으로 메시지 저장
            chatMessageRepository.save(ChatMessage.from(inviteMsg))
                    .flatMap(savedMessage -> {
                        ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(savedMessage.getId(), user, chatRoom, MessageType.SYSTEM);
                        ChatMessageResponseToChatRoomDto chatMessageResponseToChatRoomDto = ChatMessageResponseToChatRoomDto.deleteInviteFrom(chatRoomMessage, inviteMsg, beforeChatRoomMessage.getMessageId());
                        Map<String, Object> inviteUser = Map.of(
                                "type", "INVITE",
                                "message", chatMessageResponseToChatRoomDto
                                );
                        // TODO G **: 실시간으로 해당 유저가 방에 다시 들어왔음을 알림
                        template.convertAndSend("/topic/chatroom/" + chatRoom.getId(), inviteUser);
                        return Mono.fromCallable(() -> chatRoomMessageRepository.save(chatRoomMessage));
                    })
                    .subscribe();
        }
        return userChatRoomRepository.save(UserChatRoom.group(chatRoom, user));
    }

    public List<ChatRoomListResponseDto> findMyChatRoomList(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(myId);
        return chatRooms.stream()
                .filter(UserChatRoom::getIsInRoom) // 나가기 하지 않은 채팅방만
                .map(UserChatRoom::getChatRoom)
                .filter(chatRoom -> chatRoomMessageRepository.existsByChatRoomId(chatRoom.getId())) // 메시지가 존재하는 경우만 필터링
                .map(chatRoom -> {
                    if(chatRoom.getIsGroup()){
                        // 단톡이었으면 -> 채팅방 이름으로
                        return ChatRoomListResponseDto.group(chatRoom);
                    } else if (chatRoom.getPhoneNumber() != null) {
                        // 비회원 문의 톡
                        return ChatRoomListResponseDto.one(chatRoom, chatRoom.getPhoneNumber());
                    } else{
                        // 개인톡 -> 상대방 이름으로
                        Optional<UserChatRoom> opponent = userChatRoomRepository.findOpponent(myId, chatRoom.getId());

                        String chatName = opponent.stream()
                                .map(UserChatRoom::getUser)
                                .map(User::getName)
                                .findFirst()
                                .orElse("알 수 없음");

                        return ChatRoomListResponseDto.one(chatRoom, chatName);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<ChatRoomForAptDto> findMyChatRoomListForApt(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(myId);
        return chatRooms.stream()
                .filter(UserChatRoom::getIsInRoom) // 나가기 하지 않은 채팅방만
                .map(UserChatRoom::getChatRoom)
                .filter(chatRoom -> chatRoomMessageRepository.existsByChatRoomId(chatRoom.getId())) // 메시지가 존재하는 경우만 필터링
                .map(ChatRoomForAptDto::from)
                .collect(Collectors.toList());
    }


    public Mono<Tuple3<String, LocalDateTime, Long>> getLastMessageWithUnreadCount(Long roomId, Long myId) {
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
            return Mono.just(Tuples.of("채팅방에 초대되었습니다.", entryTime, 0L));
        }
        Mono<Tuple2<String, LocalDateTime>> lastMessageMono = chatMessageRepository.findById(lastMessage.getMessageId())
                .map(chatMessage -> Tuples.of(chatMessage.getMsg(), lastMessage.getRegDate()));


        Long unreadCount = messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString());
        System.out.println("안읽은 메시지 수 확인해보기 :" + unreadCount);
        Mono<Long> unreadCountMono = Mono.just(unreadCount);

        return Mono.zip(lastMessageMono, unreadCountMono)
                .map(tuple -> Tuples.of(
                        tuple.getT1().getT1(), // msg
                        tuple.getT1().getT2(), // createdDate
                        tuple.getT2()          // unreadCount
                ));
    }

    public ChatRoom findChatRoomByRoomId(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }

    public List<ChatRoom> findChatRoomsByPhoneNumber(String phoneNumber) {
        return chatRoomRepository.findByPhoneNumber(phoneNumber);
    }

    @Transactional
    public void increaseNumberInChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId);
        chatRoom.increaseMemberNum();
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void deleteChatRoom(Long roomId, Long myId){
        // 채팅 이용자가 0명인 경우 전체 대화 삭제
        ChatRoom chatRoom = chatRoomRepository.findById(roomId);
        if(chatRoom != null) {
                chatRoom.decreaseMemberNum(); // memberNum 감소
                if (chatRoom.getMemberNum() == 0) {
                    // roomId를 가진 UserChatRoom 데이터 전체 삭제 & ChatRoomMessage 데이터 전체 삭제
                    userChatRoomRepository.deleteByChatRoomId(roomId);
                    chatRoomMessageRepository.deleteByChatRoomId(roomId);
                    chatRoomRepository.deleteById(roomId);
                } else {
                    UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId);
                    if (chatRoom.getIsGroup()) {
                        userChatRoomRepository.deleteById(userChatRoom.getId());
                        User user = userService.findByUserId(myId);
                        String deleteMsg = user.getName() + "님이 채팅방을 나갔습니다.";
                        AtomicReference<UUID> msgId = new AtomicReference<>();
                        chatMessageRepository.save(ChatMessage.from(deleteMsg))
                                .flatMap(savedMessage -> {
                                    msgId.set(savedMessage.getId());
                                    ChatRoomMessage chatRoomMessage = ChatRoomMessage.save(msgId.get(), user, chatRoom, MessageType.SYSTEM);
                                    ChatMessageResponseToChatRoomDto chatMessageResponseToChatRoomDto = ChatMessageResponseToChatRoomDto.deleteFrom(chatRoomMessage, deleteMsg);
                                    Map<String, Object> leaveUser = Map.of(
                                            "type", "LEAVE",
                                            "message", chatMessageResponseToChatRoomDto,
                                            "msgToReadCount", messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString())
                                    );
                                    // TODO G **: 실시간으로 해당 유저가 방을 나갔음을 알림
                                    template.convertAndSend("/topic/chatroom/" + roomId, leaveUser);
                                    System.out.println("채팅방에 유저가 나감 : " + deleteMsg);
                                    messageUnreadService.removeUnread(roomId.toString(), myId.toString());
                                    return Mono.fromCallable(() -> chatRoomMessageRepository.save(chatRoomMessage));
                                })
                                .subscribe();
                    } else {
                        // 1대1 채팅방일 경우 -> UserChatRoom에 leaveTheChatRoom 실행 (isInRoom을 F로, entryTime 시간 업데이트)
                        userChatRoom.leaveTheChatRoom();
                        userChatRoomRepository.save(userChatRoom); // 변경사항 저장
                    }
                }
        }
    }
}
