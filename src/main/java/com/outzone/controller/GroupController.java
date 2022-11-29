package com.outzone.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.outzone.mapper.FriendsMapper;
import com.outzone.mapper.GroupMapper;
import com.outzone.mapper.UserMapper;
import com.outzone.pojo.GroupsDTO;
import com.outzone.pojo.ResponseResult;
import com.outzone.pojo.UserDTO;
import com.outzone.service.SecurityContextService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Todo 创建群组
// Todo 销毁群组
// Todo 添加管理员
// Todo 上传
// Todo 移动
// Todo 下载
// Todo 删除
@Controller
@RequestMapping("/api/group")
public class GroupController {

    ResponseResult ok = new ResponseResult(HttpStatus.OK.value(),"请求成功");
    ResponseResult forbidden= new ResponseResult(HttpStatus.FORBIDDEN.value(), "无权访问");
    ResponseResult failed= new ResponseResult(HttpStatus.NOT_FOUND.value(), "没有找到");
    @Resource
    SecurityContextService securityContextService;
    @Resource
    UserMapper userMapper;
    @Resource
    FriendsMapper friendsMapper;
    @Resource
    GroupMapper groupMapper;
    @PostMapping("/createGroup")
    @ResponseBody
    public ResponseResult createGroup(@RequestBody String JSONString){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        String groupName = JSON.parseObject(JSONString).getString("groupName");
        if(!Objects.isNull(groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                .eq(GroupsDTO::getGroupName,groupName)))){
            return failed;
        }
        Long requestUserId =requestUser.getId();
        List<Long> groupUser = JSONArray.parseArray(JSON.parseObject(JSONString).getString("userId"));
        groupUser.stream()
                .filter(groupUserId -> groupUserId != requestUserId)
                .distinct()
                .collect(Collectors.toList());
        List<UserDTO> toAddUsers = userMapper.selectBatchIds(groupUser);
        if(groupUser.size()!=toAddUsers.size()) return failed;
        Long groupId = IdWorker.getId();
        GroupsDTO masterOfGroup = new GroupsDTO(null,groupId,requestUser.getId(),groupName,true,"master");
        groupMapper.insert(masterOfGroup);
        for(int i = 0;i < toAddUsers.size();i++){
            GroupsDTO groupMember = new GroupsDTO(null,groupId,toAddUsers.get(i).getId(),groupName,false,"user");
            groupMapper.insert(groupMember);
        }

        return ok;

    }
    @GetMapping("/getGroupInvite")
    @ResponseBody
    public ResponseResult getGroupInvite(){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<GroupsDTO> inviteList = groupMapper.selectList(new LambdaQueryWrapper<GroupsDTO>()
                .eq(GroupsDTO::getIsMemeber,false)
                .eq(GroupsDTO::getUserId,requestUser.getId()));
        ok.setData(inviteList);
        return ok;
    }

    @GetMapping("/allowGroupInvite")
    @ResponseBody
    public ResponseResult allowGroupInvite(HttpServletRequest request){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        Long inviteGroupId = Long.valueOf(request.getParameter("inviteGroupId"));
        GroupsDTO inviteGroup = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                .eq(GroupsDTO::getUserId,requestUser.getId())
                .eq(GroupsDTO::getGroupId,inviteGroupId));

        if(Objects.isNull(inviteGroup)) return failed;
        inviteGroup.setIsMemeber(true);
        groupMapper.updateById(inviteGroup);
        return ok;
    }

    @PostMapping("/inviteUser")
    @ResponseBody
    public ResponseResult inviteUser(@RequestParam String JSONString ){
        ok.setData(null);
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();

        Long groupId  = JSON.parseObject(JSONString).getLong("groupId");
        GroupsDTO isAdmin = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                .eq(GroupsDTO::getGroupId,groupId)
                .eq(GroupsDTO::getUserId,requestUser.getId()));
        if(Objects.isNull(isAdmin)) return failed;
        if(!isAdmin.getRole().equals("master") && !isAdmin.getRole().equals("admin")) return forbidden;

        List<Long>  toInviteUserId = JSONArray.parseArray(
                JSON.parseObject(JSONString).getString("userId"));

        List<UserDTO> toAddUsers = userMapper.selectBatchIds(toInviteUserId);
        if(toInviteUserId.size()!=toAddUsers.size()) return failed;

        for(int i = 0;i < toAddUsers.size();i++){
            GroupsDTO groupMember = new GroupsDTO(null,groupId,toAddUsers.get(i).getId(), isAdmin.getGroupName()
                    , false,"user");
            groupMapper.insert(groupMember);
        }
        return ok;

    }
}
