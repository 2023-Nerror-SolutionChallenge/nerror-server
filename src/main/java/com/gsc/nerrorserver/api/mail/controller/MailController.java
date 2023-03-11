package com.gsc.nerrorserver.api.mail.controller;

import com.gsc.nerrorserver.api.mail.dto.MailReceiveDto;
import com.gsc.nerrorserver.api.mail.service.MailFirebaseService;
import com.gsc.nerrorserver.api.mail.service.MailService;
import com.gsc.nerrorserver.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mailbox")
public class MailController {

    private final MailService mailService;
    private final MailFirebaseService mailFirebaseService;

    /* 메일 계정 추가 API */
    // TODO : 메일함 현황 불러오기, 사용자 레벨 조회
    @PostMapping("/add")
    public ApiResponse addAccount(HttpServletResponse res, @RequestBody MailReceiveDto dto) {
        try {
            mailFirebaseService.addMailbox(dto);
            return ApiResponse.success("msg", "계정 추가에 성공했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return ApiResponse.fail();
        }
    }

    /* 메일 읽어오는 API */
    @PostMapping("/save")
    public ApiResponse saveMailData(HttpServletResponse res, @RequestBody MailReceiveDto dto) {
        try {
            mailFirebaseService.saveMailData(dto);
            // total count update 쿼리 필요할듯
            return ApiResponse.success("msg", "메세지를 읽어오는 데에 성공했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ApiResponse.unauthorized("error", "메세지를 읽어오는 과정에서 오류가 발생했습니다.");
        }
    }

    /* 새로고침 API (최신 메일 불러오기, 총 메일 개수 업데이트) */
    @GetMapping("/refresh")
    public ApiResponse refresh(HttpServletResponse res, @RequestParam(name = "id") String id, @RequestParam(name = "username") String username) {
        try {
            MailReceiveDto dto = mailFirebaseService.findMailReceiveDtoById(id, username);
            mailFirebaseService.saveRecentMailData(dto);
            mailFirebaseService.updateCounts(id); // 총 메일 갯수 업데이트
            return ApiResponse.success("msg", "[새로고침] 메세지 개수를 업데이트했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ApiResponse.unauthorized("error", "메세지 개수를 업데이트하는 과정에서 오류가 발생했습니다.");
        }
    }

//    @GetMapping("/mailbox/my")
//    public ApiResponse mypage(HttpServletResponse res, @RequestParam(name = "id") String id) {
//        // totalCount, deleteCount, currentLevel 등등 update 후 data fetching
//
//    }
}
