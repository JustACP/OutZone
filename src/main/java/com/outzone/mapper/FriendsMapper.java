package com.outzone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outzone.pojo.FriendsDTO;
import com.outzone.pojo.vo.FriendsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendsMapper extends BaseMapper<FriendsDTO> {
    List<FriendsVO> getFriendsList(Long userId);
    List<FriendsVO> getFriendsInviteList(Long userId);
}
