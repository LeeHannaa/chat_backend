package com.ddhouse.chat.repository;

import com.ddhouse.chat.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByAptIdIn(List<Long> aptIds);

}
