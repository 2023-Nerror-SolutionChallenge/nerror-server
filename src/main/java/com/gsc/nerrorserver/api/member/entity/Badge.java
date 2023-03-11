package com.gsc.nerrorserver.api.member.entity;

public enum Badge {

    STARTERS("시작이 반"),
    ENVIRONMENTAL_TUTELARY("환경 수호자"),
    EARTH_TUTELARY("지구의 수호자"),
    MAIL_RICH("메일 부자"),
    MARBON_MARATHONER("말본 마라토너"),
    ENVIRONMENTAL_MODEL("환경 모범생");

    public final String name;

    private Badge(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
