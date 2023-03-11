package com.gsc.nerrorserver.api.member.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberFirebaseService {

    public static final String COLLECTION_MEMBER = "Member";
    public static final String COLLECTION_ACCOUNT_LIST = "AccountList";
    public static final String COLLECTION_MAIL_LIST = "MailList";

    /* 사용자 저장 */
    public void saveMember(Member member) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_MEMBER).document(member.getId()).set(member);
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
}
