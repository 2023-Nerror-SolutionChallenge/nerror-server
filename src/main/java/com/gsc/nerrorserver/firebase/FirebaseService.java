package com.gsc.nerrorserver.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FirebaseService {

    public static final String COLLECTION_NAME = "Member";

    // 사용자 저장
    public void saveMember(Member member) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(member.getEmail()).set(member);
        log.info(member + " 저장이 완료되었습니다.");
    }

    // 사용자 조회 (by email)
    public Member findByEmail(String email) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = ref.get();
        DocumentSnapshot doc = future.get();

        if (doc.exists()) return doc.toObject(Member.class);
        else return null;
    }

    public boolean existsMemberByEmail(String email) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = ref.get();
        DocumentSnapshot doc = future.get();
        return doc.exists();
    }

    public boolean existsMemberByNickname(String nickname) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Query ref = db.collection(COLLECTION_NAME).whereEqualTo("nickname", nickname);
        ApiFuture<QuerySnapshot> future = ref.get();
        QuerySnapshot doc = future.get();
        return !doc.isEmpty();
    }

}
