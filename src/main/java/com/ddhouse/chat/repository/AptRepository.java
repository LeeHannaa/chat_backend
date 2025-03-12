package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.Apt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AptRepository extends JpaRepository<Apt, Long> {
}
