package com.outzone.pojo.vo;

import com.outzone.mapper.FriendsMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.sql.Date;

@Data
@Accessors(chain = true)
public class FriendsVO {
    Long id;
    String username;
    String icon;
    Date time;
    boolean isFriend;

    public FriendsVO(Long id,String username){
        this.id = id;
        this.username = username;
    }

}
