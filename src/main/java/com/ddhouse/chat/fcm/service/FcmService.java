package com.ddhouse.chat.fcm.service;

import com.ddhouse.chat.dto.response.FcmDto;
import com.ddhouse.chat.fcm.dto.FcmMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FcmService {
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/" +
            "ddhouse-chat/messages:send";
    private final ObjectMapper objectMapper;

    public void sendMessageTo(FcmDto fcmDto) throws IOException {
        String message = makeMessage(fcmDto);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }

    private String makeMessage(FcmDto fcmDto) throws JsonParseException, JsonProcessingException {
        Map<String, String> data = new HashMap<>();
        if(fcmDto.getTitle() == "새 메시지 도착!"){ // 채팅
            data.put("roomId", fcmDto.getRoomId());
            data.put("roomName", fcmDto.getRoomName());

            FcmMessage fcmMessage = FcmMessage.builder()
                    .message(FcmMessage.Message.builder()
                            .token(fcmDto.getTargetToken())
                            .notification(FcmMessage.Notification.builder()
                                    .title(fcmDto.getTitle())
                                    .body(fcmDto.getBody())
                                    .build())
                            .data(data)
                            .build())
                    .validateOnly(false)
                    .build();
            return objectMapper.writeValueAsString(fcmMessage);
        } else{ // 문의 쪽지
            FcmMessage fcmMessage = FcmMessage.builder()
                    .message(FcmMessage.Message.builder()
                            .token(fcmDto.getTargetToken())
                            .notification(FcmMessage.Notification.builder()
                                    .title(fcmDto.getTitle())
                                    .body(fcmDto.getBody())
                                    .build())
                            .build())
                    .validateOnly(false)
                    .build();
            return objectMapper.writeValueAsString(fcmMessage);
        }
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "ddhouse-chat-firebase-adminsdk-fbsvc-68c0f806ff.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
