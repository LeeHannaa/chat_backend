package com.ddhouse.chat.service;

import com.ddhouse.chat.domain.*;
import com.ddhouse.chat.dto.info.ChatRoomForAptDto;
import com.ddhouse.chat.dto.request.ChatMessageRequestDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseCreateDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseDto;
import com.ddhouse.chat.dto.info.ChatRoomDto;
import com.ddhouse.chat.dto.response.ChatMessage.ChatMessageResponseToFindMsgDto;
import com.ddhouse.chat.exception.NotFlowException;
import com.ddhouse.chat.exception.NotFoundException;
import com.ddhouse.chat.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final AptRepository aptRepository;
    private final ChatService chatService;
    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final ChatRoomMessageService chatRoomMessageService;
    private final MessageUnreadService messageUnreadService;
    public int getRoomMemberNum(Long roomId){
        return chatRoomRepository.findById(roomId).get().getMemberNum();
    }

    public Mono<List<ChatMessageResponseDto>> findChatMessages(Long roomId, Long myId) {
        /*
        1. 해당 방에 메시지 내역이 잇는지 확인
            1-1. ChatRoomMessage에서 해당 roomId가 존재하는 지 확인
                1-1-1. 있다면 2번으로 이동
                1-1-2. 없다면 방의 정보를 담아서 넘기기
        2. 메시지가 있다면 해당 roomId에 해당하는 모든 ChatRoomMessage 리스트 반환
        ** entryTime을 기준으로 이후의 메시지만 반환 **
            2-1. 각각의 messageId를 가지고 msg를 받아와서 dto에 저장
            2-2. ChatRoomMessage에 createTime을 기준으로 정렬 후 넘기기
        */
        List<ChatRoomMessage> chatRoomMessages = chatRoomMessageRepository.findAllByChatRoomId(roomId);
        if(chatRoomMessages.isEmpty()){
            System.out.println("채팅 내역이 없는 경우 (채팅방만 존재) -> 채팅방의 정보만 보내기! ");
            ChatMessageResponseCreateDto chatMessageResponseCreateDto = ChatMessageResponseCreateDto.create(
                    userChatRoomRepository.findByChatRoomId(roomId).orElseThrow(() -> new NotFoundException("해당 채팅방의 정보를 찾을 수 없습니다."))
            );
            return Mono.just(Collections.singletonList(chatMessageResponseCreateDto));
        }
        // UserChatRoom에서 해당 userId를 통해 entryTime을 가져오기
        // entryTime을 가지고 ChatRoomMessage에서 regDate가 해당 entryTime 이후의 내용들만 메시지를 담아서 보내기
        LocalDateTime standardTime = userChatRoomRepository
                .findByUserIdAndChatRoomId(myId, roomId)
                .orElseThrow(() -> new NotFoundException("채팅방 정보가 없습니다."))
                .getEntryTime();

        Mono<List<ChatMessageResponseDto>> chatRoomMessagesMono = Flux.fromIterable(chatRoomMessages)
                .filter(chatRoomMessage -> chatRoomMessage.getRegDate().isAfter(standardTime)) // standardTime 이후만
                // 내 기기에서 삭제된 메시지는 제외하긴 하는데 그 경우 userId가 나의 id와 동일한 경우에만 제외
                .filter(chatRoomMessage -> !chatRoomMessage.getDeleteUserList().contains(myId))
                .flatMap(chatRoomMessage -> {
                    UUID msgId = chatRoomMessage.getMessageId();
                    return chatMessageRepository.findById(msgId)
                            .flatMap(chatMessage -> {
                                if(chatRoomMessage.getType() == MessageType.TEXT){
                                    // TODO G **: 각 메시지마다 읽지 않은 유저의 수를 함께 전달
                                    int unreadCountByMsgId = messageUnreadService.getUnreadCountByMsgId(chatRoomMessage.getChatRoom().getId().toString(), msgId.toString());
                                    if (chatRoomMessage.getIsDelete()) {
                                        // 전체 삭제된 메시지 처리
                                        return Mono.just(ChatMessageResponseToFindMsgDto.fromAllDelete(chatMessage, chatRoomMessage, unreadCountByMsgId));
                                    } else {
                                        return Mono.just(ChatMessageResponseToFindMsgDto.from(chatMessage, chatRoomMessage, unreadCountByMsgId));
                                    }
                                }
                                // SYSTEM 타입의 메시지일 경우
                                return Mono.just(ChatMessageResponseToFindMsgDto.deleteFrom(chatMessage, chatRoomMessage));
                            });
                })
                .collectList()
                .map(chatMessages -> {
                    // 오래된 날짜 순서대로
                    chatMessages.sort(Comparator.comparing(ChatMessageResponseToFindMsgDto::getCreatedDate));
                    List<ChatMessageResponseDto> result = chatMessages.stream()
                            .map(msg -> (ChatMessageResponseDto) msg)  // 상속 관계를 이용한 형 변환
                            .collect(Collectors.toList());
                    return result;
            });
        return chatRoomMessagesMono;
    }


    public Mono<List<ChatMessageResponseDto>> getChatRoomByAptIdAndUserId(Long aptId, Long myId) {
        // 1. 기존에 채팅하던 방이 있는 경우
        // 1-1. 내 아이디로 나의 채팅방 불러오기
        List<ChatRoomForAptDto> chatRooms = chatService.findMyChatRoomListForApt(myId);
        // 1-2. chatRooms에서 aptId랑 파라미터 aptId랑 비교해서 동일한 데이터가 있으면 채팅방이 있는 경우!
        if (!chatRooms.isEmpty()) {
            for (ChatRoomForAptDto chatRoomForAptDto : chatRooms) {
                if (chatRoomForAptDto.getApt() != null && chatRoomForAptDto.getApt().getId().equals(aptId)) {
                    return findChatMessages(chatRoomForAptDto.getRoomId(), myId);
                }
            }
        }

        // 2. 방을 생성해야하는 경우
        Optional<Apt> aptOptional = aptRepository.findById(aptId);
        if (aptOptional.isPresent()) {
            Apt apt = aptOptional.get();
            if (apt.getUser().getId().equals(myId)) {
                // 내가 올린 매물 내가 문의하기 누른 경우
                return Mono.error(new NotFlowException("비정상 플로우 : 내가 올린 매물 내가 문의하게 된 경우"));
            } else {
                // 새로운 방을 생성해야하는 경우 (1:1)
                System.out.println("새로운 방 생성");
                User user = userRepository.findById(myId).orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
                UserChatRoom createdChatRoom = chatService.createChatRoom(ChatRoomDto.createChatRoomDto(apt, user));
                ChatMessageResponseCreateDto newRoomInfo = ChatMessageResponseCreateDto.create(createdChatRoom);
                return Mono.just(Collections.singletonList(newRoomInfo));
            }
        }
        // aptId가 존재하지 않는 경우
        return Mono.error(new NotFoundException("해당 매물 정보를 찾을 수 없습니다."));
    }

    public Mono<ChatMessage> saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        return chatMessageRepository.save(ChatMessage.from(chatMessageRequestDto.getMsg()))
                .flatMap(savedMessage -> {
                    UUID msgId = savedMessage.getId();
                    return chatRoomMessageService.saveChatRoomMessage(chatMessageRequestDto, msgId)
                            .thenReturn(savedMessage);
                });
    }


    public List<Long> findReceiverId(ChatMessageRequestDto chatMessageRequestDto){ // 소켓 통신할 때 수신자 id 찾기
        // TODO G **: 채팅방에 있는 모든 userId를 담은 List를 반환
        /*
        1. 채팅방 id가 같은 userChatRoom을 다 가지고 오기
        2. 가져온 데이터를 확인하면서 writerId랑 다른 id가 receiverId.
        3. 찾은 receiverId를 반환하기 -> 단체 채팅으로 넘어간다면 리스트로 반환? 음 이건 고민해보기
        */
        return userChatRoomRepository.findAllByChatRoomId(chatMessageRequestDto.getRoomId())
                .stream()
                .map(userChatRoom -> userChatRoom.getUser().getId())
                .filter(userId -> !userId.equals(chatMessageRequestDto.getWriterId()))
                .collect(Collectors.toList());
    }

    public UserChatRoom getUserInChatRoom(Long userId, Long roomId){
        return userChatRoomRepository.findByUserIdAndChatRoomId(userId, roomId)
                .orElseThrow(() -> new NotFoundException("채팅방에 해당 유저가 존재하지 않습니다."));
    }

    public void saveReEntryUserInChatRoom(UserChatRoom userChatRoom){
        userChatRoom.reEntryInChatRoom();
        userChatRoomRepository.save(userChatRoom);
    }
}
