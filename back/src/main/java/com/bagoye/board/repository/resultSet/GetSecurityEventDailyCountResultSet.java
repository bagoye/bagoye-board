package com.bagoye.board.repository.resultSet;

public interface GetSecurityEventDailyCountResultSet {
    String getEventDate();
    String getEventType();
    long getCount();
}
