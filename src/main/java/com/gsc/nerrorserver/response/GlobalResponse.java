package com.gsc.nerrorserver.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GlobalResponse {

    private String msg;
    private int statusCode;

    public GlobalResponse(String msg, int statusCode) {
        this.msg = msg;
        this.statusCode = statusCode;
    }

}
