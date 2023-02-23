package com.gsc.nerrorserver.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String password;

    private String nickname;

    @Column(name = "deleted_count")
    private Long deletedCount;

    @Column(name = "current_level")
    private Long currentLevel;

    @Column(name = "total_count")
    private Long totalCount;

}
