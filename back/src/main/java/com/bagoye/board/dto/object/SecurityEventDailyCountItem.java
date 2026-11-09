package com.bagoye.board.dto.object;

import com.bagoye.board.repository.resultSet.GetSecurityEventDailyCountResultSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventDailyCountItem {
    private String date;
    private String eventType;
    private long count;

    public SecurityEventDailyCountItem(GetSecurityEventDailyCountResultSet resultSet) {
        this.date = resultSet.getEventDate();
        this.eventType = resultSet.getEventType();
        this.count = resultSet.getCount();
    }

    public static List<SecurityEventDailyCountItem> copyList(List<GetSecurityEventDailyCountResultSet> resultSets) {
        List<SecurityEventDailyCountItem> list = new ArrayList<>();
        for (GetSecurityEventDailyCountResultSet resultSet : resultSets) {
            SecurityEventDailyCountItem item = new SecurityEventDailyCountItem(resultSet);
            list.add(item);
        }
        return list;
    }
}
