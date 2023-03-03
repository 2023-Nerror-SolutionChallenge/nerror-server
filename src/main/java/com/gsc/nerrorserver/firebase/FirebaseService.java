package com.gsc.nerrorserver.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.entity.MailData;
import com.gsc.nerrorserver.api.entity.Member;
import com.gsc.nerrorserver.api.service.MailService;
import com.gsc.nerrorserver.api.service.dto.MailReceiveDto;
import com.gsc.nerrorserver.api.service.dto.MailSaveDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {

    public static final String COLLECTION_NAME = "Member";
    private final MailService mailService;

    // 사용자 저장
    public void saveMember(Member member) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME).document(member.getId()).set(member);
        log.info(member + " 저장이 완료되었습니다.");
    }

    // 사용자 조회 (by email)
    public Member findById(String email) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = ref.get();
        DocumentSnapshot doc = future.get();

        if (doc.exists()) return doc.toObject(Member.class);
        else return null;
    }

    public boolean existsMemberById(String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_NAME).document(id);
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

    public void addAccount(MailReceiveDto dto) throws Exception {
        // member - id로 검색, accountList 내에 username과 데이터 저장
        Firestore db = FirestoreClient.getFirestore();
        WriteBatch batch = db.batch();

        // TODO: 계정 중복 추가 방지, 트랜잭션 2회 저장
        DocumentReference ref = db.collection(COLLECTION_NAME).document(dto.getId()).collection("AccountList").document(dto.getUsername());
        batch.set(ref, dto);

        // 메일 총 갯수 저장
        // TODO - 클라이언트 응답으로 메일 총 갯수 먼저 보내놓고, Fetching 작업은 비동기 처리?
        Map<String, Object> count = new HashMap<>();
        count.put("count", mailService.readInboundMailCount(dto));
        batch.update(ref, count);

        // 트랜잭션 커밋
        ApiFuture<List<WriteResult>> future = batch.commit();
        log.info("계정 추가가 완료되었습니다.");
    }

    public void saveMailData(MailReceiveDto dto) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference ref = db.collection("Member").document(dto.getId())
                .collection("AccountList").document(dto.getUsername()).collection("MailList");
        Collection<MailData> mailData = mailService.readInboundMails(dto);
        for (MailData data : mailData) {
            long count = ref.count().get().get().getCount(); // DB 저장 메시지 수
            List<QueryDocumentSnapshot> documents = ref.whereEqualTo("receivedDate", data.getReceivedDate())
                    .whereEqualTo("from", data.getFrom())
                    .get().get().getDocuments();
            if (documents.isEmpty()) {
                ApiFuture<DocumentReference> future = ref.add(data);
            } else log.warn("이미 존재하는 메일이므로 저장하지 않습니다. 제목 : {}", data.getSubject());
        }
    }
}

