package com.bagoye.board.dto.object;

import com.bagoye.board.repository.resultSet.GetSecurityEventIpCountResultSet;
import com.bagoye.board.repository.resultSet.GetSecurityEventTypeCountResultSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEventCountItem {
    private String name;
    private long count;

    public static List<SecurityEventCountItem> copyTypeList(List<GetSecurityEventTypeCountResultSet> resultSets) {
        List<SecurityEventCountItem> list = new ArrayList<>();
        for (GetSecurityEventTypeCountResultSet resultSet : resultSets) {
            list.add(new SecurityEventCountItem(resultSet.getEventType(), resultSet.getCount()));
        }
        return list;
    }

    public static List<SecurityEventCountItem> copyIpList(List<GetSecurityEventIpCountResultSet> resultSets) {
        List<SecurityEventCountItem> list = new ArrayList<>();
        for (GetSecurityEventIpCountResultSet resultSet : resultSets) {
            list.add(new SecurityEventCountItem(resultSet.getIpAddress(), resultSet.getCount()));
        }
        return list;
    }
}
