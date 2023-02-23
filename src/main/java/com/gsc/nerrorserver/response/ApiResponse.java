package com.gsc.nerrorserver.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

    private final ApiResponseHeader header;
    private final Map<String, T> body;

    private final static int SUCCESS = 200;
    private final static int NOT_FOUND = 400;
    private final static int FAILED = 500;
    private final static String SUCCESS_MESSAGE = "(200) SUCCESS";
    private final static String NOT_FOUND_MESSAGE = "(400) NOT FOUND";
    private final static String FAILED_MESSAGE = "(500) 서버 오류";

    public static <T> ApiResponse<T> success(String name, T body) {
        Map<String, T> map = new HashMap<>();
        map.put(name, body);

        return new ApiResponse(new ApiResponseHeader(SUCCESS, SUCCESS_MESSAGE), map);
    }

    public static <T> ApiResponse<T> fail() {
        return new ApiResponse(new ApiResponseHeader(FAILED, FAILED_MESSAGE), null);
    }
}
