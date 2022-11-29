package com.outzone.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.outzone.mapper.FriendsMapper;
import com.outzone.mapper.UserMapper;
import com.outzone.pojo.FriendsDTO;
import com.outzone.pojo.ResponseResult;
import com.outzone.pojo.UserDTO;
import com.outzone.pojo.vo.FriendsVO;
import com.outzone.service.SecurityContextService;
import jdk.jfr.Frequency;
import org.apache.ibatis.annotations.Param;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//Todo 添加朋友删除朋友 搜索朋友
@Controller
@RequestMapping("/api/friends")
public class FirendController {
    @Resource
    SecurityContextService securityContextService;
    @Resource
    UserMapper userMapper;
    @Resource
    FriendsMapper friendsMapper;

    ResponseResult ok = new ResponseResult(HttpStatus.OK.value(),"请求成功");
    ResponseResult forbidden= new ResponseResult(HttpStatus.FORBIDDEN.value(), "无权访问");
    ResponseResult failed= new ResponseResult(HttpStatus.NOT_FOUND.value(), "没有找到");
    @GetMapping("/searchUser")
    @ResponseBody
    public ResponseResult seachUserByUsername(@RequestParam String username){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        if(Objects.isNull(requestUser)) return forbidden;

        List<FriendsVO> searchedUser = userMapper.searchUserByUsername(username,requestUser.getId());
        ok.setData(searchedUser);
        return ok;

    }

    @GetMapping("/makeFriendsWith")
    @ResponseBody
    public ResponseResult makeFriendsWith(HttpServletRequest request){
        ok.setData(null);
        UserDTO  requestUser = securityContextService.getUserFromContext().getUserDTO();
        if(Objects.isNull(requestUser)) return forbidden;

        Long destUserId = Long.valueOf(request.getParameter("destUserId"));
        UserDTO destUser = userMapper.selectById(destUserId);
        if(Objects.isNull(destUser)) return failed;

        FriendsDTO friend = new FriendsDTO();

        friend.setId(null)
                .setIsFriend(false)
                .setInviteId(requestUser.getId())
                .setInvitedId(destUserId)
                .setTime(new Timestamp(new Date().getTime()));

        friendsMapper.insert(friend);

        return ok;

    }

    @GetMapping("/getFriendsList")
    @ResponseBody
    public ResponseResult getFriendsList(){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<FriendsVO> friendsList = friendsMapper.getFriendsList(requestUser.getId());
        ok.setData(friendsList);
        return  ok;
    }

    @GetMapping("/getFriendsInviteList")
    @ResponseBody
    public ResponseResult getFriendsInviteList(){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<FriendsVO> requestToBeFriend = friendsMapper.getFriendsInviteList(requestUser.getId());
        ok.setData(requestToBeFriend);
        return ok;
    }

    @GetMapping("/allowFriendInvite")
    @ResponseBody
    public ResponseResult allowFriendInvite(HttpServletRequest request){
        ok.setData(null);

        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        Long inviteUserId = Long.valueOf(request.getParameter("inviteUserId"));
        FriendsDTO allowFriend= friendsMapper.selectOne(new LambdaQueryWrapper<FriendsDTO>()
                .eq(FriendsDTO::getInviteId,inviteUserId)
                .eq(FriendsDTO::getInvitedId,requestUser.getId())
                .eq(FriendsDTO::getIsFriend,false));
        if(Objects.isNull(allowFriend)) return failed;

        allowFriend.setIsFriend(true);
        FriendsDTO newFriend = new FriendsDTO();
        newFriend.setTime(new Timestamp(new Date().getTime()))
                        .setIsFriend(true)
                        .setInvitedId(allowFriend.getInviteId())
                        .setInviteId(allowFriend.getInvitedId())
                        .setId(null);
        allowFriend.setTime(newFriend.getTime());
        friendsMapper.updateById(allowFriend);
        friendsMapper.insert(newFriend);
        return ok;



    }

    @GetMapping("/delFriend")
    @ResponseBody
    public ResponseResult delFriend(HttpServletRequest request){
        ok.setData(null);
        Long toDeleteUserId = Long.valueOf(request.getParameter("toDeleteUserId"));
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        FriendsDTO toDeleteUser = friendsMapper.selectOne(new LambdaQueryWrapper<FriendsDTO>()
                .eq(FriendsDTO::getInviteId,toDeleteUserId)
                .eq(FriendsDTO::getInvitedId,requestUser.getId())
                .eq(FriendsDTO::getIsFriend,true));
        FriendsDTO ownRelationship = friendsMapper.selectOne(new LambdaQueryWrapper<FriendsDTO>()
                .eq(FriendsDTO::getInvitedId,toDeleteUserId)
                .eq(FriendsDTO::getInviteId,requestUser.getId())
                .eq(FriendsDTO::getIsFriend,true));
        if(Objects.isNull(toDeleteUser) || Objects.isNull(ownRelationship)) return failed;
        List<Long> deleteId = new ArrayList<>();
        deleteId.add(ownRelationship.getId());
        deleteId.add(toDeleteUser.getId());
        userMapper.deleteBatchIds(deleteId);
        return ok;

    }






}
