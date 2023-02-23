package com.gsc.nerrorserver.api.repository;

import com.gsc.nerrorserver.api.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
