package com.ddhouse.chat.domain;

import com.ddhouse.chat.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Apt extends BaseEntity {
    @Id
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false) // 매물 등록자
    private User user;
}
