package com.gsc.nerrorserver.api.mail.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.gsc.nerrorserver.api.mail.dto.MailReceiveDto;
import com.gsc.nerrorserver.api.mail.entity.MailData;
import com.gsc.nerrorserver.api.member.entity.Badge;
import com.gsc.nerrorserver.api.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailFirebaseService {

    private final String COLLECTION_MEMBER = "Member";
    private final String COLLECTION_ACCOUNT_LIST = "AccountList";
    private final String COLLECTION_MAIL_LIST = "MailList";
    private final MailService mailService;

    @Transactional
    /* 메일 계정 추가 */
    public void addMailbox(MailReceiveDto dto) throws Exception {
        // member - id로 검색, accountList 내에 username과 데이터 저장
        Firestore db = FirestoreClient.getFirestore();
        WriteBatch batch = db.batch();

        DocumentReference refOfMailbox = db.collection(COLLECTION_MEMBER).document(dto.getId()).collection(COLLECTION_ACCOUNT_LIST).document(dto.getUsername());
        batch.set(refOfMailbox, dto);

        // 계정별 메일 갯수 저장
        // TODO - 클라이언트 응답으로 메일 총 갯수 먼저 보내놓고, Fetching 작업은 비동기 처리?
        Map<String, Object> count = new HashMap<>();
        count.put("count", mailService.readMailCount(dto));
        batch.update(refOfMailbox, count);

        // 총 메일 갯수 업데이트
        int totalCount = 0;
        CollectionReference cRef = db.collection(COLLECTION_MEMBER).document(dto.getId()).collection(COLLECTION_ACCOUNT_LIST);
        for (DocumentReference doc : cRef.listDocuments()) {
            totalCount += Integer.parseInt((String.valueOf(doc.get().get().get("count"))));
        }

        DocumentReference refOfTotalCount = db.collection(COLLECTION_MEMBER).document(dto.getId());
        batch.update(refOfTotalCount, "totalCount", totalCount);

        // 트랜잭션 커밋
        ApiFuture<List<WriteResult>> future = batch.commit();
        log.info("계정 추가가 완료되었습니다.");
    }

    @Transactional
    /* 전체 메일 Fetch 후 DB 저장 */
    public void saveMailData(MailReceiveDto dto) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference ref = db.collection(COLLECTION_MEMBER).document(dto.getId())
                .collection(COLLECTION_ACCOUNT_LIST).document(dto.getUsername()).collection(COLLECTION_MAIL_LIST);
        Collection<MailData> mailData = mailService.readAllMails(dto);
        for (MailData data : mailData) {
            List<QueryDocumentSnapshot> documents = ref.whereEqualTo("receivedDate", data.getReceivedDate())
                    .whereEqualTo("from", data.getFrom())
                    .get().get().getDocuments();
            if (documents.isEmpty()) {
                ApiFuture<DocumentReference> future = ref.add(data);
            } else log.warn("이미 존재하는 메일이므로 저장하지 않습니다. 제목 : {}", data.getSubject());
        }
    }

    @Transactional
    /* (새로고침) 메일 총 개수 업데이트 */
    public void updateCounts(String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        WriteBatch batch = db.batch();

        int totalCount = 0;
        CollectionReference cRef = db.collection(COLLECTION_MEMBER).document(id).collection(COLLECTION_ACCOUNT_LIST);
        for (DocumentReference doc : cRef.listDocuments()) {
            MailReceiveDto dto = doc.get().get().toObject(MailReceiveDto.class);
            int count = mailService.readMailCount(dto);
            batch.update(doc, "count", count);
            totalCount += count;
        }

        DocumentReference refOfTotalCount = db.collection(COLLECTION_MEMBER).document(id);
        batch.update(refOfTotalCount, "totalCount", totalCount);

        ApiFuture<List<WriteResult>> result = batch.commit();
    }

    @Transactional
    /* (새로고침) 최신 메일 Fetch 후 DB 저장 */
    public void saveRecentMailData(MailReceiveDto dto) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference ref = db.collection(COLLECTION_MEMBER).document(dto.getId())
                .collection(COLLECTION_ACCOUNT_LIST).document(dto.getUsername()).collection(COLLECTION_MAIL_LIST);

        // DB 저장된 메일 중 receivedDate 기준으로 가장 최신값 쿼리해서, searchTerm으로 넘겨주고 inbox에서 필터링하면 되나?
        Query q = db.collection(COLLECTION_MEMBER).document(dto.getId())
                .collection(COLLECTION_ACCOUNT_LIST).document(dto.getUsername()).collection(COLLECTION_MAIL_LIST).orderBy("receivedDate", Query.Direction.DESCENDING).limit(1);
        try {
            Date lastUpdatedDate = q.get().get().getDocuments().get(0).getDate("receivedDate");
            log.info("최신 업데이트 : " + lastUpdatedDate);

            Collection<MailData> mailData = mailService.readRecentMails(dto, lastUpdatedDate);
            for (MailData data : mailData) {
                List<QueryDocumentSnapshot> documents = ref.whereEqualTo("receivedDate", data.getReceivedDate())
                        .whereEqualTo("from", data.getFrom())
                        .get().get().getDocuments();
                if (documents.isEmpty()) {
                    ApiFuture<DocumentReference> future = ref.add(data);
                    log.info("새로운 메일이 도착했습니다.");
                } else log.warn("이미 존재하는 메일이므로 저장하지 않습니다. 제목 : {}", data.getSubject());
            }
        } catch (IndexOutOfBoundsException e) {
            // 메일 save 를 안해서 메일 save를 먼저 해줘야 하는 경우
            saveMailData(dto);
        }

    }

    @Transactional
    /* MailReceiveDto DB에서 찾기 (by Id, Username) */
    public MailReceiveDto findMailReceiveDtoById(String id, String username) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(COLLECTION_MEMBER).document(id)
                .collection(COLLECTION_ACCOUNT_LIST).document(username).get().get().toObject(MailReceiveDto.class);
    }

    @Transactional
    /* 등록된 MailReceiveDto 모두 찾기 (by id) */
    public List<MailReceiveDto> findReceiveDtoListById(String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference ref = db.collection(COLLECTION_MEMBER).document(id).collection(COLLECTION_ACCOUNT_LIST);

        List<MailReceiveDto> dtoList = new ArrayList<>();
        for (DocumentReference doc : ref.listDocuments()) {
            MailReceiveDto dto = doc.get().get().toObject(MailReceiveDto.class);
            dtoList.add(dto);
        }

        return dtoList;
    }

    /* 뱃지 부여 -> TODO : 새로고침시 부여할 것인가? */
    @Transactional
    public List<Badge> getBadge(String id) throws Exception {

        Firestore db = FirestoreClient.getFirestore();
        Member member = db.collection(COLLECTION_MEMBER).document(id).get().get().toObject(Member.class);
        List<Badge> currentBadgeList = member.getBadgeList();
        List<String> currentAttendance = member.getAttendance();

        long currentMailCount = (long) member.getTotalCount();
        long currentDeletedCount = (long) member.getDeletedCount();
        long deletedRatio = (currentDeletedCount / currentMailCount) * 100;

        if (currentDeletedCount >= 1 && !currentBadgeList.contains(Badge.STARTERS)) {
            currentBadgeList.add(Badge.STARTERS);
            log.info("[뱃지 부여] '{}' 뱃지를 지급했습니다.", Badge.STARTERS.getName());
        }
        else if (currentDeletedCount >= 65 && !currentBadgeList.contains(Badge.ENVIRONMENTAL_TUTELARY)) {
            currentBadgeList.add(Badge.ENVIRONMENTAL_TUTELARY);
            log.info("[뱃지 부여] '{}' 뱃지를 지급했습니다.", Badge.ENVIRONMENTAL_TUTELARY.getName());
        }
        else if (currentDeletedCount >= 422 && !currentBadgeList.contains(Badge.EARTH_TUTELARY)) {
            currentBadgeList.add(Badge.EARTH_TUTELARY);
            log.info("[뱃지 부여] '{}' 뱃지를 지급했습니다.", Badge.EARTH_TUTELARY.getName());
        }
        else if (currentMailCount > 10000 && !currentBadgeList.contains(Badge.MAIL_RICH)) {
            currentBadgeList.add(Badge.MAIL_RICH);
            log.info("[뱃지 부여] '{}' 뱃지를 지급했습니다.", Badge.MAIL_RICH.getName());
        }
        else if (deletedRatio >= 42.195 && !currentBadgeList.contains(Badge.MARBON_MARATHONER)) {
            currentBadgeList.add(Badge.MARBON_MARATHONER);
            log.info("[뱃지 부여] '{}' 뱃지를 지급했습니다.", Badge.MARBON_MARATHONER.getName());
        }
        else if (currentAttendance.size() >= 7 && !currentBadgeList.contains(Badge.ENVIRONMENTAL_MODEL)) {
            currentBadgeList.add(Badge.ENVIRONMENTAL_MODEL);
            log.info("[뱃지 부여] '{}' 뱃지를 지급했습니다.", Badge.ENVIRONMENTAL_MODEL.getName());
        }

        ApiFuture<WriteResult> future = db.collection(COLLECTION_MEMBER).document(id).set(member);
        return currentBadgeList;
    }
}
