package com.ssafy.cozytrain.api.repository;

import com.ssafy.cozytrain.api.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckListRepository extends JpaRepository<CheckList, Long> {
}
