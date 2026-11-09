package com.bagoye.board.common;

public enum Role {
    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + name();
    }
}
