package com.ddhouse.chat.service;


import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.info.ChatRoomDto;
import com.ddhouse.chat.dto.info.ChatRoomForAptDto;
import com.ddhouse.chat.dto.request.GroupChatRoomCreateDto;
import com.ddhouse.chat.dto.request.InviteGroupRequestDto;
import com.ddhouse.chat.dto.response.ChatRoomInfoResponseDto;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MessageUnreadService messageUnreadService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final SimpMessageSendingOperations template;


    public UserChatRoom createChatRoom(ChatRoomDto chatRoomDto) {
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(chatRoomDto));
        userChatRoomRepository.save(UserChatRoom.otherFrom(chatRoomDto, chatRoom));
        return userChatRoomRepository.save(UserChatRoom.from(chatRoomDto, chatRoom));
    }

    public ChatRoom createGroupChatRoom(GroupChatRoomCreateDto groupChatRoomCreateDto){
        // 채팅방 저장
        ChatRoom chatRoom = ChatRoom.group(groupChatRoomCreateDto.getChatRoomName(), groupChatRoomCreateDto.getUserIds().size());
        chatRoomRepository.save(chatRoom);

        List<UserChatRoom> userChatRooms = groupChatRoomCreateDto.getUserIds().stream()
                .map(userId -> UserChatRoom.group(chatRoom, userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("해당 아이디를 가진 유저를 찾지 못했습니다."))))
                .collect(Collectors.toList());
        userChatRoomRepository.saveAll(userChatRooms);
        return chatRoom;
    }
    public UserChatRoom inviteGroupChatRoom(InviteGroupRequestDto inviteGroupRequestDto){
        // TODO G : 채팅방에 유저 초대하면 누가 누구를 초대했는지 시스템타입으로 메시지 저장
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
        }

        return userChatRoomRepository.save(UserChatRoom.group(chatRoom, user));
    }


    // 채팅 전체 리스트
//    public List<ChatRoomDto> findChatRoomList() {
//        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
//        return chatRooms.stream().map(ChatRoomDto::from).collect(Collectors.toList());
//    }

    public List<ChatRoomInfoResponseDto> findMyChatRoomList(Long myId) {
        // 내가 문의자로 들어간 채팅방 or 내가 관리자로 있는 채팅방
        List<UserChatRoom> chatRooms = userChatRoomRepository.findByUserId(myId);
        return chatRooms.stream()
                .filter(UserChatRoom::getIsInRoom) // 나가기 하지 않은 채팅방만
                .map(UserChatRoom::getChatRoom)
                .filter(chatRoom -> chatRoomMessageRepository.existsByChatRoomId(chatRoom.getId())) // 메시지가 존재하는 경우만 필터링
                .map(ChatRoomInfoResponseDto::create)
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
        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId)
                .orElseThrow(() -> new NotFoundException("채팅방에서 해당 유저의 정보를 찾을 수 없습니다."));
        LocalDateTime entryTime = userChatRoom.getEntryTime();

        // chatRoomMessage에서 해당 roomId에서 createTime을 보고 가장 최신의 (+ isDelete가 ME -> MeswsageDeletePersonal에서 정보 확인) messageId를 가져와서 ChatMessage 테이블에서 msg 가져오기
        // chatRoomMessage에서 마지막 메시지의 isDelete가 ALL이면 "삭제된 메시지 입니다."로 설정
        List<ChatRoomMessage> chatRoomMessages = chatRoomMessageRepository.findTop100ByChatRoomIdAndRegDateAfterOrderByRegDateDesc(roomId, entryTime);
        Optional<ChatRoomMessage> lastMessageOpt = Optional.empty();
        for (ChatRoomMessage message : chatRoomMessages) {
            String deleteUsers = message.getDeleteUsers();

            // deleteUsers가 null이거나 비어있으면 바로 리턴
            if (deleteUsers == null || deleteUsers.isBlank()) {
                lastMessageOpt = Optional.of(message);
                break;
            }

            List<Long> deleteUserList = message.getDeleteUserList();
            if (deleteUserList!= null && !deleteUserList.contains(myId)) {
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
                .map(chatMessage -> {
                    String msgContent = lastMessage.getIsDelete()
                            ? "삭제된 메시지입니다."
                            : chatMessage.getMsg();
                    return Tuples.of(msgContent, lastMessage.getRegDate());
                });

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
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("해당 채팅방 정보를 찾을 수 없습니다."));
    }
    @Transactional
    public void increaseNumberInChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new NotFoundException("해당 채팅방 정보를 찾을 수 없습니다."));
        chatRoom.increaseMemberNum();
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public void deleteChatRoom(Long roomId, Long myId){
        /*
        [ 채팅방 삭제 로직 ]
        1. roomId를 가지고 ChatRoom에 memberNum 감소
            1-1. memberNum이 0인 경우 해당 roomId를 가진 UserChatRoom 데이터 전체 삭제 & ChatRoomMessage 데이터 전체 삭제 (카산드라는 삭제할지 말지 나중에 결정)
            1-2. memberNum이 0이 아닌 경우 : UserChatRoom에 leaveTheChatRoom 실행 (isInRoom을 F로, entryTime 시간 업데이트)
        */
        // 채팅 이용자가 0명인 경우 전체 대화 삭제
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(roomId);
        chatRoomOptional.ifPresent(chatRoom -> {
            chatRoom.decreaseMemberNum(); // memberNum 감소
            if (chatRoom.getMemberNum() == 0) {
                // roomId를 가진 UserChatRoom 데이터 전체 삭제 & ChatRoomMessage 데이터 전체 삭제
                userChatRoomRepository.deleteByChatRoomId(roomId);
                chatRoomMessageRepository.deleteByChatRoomId(roomId);
                chatRoomRepository.deleteById(roomId);
            }
            else {
                UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(myId, roomId)
                        .orElseThrow(() -> new NotFoundException("해당 채팅방에 존재하는 유저의 정보를 알 수 없습니다."));
                // TODO G **: 단체 채팅방인 경우 isInRoom을 False로 하는게 아니라 그냥 해당 user 아웃
                if(chatRoom.getIsGroup()){
                    userChatRoomRepository.deleteById(userChatRoom.getId());
                    // TODO G **: 실시간으로 해당 유저가 방을 나갔음을 알림
                    // TODO G : 시스템 타입으로 해당 유저가 방을 나갔다는 메시지를 저장
                    Map<String, Object> leaveUser = Map.of(
                            "type", "LEAVE",
                            "leaveUserId", myId,
                            "leaveUserName", userChatRoom.getUser().getName(),
                            "msgToReadCount", messageUnreadService.getUnreadMessageCount(roomId.toString(), myId.toString())
                    );
                    // TODO G **: 채팅방 유저가 나갈 경우 유저가 안읽은 메시지가 있으면 다 읽음처리
                    messageUnreadService.removeUnread(roomId.toString(), myId.toString());
                    template.convertAndSend("/topic/chatroom/" + roomId, leaveUser);
                }else{
                    // UserChatRoom에 leaveTheChatRoom 실행 (isInRoom을 F로, entryTime 시간 업데이트)
                    userChatRoom.leaveTheChatRoom();
                    userChatRoomRepository.save(userChatRoom); // 변경사항 저장
                }
            }
        });
    }
}
