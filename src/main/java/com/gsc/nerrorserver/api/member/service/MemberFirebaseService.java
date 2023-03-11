package com.gsc.nerrorserver.api.member.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberFirebaseService {

    public static final String COLLECTION_MEMBER = "Member";
    public static final String COLLECTION_ATTENDANCE = "Attendance";

    /* 사용자 저장 */
    public void saveMember(Member member) throws Exception {

        Firestore db = FirestoreClient.getFirestore();

        // 사용자 저장
        DocumentReference memberDoc = db.collection(COLLECTION_MEMBER).document(member.getId());
        // 회원가입 날짜 추가
        String saveDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        member.getAttendance().add(saveDate);

        ApiFuture<WriteResult> future = memberDoc.set(member);

        log.info(member + " 저장이 완료되었습니다.");
    }

    /* 사용자 조회 (by email) */
    public Member findById(String email) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_MEMBER).document(email);
        ApiFuture<DocumentSnapshot> future = ref.get();
        DocumentSnapshot doc = future.get();

        if (doc.exists()) return doc.toObject(Member.class);
        else return null;
    }

    /* 중복검사 (by Id) */
    public boolean existsMemberById(String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_MEMBER).document(id);
        ApiFuture<DocumentSnapshot> future = ref.get();
        DocumentSnapshot doc = future.get();
        return doc.exists();
    }

    // 중복검사 (by nickname)
    public boolean existsMemberByNickname(String nickname) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Query ref = db.collection(COLLECTION_MEMBER).whereEqualTo("nickname", nickname);
        ApiFuture<QuerySnapshot> future = ref.get();
        QuerySnapshot doc = future.get();
        return !doc.isEmpty();
    }

    /* 로그인시 출석부 찍기 */
    public void addAttendance(String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        // TODO : 1일 1회만 출석부에 기록되어야 함 -> 문서 제목을 yyyy-mm-dd 포맷해서 1일 1회만 기록되게 하자
        String attendDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        DocumentReference doc = db.collection(COLLECTION_MEMBER).document(id);
        Member member = doc.get().get().toObject(Member.class);

        List<String> attendance = member.getAttendance();
        if (! attendance.contains(attendDate)) {
            attendance.add(attendDate);
            ApiFuture<WriteResult> future = doc.set(member);
        } else log.info("오늘은 이미 출석을 하셨어잉");
    }
}
