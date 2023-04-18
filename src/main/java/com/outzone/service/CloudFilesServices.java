package com.outzone.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.outzone.mapper.*;
import com.outzone.pojo.*;
import com.outzone.pojo.vo.ContentVO;
import com.outzone.util.IdGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
//TODO 重构冗余代码
public class CloudFilesServices {
    @Resource
    DirectoryMapper directoryMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    GroupMapper groupMapper;
    @Resource
    GroupFileMapper groupFileMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    DeletedFileMapper deletedFileMapper;

    ResponseResult notFountResult = new ResponseResult<>(HttpStatus.NOT_FOUND.value(),"文件不存在");
    ResponseResult forbiddenResult= new ResponseResult<>(HttpStatus.FORBIDDEN.value(),"无权访问");
    ResponseResult okResult= new ResponseResult<>(HttpStatus.OK.value(), "移动成功");
    ResponseResult existResult = new ResponseResult(400,"存在同名文件夹");
    public ResponseResult moveDirectory(UserDTO requestUser, ContentVO toMoveDir, DirectoryDTO destination
            , Long groupId){

        okResult.setMsg("移动成功");
        //目录不可以移动到子目录下面
        if(destination.getAbsolutePath().contains(toMoveDir.getPath())){
            return new ResponseResult(HttpStatus.FORBIDDEN.value(),"禁止移动文件夹到子文件夹内");
        }

        List<ContentVO> destDirs =
                directoryMapper.getDirList(destination.getAbsolutePath(),
                (groupId != -1)?groupId:requestUser.getId() , groupId !=-1);

        for(ContentVO destDirsTmp : destDirs)
            if(destDirsTmp.isDirectoryType() && destDirsTmp.getName().equals(toMoveDir.getName()))
                return existResult;

        List<DirectoryDTO> toMoveDirList = directoryMapper.getAllSubDir(toMoveDir.getPath(),groupId != -1
                ,(groupId != -1)?groupId:requestUser.getId());
        if(Objects.isNull(toMoveDirList)) return notFountResult;
        DirectoryDTO allParentDir = directoryMapper.selectById(toMoveDir.getParentId());


        for(DirectoryDTO tmp:toMoveDirList) {
            //文件夹处理的多种情况
            if(tmp.getAbsolutePath().equals(toMoveDir.getPath())){
                tmp.setAbsolutePath( destination.getAbsolutePath()+tmp.getName().substring(1));
                tmp.setParentDirectoryId(destination.getDirectoryId());

            }else if(tmp.getAbsolutePath().equals("/")){
                return notFountResult;
            } else {
                if(allParentDir.getAbsolutePath().equals("/")){
                    tmp.setAbsolutePath(destination.getAbsolutePath()+tmp.getAbsolutePath().substring(1));
                } else {
                    tmp.setAbsolutePath(tmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                }
            }


            directoryMapper.updateById(tmp);
        }
        if(groupId == -1L){


            List<UserFileDTO> toMoveUserFile = userFileMapper.getAllSubUserFileList(toMoveDir.getPath(), requestUser.getId());
            for(UserFileDTO tmp:toMoveUserFile){
                if(allParentDir.getAbsolutePath().equals("/")){
                    tmp.setAbsolutePath(destination.getAbsolutePath()+tmp.getAbsolutePath().substring(1));
                } else {
                    tmp.setAbsolutePath(tmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                }
                userFileMapper.updateById(tmp);
            }




        }else{
            GroupsDTO userGroupRole = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,groupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));

            if(Objects.isNull(userGroupRole)) return forbiddenResult;


            if(userGroupRole.getRole().equals("user")){

                if(toMoveDirList.get(0).getOwnerId() != requestUser.getId()) return forbiddenResult;

            }else if(userGroupRole.equals("admin")){

                UserDTO master = groupMapper.getGroupMaster(Long.valueOf(groupId));
                if(toMoveDirList.get(0).getOwnerId() == master.getId()) return forbiddenResult;

            }else if(userGroupRole.getRole().equals("master")){
            }else{
                return forbiddenResult;
            }

            List<GroupFileDTO> toMoveGroupFile = groupFileMapper.getAllSubGroupFileList(toMoveDir.getPath(), groupId);
            for(GroupFileDTO tmp : toMoveGroupFile){
                if(allParentDir.getAbsolutePath().equals("/")){
                    tmp.setAbsolutePath(destination.getAbsolutePath()+tmp.getAbsolutePath().substring(1));
                } else {
                    tmp.setAbsolutePath(tmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                }
                groupFileMapper.updateById(tmp);
            }


        }

        return okResult;


    }

    public ResponseResult moveFiles(UserDTO requestUser, ContentVO toMoveFile, DirectoryDTO destination
            , Long groupId){
        okResult.setMsg("移动成功");
        if(groupId == -1){
            UserFileDTO toUpdateFile = userFileMapper.selectOne(new LambdaQueryWrapper<UserFileDTO>()
                    .eq(UserFileDTO::getId,toMoveFile.getId())
                    .eq(UserFileDTO::getUserId,requestUser.getId()));

            if(Objects.isNull(toUpdateFile)) return notFountResult;

            toUpdateFile.setParentDirectoryId(destination.getDirectoryId())
                    .setAbsolutePath(destination.getAbsolutePath());
            userFileMapper.updateById(toUpdateFile);

        }else{
            GroupsDTO userGroupRole = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,groupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));

            GroupFileDTO toUpdateFile = groupFileMapper.selectOne(new LambdaQueryWrapper<GroupFileDTO>()
                    .eq(GroupFileDTO::getId,toMoveFile.getId())
                    .eq(GroupFileDTO::getGroupId,groupId));
            if(Objects.isNull(toUpdateFile)) return notFountResult;


            if(Objects.isNull(userGroupRole)) return forbiddenResult;
            if(userGroupRole.getRole().equals("user")){

                if(toUpdateFile.getUserId() != requestUser.getId()) return forbiddenResult;

            }else if(userGroupRole.equals("admin")){

                UserDTO master = groupMapper.getGroupMaster(Long.valueOf(groupId));
                if(toUpdateFile.getUserId() == master.getId()) return forbiddenResult;

            }else if(userGroupRole.getRole().equals("master")){
            }else{
                return forbiddenResult;
            }

            toUpdateFile.setParentDirectoryId(destination.getDirectoryId())
                    .setAbsolutePath(destination.getAbsolutePath());
            groupFileMapper.updateById(toUpdateFile);
        }

        return okResult;
    }



    public ResponseResult deleteDir(UserDTO requestUser, ContentVO toDeleteDir, DirectoryDTO destination
            , Long groupId){

        okResult.setMsg("删除成功");
        DirectoryDTO isExist = directoryMapper.selectOne(new LambdaQueryWrapper<DirectoryDTO>()
                .eq(DirectoryDTO::getDirectoryId,toDeleteDir.getId())
                .eq(DirectoryDTO::isGroupDirectory,groupId!=-1));
        ResponseResult okResult= new ResponseResult<>(HttpStatus.OK.value(), "移动成功");

        directoryMapper.delAllSubDir(toDeleteDir.getPath(),groupId != -1,(groupId != -1)? groupId: requestUser.getId());
        if(groupId == -1L){



            userFileMapper.delAllSubUserFile(toDeleteDir.getPath(),requestUser.getId());

        }else{
            GroupsDTO userGroupRole = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,groupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));

            if(Objects.isNull(userGroupRole)) return forbiddenResult;


            if(userGroupRole.getRole().equals("user")){

                if(isExist.getOwnerId() != requestUser.getId()) return forbiddenResult;

            }else if(userGroupRole.equals("admin")){

                UserDTO master = groupMapper.getGroupMaster(Long.valueOf(groupId));
                if(isExist.getOwnerId() == master.getId()) return forbiddenResult;

            }else if(userGroupRole.getRole().equals("master")){
            }else{
                return forbiddenResult;
            }


            groupFileMapper.delAllSubGroupFile(toDeleteDir.getPath(), groupId);
        }
        return okResult;


    }

    public ResponseResult copyFiles(UserDTO requestUser, ContentVO toCopyFile, DirectoryDTO destination
            , Long groupId){

        okResult.setMsg("复制成功");

        destination = directoryMapper.selectById(destination.getDirectoryId());
        if(Objects.isNull(destination)) return notFountResult;
        DirectoryDTO allParentDir = directoryMapper.selectById(toCopyFile.getParentId());
        if(groupId == -1){
            UserFileDTO copiedFiles = userFileMapper.selectOne(new LambdaQueryWrapper<UserFileDTO>()
                    .eq(UserFileDTO::getId,toCopyFile.getId())
                    .eq(UserFileDTO::getUserId,requestUser.getId()));

            copiedFiles.setUploadDate(new Timestamp(new Date().getTime()));

            copiedFiles.setAbsolutePath(destination.getAbsolutePath());

            copiedFiles.setId(IdGeneratorUtil.generateId());

            userFileMapper.insert(copiedFiles);
        }else{
            GroupFileDTO copiedFiles = groupFileMapper.selectOne(new LambdaQueryWrapper<GroupFileDTO>()
                    .eq(GroupFileDTO::getGroupId,groupId)
                    .eq(GroupFileDTO::getId,toCopyFile.getId()));
            copiedFiles.setAbsolutePath(destination.getAbsolutePath());
            copiedFiles.setFileName(toCopyFile.getName());
            copiedFiles.setUserId(requestUser.getId());
            copiedFiles.setUploadDate(new Timestamp(new Date().getTime()));
            copiedFiles.setId(IdGeneratorUtil.generateId());
            groupFileMapper.insert(copiedFiles);
        }

        return okResult;

    }

    public ResponseResult deleteFiles(UserDTO requestUser, ContentVO toDeleteFiles, DirectoryDTO destination
            , Long groupId){

        okResult.setMsg("删除成功");

        ResponseResult okResult= new ResponseResult<>(HttpStatus.OK.value(), "移动成功");


        if(groupId == -1L){
            UserFileDTO toDeleteFile =  userFileMapper.selectOne(new LambdaQueryWrapper<UserFileDTO>()
                    .eq(UserFileDTO::getUserId,requestUser.getId())
                    .eq(UserFileDTO::getId,toDeleteFiles.getId()));
            if(Objects.isNull(toDeleteFile)){
                return notFountResult;
            }



            userFileMapper.deleteById(toDeleteFile.getId());
            DeletedFileDTO delFile = new DeletedFileDTO();
            BeanUtils.copyProperties(toDeleteFile,delFile);
            deletedFileMapper.insert(delFile);

        }else{
            GroupsDTO userGroupRole = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,groupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));

            if(Objects.isNull(userGroupRole)) return forbiddenResult;
            GroupFileDTO toDeleteFile = groupFileMapper.selectOne(new LambdaQueryWrapper<GroupFileDTO>()
                    .eq(GroupFileDTO::getGroupId,groupId)
                    .eq(GroupFileDTO::getId,toDeleteFiles.getId()));
            if(Objects.isNull(toDeleteFile)) return notFountResult;
            if(userGroupRole.getRole().equals("user")){

                if(toDeleteFile.getUserId() != requestUser.getId()) return forbiddenResult;

            }else if(userGroupRole.equals("admin")){

                UserDTO master = groupMapper.getGroupMaster(Long.valueOf(groupId));
                if(toDeleteFile.getUserId() == master.getId()) return forbiddenResult;

            }else if(userGroupRole.getRole().equals("master")){
            }else{
                return forbiddenResult;
            }


            groupFileMapper.deleteById(toDeleteFile.getId());
        }
        return okResult;


    }

    public ResponseResult copyDir(UserDTO requestUser, ContentVO toCopyDir, DirectoryDTO destination
            , Long destGroupId){
        okResult.setMsg("复制成功");

        List<ContentVO> destDirs =
                directoryMapper.getDirList(destination.getAbsolutePath(),
                        (destGroupId != -1)?destGroupId:requestUser.getId() , destGroupId !=-1);

        for(ContentVO destDirsTmp : destDirs)
            if(destDirsTmp.isDirectoryType() && destDirsTmp.getName().equals(toCopyDir.getName()))
                return existResult;

        List<DirectoryDTO> toCopyDirList = directoryMapper.getAllSubDir(toCopyDir.getPath(),destGroupId != -1
                ,(destGroupId != -1)?destGroupId:requestUser.getId());

        if(Objects.isNull(toCopyDirList)) return notFountResult;
        HashMap<Long,DirectoryDTO> copiedDirList = new HashMap<>();
        DirectoryDTO allParentDir = directoryMapper.selectById(toCopyDir.getParentId());
        Queue<DirectoryDTO> queue = new ArrayDeque<>();
        //插入新的目录
        for(DirectoryDTO copyTmp : toCopyDirList){

            if(copyTmp.getAbsolutePath().equals(toCopyDir.getPath())){

                copiedDirList.put(copyTmp.getDirectoryId(),copyTmp);
                copyTmp.setDirectoryId(IdGeneratorUtil.generateId());
                copyTmp.setParentDirectoryId((destination.getDirectoryId()));

                copyTmp.setAbsolutePath(destination.getAbsolutePath()+copyTmp.getName().substring(1));

                directoryMapper.insert(copyTmp);
            }else if(copyTmp.getAbsolutePath().equals("/")){
                return notFountResult;
            } else {
                queue.add(copyTmp);
            }

        }
        while(!queue.isEmpty()){
            DirectoryDTO tmp = queue.remove();
            if(copiedDirList.containsKey(tmp.getParentDirectoryId())){

                if(allParentDir.getAbsolutePath().equals("/")){
                    tmp.setAbsolutePath(destination.getAbsolutePath()+tmp.getAbsolutePath().substring(1));
                } else {
                    tmp.setAbsolutePath(tmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                }
                copiedDirList.put(tmp.getDirectoryId(),tmp);
                tmp.setDirectoryId(IdGeneratorUtil.generateId());
                tmp.setParentDirectoryId(copiedDirList.get(tmp.getParentDirectoryId()).getDirectoryId());


                directoryMapper.insert(tmp);
            }else{
                queue.add(tmp);
            }
        }

        if(destGroupId == -1L){

            List<UserFileDTO> toCopyUserFileList = userFileMapper.getAllSubUserFileList(toCopyDir.getPath(), requestUser.getId());
            if(Objects.isNull(toCopyUserFileList)) return okResult;
            for(UserFileDTO userFileTmp:toCopyUserFileList) {

                if (allParentDir.getAbsolutePath().equals("/") && !userFileTmp.getAbsolutePath().equals("/")) {
                    userFileTmp.setAbsolutePath(destination.getAbsolutePath() + userFileTmp.getAbsolutePath().substring(1));
                } else if (allParentDir.getAbsolutePath().equals("/") && !userFileTmp.getAbsolutePath().equals("/")){
                    userFileTmp.setAbsolutePath(destination.getAbsolutePath());
                } else {
                    userFileTmp.setAbsolutePath(userFileTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                }

                DirectoryDTO tmpParentDir = copiedDirList.get(userFileTmp.getParentDirectoryId());
                userFileTmp.setParentDirectoryId(tmpParentDir.getDirectoryId());
                userFileTmp.setUploadDate(new Timestamp(new Date().getTime()));
                userFileTmp.setId(IdGeneratorUtil.generateId());
                userFileMapper.insert(userFileTmp);
            }

        }else{
            List<GroupFileDTO> toCopyGroupFileList = groupFileMapper.getAllSubGroupFileList(toCopyDir.getPath(), destGroupId);


            GroupsDTO userGroupRole = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,destGroupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));
            if(Objects.isNull(userGroupRole)) return forbiddenResult;
            for(GroupFileDTO groupFileTmp : toCopyGroupFileList){
                groupFileTmp.setId(null);
                if (allParentDir.getAbsolutePath().equals("/") && !groupFileTmp.getAbsolutePath().equals("/")) {
                    groupFileTmp.setAbsolutePath(destination.getAbsolutePath() + groupFileTmp.getAbsolutePath().substring(1));
                } else if (allParentDir.getAbsolutePath().equals("/") && !groupFileTmp.getAbsolutePath().equals("/")){
                    groupFileTmp.setAbsolutePath(destination.getAbsolutePath());
                } else {
                    groupFileTmp.setAbsolutePath(groupFileTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                }
                DirectoryDTO tmpParentDir = copiedDirList.get(groupFileTmp.getParentDirectoryId());
                groupFileTmp.setParentDirectoryId(tmpParentDir.getDirectoryId());
                groupFileTmp.setUploadDate(new Timestamp(new Date().getTime()));
                groupFileTmp.setUserId(requestUser.getId());
                groupFileTmp.setId(IdGeneratorUtil.generateId());
                groupFileMapper.insert(groupFileTmp);
            }


        }


        return okResult;


    }

    public ResponseResult restorage(UserDTO requestUser, ContentVO toStorageFile, DirectoryDTO destination
            , Long sourceGroupId,Long destGroupId){
        Long sourceUserId = -1L;
        if(sourceGroupId != -1){
            GroupsDTO sourceGroup = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,sourceGroupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));
            if(Objects.isNull(sourceGroup)) return forbiddenResult;
        }
        if(destGroupId != -1){
            GroupsDTO destGroup = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId,destGroupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));
            if(Objects.isNull(destGroup)) return forbiddenResult;
        }

        okResult.setMsg("转存成功");

        if(toStorageFile.isDirectoryType()){

            List<ContentVO> destDirs =
                    directoryMapper.getDirList(destination.getAbsolutePath(),
                            (destGroupId != -1)?destGroupId:requestUser.getId() , destGroupId !=-1);

            for(ContentVO destDirsTmp : destDirs)
                if(destDirsTmp.isDirectoryType() && destDirsTmp.getName().equals(toStorageFile.getName()))
                    return existResult;

            DirectoryDTO sourceFile = directoryMapper.selectById(toStorageFile.getId());
            if(sourceGroupId == -1) sourceUserId = sourceFile.getOwnerId();

            List<DirectoryDTO> toCopyDirList = directoryMapper.getAllSubDir(toStorageFile.getPath(),sourceGroupId != -1
                    ,(sourceGroupId != -1)?sourceGroupId:sourceUserId);
            toCopyDirList.sort((t0, t1) -> t0.getAbsolutePath().length() - t1.getAbsolutePath().length());
            if(Objects.isNull(toCopyDirList)) return notFountResult;
            HashMap<String,DirectoryDTO> copiedDirList = new HashMap<>();
            DirectoryDTO allParentDir = directoryMapper.selectById(toStorageFile.getParentId());
            //插入新的目录
            for(DirectoryDTO copyTmp : toCopyDirList){

                if(copyTmp.getAbsolutePath().equals(toStorageFile.getPath())){
                    copyTmp.setAbsolutePath(destination.getAbsolutePath()+copyTmp.getName().substring(1));

                }else if(copyTmp.getAbsolutePath().equals("/")){
                    return notFountResult;
                } else {
                    if(allParentDir.getAbsolutePath().equals("/")){
                        copyTmp.setAbsolutePath(destination.getAbsolutePath()+copyTmp.getAbsolutePath().substring(1));
                    } else {
                        copyTmp.setAbsolutePath(copyTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                    }
                }
                DirectoryDTO parent = copiedDirList.get(copyTmp.getAbsolutePath().replaceAll(copyTmp.getName(), "/"));
                if(Objects.isNull(parent)) copyTmp.setParentDirectoryId(destination.getDirectoryId());
                else copyTmp.setParentDirectoryId(parent.getDirectoryId());

                copyTmp.setDirectoryId(IdGeneratorUtil.generateId());

                copyTmp.setOwnerId((destGroupId !=-1)?destGroupId: requestUser.getId());
                copyTmp.setGroupDirectory((destGroupId != -1));

                copiedDirList.put(copyTmp.getAbsolutePath(),copyTmp);
                copyTmp.setDirectoryId(IdGeneratorUtil.generateId());
                directoryMapper.insert(copyTmp);
            }


            if(sourceGroupId == -1L){

                List<UserFileDTO> toCopyUserFileList = userFileMapper.getAllSubUserFileList(toStorageFile.getPath(), sourceUserId);
                if(Objects.isNull(toCopyUserFileList)) return okResult;
                if(destGroupId == -1){

                }
                for(UserFileDTO userFileTmp:toCopyUserFileList) {
                    if(destGroupId == -1){
                        userFileTmp.setId(null);
                        if (allParentDir.getAbsolutePath().equals("/") && !userFileTmp.getAbsolutePath().equals("/")) {
                            userFileTmp.setAbsolutePath(destination.getAbsolutePath() + userFileTmp.getAbsolutePath().substring(1));
                        } else if (allParentDir.getAbsolutePath().equals("/") && !userFileTmp.getAbsolutePath().equals("/")){
                            userFileTmp.setAbsolutePath(destination.getAbsolutePath());
                        } else {
                            userFileTmp.setAbsolutePath(userFileTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                        }

                        DirectoryDTO tmpParentDir = copiedDirList.get(userFileTmp.getAbsolutePath());
                        if(Objects.isNull(tmpParentDir)) userFileTmp.setParentDirectoryId(destination.getDirectoryId());
                        else userFileTmp.setParentDirectoryId(tmpParentDir.getDirectoryId());
                        userFileTmp.setUserId(requestUser.getId());
                        userFileTmp.setUploadDate(new Timestamp(new Date().getTime()));
                        userFileTmp.setId(IdGeneratorUtil.generateId());
                        userFileMapper.insert(userFileTmp);
                    }else{
                        GroupFileDTO groupFileTmp = new GroupFileDTO();
                        groupFileTmp.setId(null);
                        if (allParentDir.getAbsolutePath().equals("/") && !userFileTmp.getAbsolutePath().equals("/")) {
                            groupFileTmp.setAbsolutePath(destination.getAbsolutePath() + userFileTmp.getAbsolutePath().substring(1));
                        } else if (allParentDir.getAbsolutePath().equals("/") && !userFileTmp.getAbsolutePath().equals("/")){
                            groupFileTmp.setAbsolutePath(destination.getAbsolutePath());
                        } else {
                            groupFileTmp.setAbsolutePath(userFileTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                        }
                        DirectoryDTO tmpParentDir = copiedDirList.get(groupFileTmp.getAbsolutePath());
                        if(Objects.isNull(tmpParentDir)) groupFileTmp.setParentDirectoryId(destination.getDirectoryId());
                        else groupFileTmp.setParentDirectoryId(tmpParentDir.getDirectoryId());
                        groupFileTmp.setFileId(userFileTmp.getFileId());

                        groupFileTmp.setUploadDate(new Timestamp(new Date().getTime()));
                        groupFileTmp.setUserId(requestUser.getId());
                        groupFileTmp.setFileName(userFileTmp.getFileName());
                        groupFileTmp.setGroupId(destGroupId);
                        groupFileTmp.setId(IdGeneratorUtil.generateId());
                        groupFileMapper.insert(groupFileTmp);
                    }
                }



            }else{
                List<GroupFileDTO> toCopyGroupFileList = groupFileMapper.getAllSubGroupFileList(toStorageFile.getPath(), sourceGroupId);


                GroupsDTO userGroupRole = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                        .eq(GroupsDTO::getGroupId,sourceGroupId)
                        .eq(GroupsDTO::getUserId,requestUser.getId()));
                if(Objects.isNull(userGroupRole)) return forbiddenResult;
                for(GroupFileDTO groupFileTmp : toCopyGroupFileList){
                    if(destGroupId != -1){
                        groupFileTmp.setId(null);
                        if (allParentDir.getAbsolutePath().equals("/") && !groupFileTmp.getAbsolutePath().equals("/")) {
                            groupFileTmp.setAbsolutePath(destination.getAbsolutePath() + groupFileTmp.getAbsolutePath().substring(1));
                        } else if (allParentDir.getAbsolutePath().equals("/") && !groupFileTmp.getAbsolutePath().equals("/")){
                            groupFileTmp.setAbsolutePath(destination.getAbsolutePath());
                        } else {
                            groupFileTmp.setAbsolutePath(groupFileTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                        }
                        DirectoryDTO tmpParentDir = copiedDirList.get(groupFileTmp.getAbsolutePath());
                        if(Objects.isNull(tmpParentDir)) groupFileTmp.setParentDirectoryId(destination.getDirectoryId());
                        else groupFileTmp.setParentDirectoryId(tmpParentDir.getDirectoryId());
                        groupFileTmp.setUploadDate(new Timestamp(new Date().getTime()));
                        groupFileTmp.setGroupId(destGroupId);

                        groupFileTmp.setUserId(requestUser.getId());
                        groupFileTmp.setFileId(IdGeneratorUtil.generateId());
                        groupFileMapper.insert(groupFileTmp);
                    }else{
                        UserFileDTO userFileTmp = new UserFileDTO();
                        userFileTmp.setId(null);
                        if (allParentDir.getAbsolutePath().equals("/") && !groupFileTmp.getAbsolutePath().equals("/")) {
                            userFileTmp.setAbsolutePath(destination.getAbsolutePath() + groupFileTmp.getAbsolutePath().substring(1));
                        } else if (allParentDir.getAbsolutePath().equals("/") && !groupFileTmp.getAbsolutePath().equals("/")){
                            userFileTmp.setAbsolutePath(destination.getAbsolutePath());
                        } else {
                            userFileTmp.setAbsolutePath(groupFileTmp.getAbsolutePath().replaceAll(allParentDir.getAbsolutePath(), destination.getAbsolutePath()));
                        }

                        DirectoryDTO tmpParentDir = copiedDirList.get(groupFileTmp.getAbsolutePath());
                        userFileTmp.setFileName(groupFileTmp.getFileName());
                        if(Objects.isNull(tmpParentDir)) userFileTmp.setParentDirectoryId(destination.getDirectoryId());
                        else userFileTmp.setParentDirectoryId(tmpParentDir.getDirectoryId());
                        userFileTmp.setUserId(requestUser.getId());
                        userFileTmp.setUploadDate(new Timestamp(new Date().getTime()));
                        userFileTmp.setId(IdGeneratorUtil.generateId());
                        userFileMapper.insert(userFileTmp);
                    }

                }


            }




        }else{
            DirectoryDTO allParentDir = directoryMapper.selectById(toStorageFile.getParentId());

            if(sourceGroupId == -1) {
                sourceUserId = userFileMapper.selectById(toStorageFile.getId()).getUserId();
                UserFileDTO copiedFiles = userFileMapper.selectOne(new LambdaQueryWrapper<UserFileDTO>()
                        .eq(UserFileDTO::getId,toStorageFile.getId())
                        .eq(UserFileDTO::getUserId,sourceUserId));
                if(destGroupId == -1){

                    copiedFiles.setUploadDate(new Timestamp(new Date().getTime()));

                    copiedFiles.setAbsolutePath(destination.getAbsolutePath());

                    copiedFiles.setId(IdGeneratorUtil.generateId());
                    copiedFiles.setParentDirectoryId(destination.getParentDirectoryId());
                    copiedFiles.setUserId(requestUser.getId());
                    copiedFiles.setUploadDate(new Timestamp(new Date().getTime()));

                    userFileMapper.insert(copiedFiles);
                }else{
                    GroupFileDTO toStorageIntoGroup = new GroupFileDTO();
                    toStorageIntoGroup.setAbsolutePath(destination.getAbsolutePath())
                                    .setFileId(toStorageFile.getId())
                                    .setFileName(toStorageFile.getName())
                                    .setUploadDate(new Timestamp(new Date().getTime()))
                                    .setId(IdGeneratorUtil.generateId())
                                    .setParentDirectoryId(destination.getDirectoryId())
                                    .setGroupId(destGroupId)
                                    .setUserId(requestUser.getId());

                    groupFileMapper.insert(toStorageIntoGroup);
                }
            }else{
                GroupFileDTO copiedFile = groupFileMapper.selectById(toStorageFile.getId());
                if(destGroupId == -1){
                    UserFileDTO toStorageIntoUser = new UserFileDTO(IdGeneratorUtil.generateId(), requestUser.getId(), copiedFile.getFileId(),
                            destination.getDirectoryId(),destination.getAbsolutePath(),new Timestamp(new Date().getTime()),
                            toStorageFile.getName());
                    userFileMapper.insert(toStorageIntoUser);

                }else{
                    copiedFile.setUserId(requestUser.getId())
                            .setUploadDate(new Timestamp(new Date().getTime()))
                            .setId(IdGeneratorUtil.generateId())
                            .setAbsolutePath(destination.getAbsolutePath())
                            .setParentDirectoryId(destination.getParentDirectoryId());
                    groupFileMapper.insert(copiedFile);
                }
            }





        }
        return okResult;

    }

    public ResponseResult recoveryDelFiles(List<Long> ids){
        List<DeletedFileDTO> delFileList = deletedFileMapper.selectBatchIds(ids);
//        List<UserFileDTO> userFileDTOList = new ArrayList<>();

        for(DeletedFileDTO tmp : delFileList){
            UserFileDTO user = new UserFileDTO();
            BeanUtils.copyProperties(tmp, user);
            userFileMapper.insert(user);
        }
        okResult.setMsg("复制成功");
        return okResult;


    }

}
