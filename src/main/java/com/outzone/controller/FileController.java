package com.outzone.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.outzone.mapper.*;
import com.outzone.pojo.*;
import com.outzone.pojo.vo.ContentVO;
import com.outzone.service.CloudFilesServices;
import com.outzone.service.FileUploadService;
import com.outzone.service.SecurityContextService;
import com.outzone.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/file")
@Slf4j

public class FileController {

    private final static String utf8 = "UTF-8";
    @Value("${upload.file.path}")
    private String uploadFilePath;
    @Resource
    private FileUploadService fileUploadService;

    @Resource
    UserFileMapper userFileMapper;
    @Resource
    GroupFileMapper groupFileMapper;
    @Resource
    FileMapper fileMapper;
    @Resource
    SecurityContextService securityContextService;
    @Resource
    DirectoryMapper directoryMapper;
    @Resource
    GroupMapper groupMapper;
    @Resource
    CloudFilesServices cloudFilesServices;
    @Resource
    UserMapper userMapper;
    @Resource
    RedisUtil redisUtil;
    @Resource
    ShareMapper shareMapper;
    private final String downloadDomain = StaticValue.url;



    /**
     * 上传前调用(只调一次)，判断文件是否已经被上传完成，如果是，跳过，
     * 如果不是，判断是否传了一半，如果是，将缺失的分片编号返回，让前端传输缺失的分片即可
     */
    @GetMapping("/upload")
    @ResponseBody
    public void checkFile (MultipartFileParamsVO fileParamsVO , HttpServletResponse response) throws IOException {
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();

        //采用NoContent 表示有要跳过的

        ResponseResult res = fileUploadService.checkFileAndChunks(fileParamsVO,response,requestUser);

        ResponseResult.writeByResponse(response,res);



    }

    @GetMapping("/preCheckFileExist")
    public void preCheckFileExist(MultipartFileParamsVO fileParamsVO , HttpServletResponse response) throws IOException {
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        ResponseResult responseResult = new ResponseResult<>();
        if(fileUploadService.checkFile(fileParamsVO,requestUser)){
            responseResult.setCode(HttpStatus.OK.value());
            responseResult.setMsg("文件已存在");
            Map<String,Boolean> isSkip = new HashMap<>();
            isSkip.put("isSkip",true);
            isSkip.put("needMerge", false);
            responseResult.setData(isSkip);

            if(fileParamsVO.getGroupId()==-1){
                fileUploadService.uploadExistUserFile(fileParamsVO, requestUser);
            }else{
                fileUploadService.uploadExistGroupFile(fileParamsVO,requestUser);
            }


        }else{
            responseResult.setCode(210);
            responseResult.setMsg("文件不存在");
            Map<String,Boolean> isSkip = new HashMap<>();
            isSkip.put("isSkip",false);
            isSkip.put("needMerge", true);
            responseResult.setData(isSkip);
        }
        ResponseResult.writeByResponse(response,responseResult);

    }

    @GetMapping("/test")

    public void test(HttpServletResponse response) throws IOException {
        ResponseResult res = new ResponseResult<>(200,"123");


        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.setStatus(204);
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(res));
        writer.flush();




    }


    @PostMapping("/uploadIcon")
    @ResponseBody
    public ResponseResult uploadIcon(MultipartFileParamsVO file){

        return fileUploadService.upload(file);
    }

    /**
     * 上传调用
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseResult upload(MultipartFileParamsVO file){

        return fileUploadService.upload(file);
    }

    /**
     * 上传完成调用，进行分片文件合并
     */
    @PostMapping("/merge")
    @ResponseBody
    public ResponseResult uploadSuccess(@RequestBody  UploadFileInfo file,MultipartFile icon){
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        ResponseResult res = fileUploadService.uploadSuccess(file);
        String filePrefix;
        if(res.getCode() == HttpStatus.OK.value()){
            try{
                filePrefix = file.getFilename().substring(file.getFilename().lastIndexOf(".") + 1);
            }catch (StringIndexOutOfBoundsException e){
                log.info("|file: \""+file.getFilename()+"\"| 该文件没有后缀名");
                filePrefix = "null";
            }

            FileDTO newFile = fileMapper.selectOne(new LambdaQueryWrapper<FileDTO>()
                    .eq(FileDTO::getMd5,file.getIdentifier()));
            if(Objects.isNull(newFile)){
                newFile = new FileDTO();
                newFile
                        .setFileSize(file.getTotalSize())
                        .setFilename(file.getFilename())
                        .setMd5(file.getIdentifier())
                        .setCount(0)
                        .setPhysicalPath(uploadFilePath+file.getIdentifier())
                        .setUploadtime(new Timestamp(new Date().getTime()))
                        .setFileType(filePrefix);
                fileMapper.insert(newFile);
            }


            //TODO 等前端是不是icon上传到合并请求里面
            if(Objects.isNull(icon)){
                fileUploadService.setFileIcon(newFile);
            }else{
                String uploadIconPath = uploadFilePath + "icon/"+file.getIdentifier()+".png";
                File iconFile = new File(uploadIconPath);
                if(!iconFile.getParentFile().exists()) iconFile.getParentFile().mkdirs();
                try {
                    icon.transferTo(iconFile);
                    newFile.setIcon(StaticValue.url+"/icon/"+file.getIdentifier()+".png");
                } catch (IOException e) {
                    log.error("|user: "+requestUser.getId()+" |fileMd5:"+file.getIdentifier()+"|icon上传出现错误|");
                    res.setMsg("icon上传出现错误");
                    res.setCode(HttpStatus.NOT_FOUND.value());

                    fileUploadService.setFileIcon(newFile);
                    throw new RuntimeException(e);

                }

            }


            DirectoryDTO parentDirectoryDto = new DirectoryDTO();
            LambdaQueryWrapper<DirectoryDTO> directoryWrapper = new LambdaQueryWrapper<>();




            if(file.getGroupId() == -1){
                fileUploadService.uploadExistUserFile(new MultipartFileParamsVO().setByUploadFileInfo(file),requestUser);

            }else{
                fileUploadService.uploadExistGroupFile(new MultipartFileParamsVO().setByUploadFileInfo(file),requestUser);


            }
        }
        return res;
    }



    @PostMapping("/getNowFileList")
    @ResponseBody
    public ResponseResult getNowUserDirectoryFileList(@RequestBody String absolutePathJSON){

        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        Long groupId = JSONObject.parseObject(absolutePathJSON).getLong("groupId");

        String absolutePath = (String) JSONObject.parseObject(absolutePathJSON).get("absolutePath");

        List<ContentVO> resList = directoryMapper.getDirList(absolutePath, requestUser.getId(),groupId != -1);;

        if(Objects.isNull(groupId) ||groupId == -1){
            resList.addAll(userFileMapper.getUserFileList(absolutePath, requestUser.getId()));


        }else{
            GroupsDTO isGroupUser = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getGroupId, groupId)
                    .eq(GroupsDTO::getUserId,requestUser.getId()));
            if(!Objects.isNull(isGroupUser)){

                resList.addAll(groupFileMapper.getGroupFileList(absolutePath, groupId));

            }

        }


        return  new ResponseResult<List>(HttpStatus.OK.value(), "当前目录下文件",resList);
    }

    @RequestMapping("/auth")
    public void auth(HttpServletRequest request,HttpServletResponse response){
        String url = request.getHeader("X-Original-URI");
        String []splitOfUrl = url.split("/");
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        String token = "download:"+url.substring(url.lastIndexOf("=")+1);
        String authUrl = redisUtil.getCacheObject(token);
        if(Objects.isNull(authUrl)){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        if(authUrl.contains(splitOfUrl[2])){
            response.setStatus(HttpStatus.OK.value());
            log.info("|操作: 文件鉴权 |结果: 成功 |token:"+token+"| md5:"+splitOfUrl[3]+" |");
        }else{
            log.info("|操作: 文件鉴权 |结果: 失败 |token:"+token+"| md5:"+splitOfUrl[3]+" |");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

    }



    @RequestMapping("/download")
    @ResponseBody
    public ResponseResult<String> download(DownloadBasicInfoVo downloadBasicInfo) throws ServletException, IOException {
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();

        Long fileId = downloadBasicInfo.getId();
        Long parentId = downloadBasicInfo.getParentId();
        String filename = downloadBasicInfo.getFilename();
        Long groupId = downloadBasicInfo.getGroupId();
        ResponseResult<String> result = new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件不存在");
        FileDTO realFile;
        String nginxUrl;
        String token;
        if(groupId == -1){
            UserFileDTO userFileDTO = userFileMapper.selectOne(new LambdaQueryWrapper<UserFileDTO>()
                    .eq(UserFileDTO::getId,fileId)
                    .eq(UserFileDTO::getUserId,requestUser.getId()));

            if(Objects.isNull(userFileDTO)) return result;
            realFile = fileMapper.selectOne(new LambdaQueryWrapper<FileDTO>()
                    .eq(FileDTO::getId,userFileDTO.getFileId()));
            if(Objects.isNull(realFile)) return result;



            nginxUrl = "/downloadFile/" + realFile.getMd5()+"/"
                    +realFile.getFilename()+"?renameto="+userFileDTO.getFileName();
        }else{
            GroupFileDTO groupFileDTO = groupFileMapper.selectOne(new LambdaQueryWrapper<GroupFileDTO>()
                    .eq(GroupFileDTO::getId,fileId)
                    .eq(GroupFileDTO::getGroupId,groupId)
                    .eq(GroupFileDTO::getUserId,requestUser.getId()));

            if(Objects.isNull(groupFileDTO)) return result;
            realFile = fileMapper.selectOne(new LambdaQueryWrapper<FileDTO>()
                    .eq(FileDTO::getId,groupFileDTO.getFileId()));
            if(Objects.isNull(realFile)) return result;

            nginxUrl = "/downloadFile/" + realFile.getMd5()+"/"
                    +realFile.getFilename()+"?renameto="+groupFileDTO.getFileName();

            StringBuilder b = new StringBuilder();

        }
        token = UUID.randomUUID().toString();
        nginxUrl += "&token="+token;
        redisUtil.setCacheObject("download:"+token,nginxUrl,1,TimeUnit.DAYS);





        log.info("|操作: 下载请求|"+"|user: "+requestUser.getUsername()+"|文件： "+realFile.getMd5()+"|token: " +token);
        result.setCode(HttpStatus.OK.value());
        result.setMsg("文件存在");
        result.setData(downloadDomain+nginxUrl);

        return result;

    }

    @PostMapping("/moveFiles")
    @ResponseBody
    public ResponseResult moveFiles(@RequestBody String JSONString){


        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<ContentVO> toMoveFiles = JSONArray.parseArray(JSON.toJSONString(JSONObject.parseObject(JSONString)
                .get("files")),ContentVO.class);
        String destPath = JSONObject.parseObject(JSONString).getString("destination");

        Long groupId = JSONObject.parseObject(JSONString).getLong("groupId");
        ResponseResult result = new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件不存在");

        DirectoryDTO destination;
        if(!Objects.isNull(requestUser) && !toMoveFiles.isEmpty()){
            destination = directoryMapper.selectOne(new LambdaQueryWrapper<DirectoryDTO>()
                    .eq(DirectoryDTO::getOwnerId,requestUser.getId())
                    .eq(DirectoryDTO::getAbsolutePath,destPath)
                    .eq(DirectoryDTO::isGroupDirectory,groupId != -1));
            if(Objects.isNull(destination)) return result;


            for(ContentVO tmp : toMoveFiles){
                if(tmp.isDirectoryType()){
                    result = cloudFilesServices.moveDirectory(requestUser,tmp,destination,groupId);
                    if(result.getCode() != HttpStatus.OK.value()) return result;
                }else{
                    result = cloudFilesServices.moveFiles(requestUser,tmp,destination,groupId);
                    if(result.getCode() != HttpStatus.OK.value()) return result;

                }
            }




        }

        return result;
    }

    @PostMapping("/copyFiles")
    @ResponseBody
    public ResponseResult copyFiles(@RequestBody String JSONString){

        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<ContentVO> toCopyFiles = JSONArray.parseArray(JSON.toJSONString(JSONObject.parseObject(JSONString)
                .get("files")),ContentVO.class);

        String destPath = JSONObject.parseObject(JSONString).getString("destination");
        Long groupId = JSONObject.parseObject(JSONString).getLong("groupId");

        ResponseResult result = new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件不存在");

        DirectoryDTO destination;
        if(!Objects.isNull(requestUser) && !toCopyFiles.isEmpty()){
            destination = directoryMapper.selectOne(new LambdaQueryWrapper<DirectoryDTO>()
                    .eq(DirectoryDTO::getOwnerId,requestUser.getId())
                    .eq(DirectoryDTO::getAbsolutePath,destPath)
                    .eq(DirectoryDTO::isGroupDirectory,groupId != -1));
            if(Objects.isNull(destination)) return result;


            for(ContentVO tmp : toCopyFiles){
                if(tmp.isDirectoryType()){
                    result = cloudFilesServices.copyDir(requestUser,tmp,destination,groupId);
                    if(result.getCode() != HttpStatus.OK.value()) return result;
                }else{
                    result = cloudFilesServices.copyFiles(requestUser,tmp,destination,groupId);
                    if(result.getCode() != HttpStatus.OK.value()) return result;

                }
            }




        }

        return result;
    }


    @PostMapping("/deleteFiles")
    @ResponseBody
    public ResponseResult deleteFiles(@RequestBody String JSONString){


        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<ContentVO> toDeleteFiles = JSONArray.parseArray(JSON.toJSONString(JSONObject.parseObject(JSONString)
                .get("files")),ContentVO.class);

        String destPath = JSONObject.parseObject(JSONString).getString("destination");
        Long groupId = JSONObject.parseObject(JSONString).getLong("groupId");

        ResponseResult result = new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "文件不存在");

        DirectoryDTO destination;
        if(!Objects.isNull(requestUser) && !toDeleteFiles.isEmpty()){
            destination = directoryMapper.selectOne(new LambdaQueryWrapper<DirectoryDTO>()
                    .eq(DirectoryDTO::getOwnerId,requestUser.getId())
                    .eq(DirectoryDTO::getAbsolutePath,destPath)
                    .eq(DirectoryDTO::isGroupDirectory,groupId != -1));
            if(Objects.isNull(destination)) return result;


            for(ContentVO tmp : toDeleteFiles){
                if(tmp.isDirectoryType()){
                    result = cloudFilesServices.deleteDir(requestUser,tmp,destination,groupId);
                    if(result.getCode() != HttpStatus.OK.value()) return result;
                }else{
                    result = cloudFilesServices.deleteFiles(requestUser,tmp,destination,groupId);
                    if(result.getCode() != HttpStatus.OK.value()) return result;

                }
            }




        }

        return result;
    }

    @PostMapping("/shareFiles")
    @ResponseBody
    public ResponseResult shareFiles(@RequestBody String JSONString){
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        ResponseResult failed = new ResponseResult(HttpStatus.FORBIDDEN.value(), "分享失败");
        ResponseResult<String> ok = new ResponseResult<String>(HttpStatus.OK.value(), "分享成功");
        String password = JSONObject.parse(JSONString).getString("password");
        List<ContentVO> shareContentList= JSONArray.parseArray(
                JSON.toJSONString(JSONObject.parseObject(JSONString).get("files")), ContentVO.class);
        List<ShareDTO> shareList = shareContentList.stream()
                .map(shareContent -> {return new ShareDTO().setFileOrDirectoryId(shareContent.getId())
                                                            .setDirectory(shareContent.isDirectoryType());})
                .collect(Collectors.toList());
        List<Long> userFileId = shareList.stream()
                            .filter(shareDTO -> !shareDTO.isDirectory())
                            .map(shareDTO -> shareDTO.getFileOrDirectoryId())
                            .collect(Collectors.toList());
        List<Long> directoryId = shareList.stream()
                .filter(shareDTO -> shareDTO.isDirectory())
                .map(shareDTO -> shareDTO.getFileOrDirectoryId())
                .collect(Collectors.toList());

        if(!userFileId.isEmpty()){
            int userFileCount =
                    userFileMapper.selectCount(
                    new LambdaQueryWrapper<UserFileDTO>()
                            .in(UserFileDTO::getId,userFileId)
                            .eq(UserFileDTO::getUserId,requestUser.getId()));
            if(userFileCount != userFileId.size()) return failed;
        }
        if(!directoryId.isEmpty()){
            int directoryCount  =
                    directoryMapper.selectCount(
                            new LambdaQueryWrapper<DirectoryDTO>()
                                    .in(DirectoryDTO::getDirectoryId,directoryId)
                                    .eq(DirectoryDTO::isGroupDirectory,false)
                                    .eq(DirectoryDTO::getOwnerId,requestUser.getId()));
            if(directoryCount != directoryId.size()) return failed;
        }




        //运行速度 for>forEach>stream
        String shareFileUUID = UUID.randomUUID().toString().replaceAll("-","");
        String shareUrl =StaticValue.url+"/share/"+shareFileUUID;
        for(int nowFileCount = 0; nowFileCount < shareList.size();nowFileCount++){

            ShareDTO tmp = shareList.get(nowFileCount);
            tmp.setShareId(shareFileUUID)
                .setPasswords(password)
                .setUrl(shareUrl)
                .setId(null)
                .setUserId(requestUser.getId());
            shareMapper.insert(tmp);
        }
        ok.setData(shareUrl);
        return ok;
    }

    @GetMapping(value = "/isShareFileEncrypted")
    @ResponseBody
    public ResponseResult isShareFileEncrypted(HttpServletRequest request){

        String shareId = request.getParameter("uuid");
        ResponseResult encrypt = new ResponseResult(HttpStatus.FORBIDDEN.value(), "分享已加密");
        ResponseResult<String> ok = new ResponseResult<String>(HttpStatus.OK.value(), "分享未加密");
        ResponseResult<String> notFound = new ResponseResult<String>(HttpStatus.NOT_FOUND.value(), "分享不存在");
        List<ShareDTO> shareList = shareMapper.selectList(new LambdaQueryWrapper<ShareDTO>()
                .eq(ShareDTO::getShareId,shareId));
        if(shareList.isEmpty()) return notFound;
        if(shareList.get(0).getPasswords().isEmpty()) return ok;
        else return encrypt;

    }

    @GetMapping("/share/{uuid}")
    @ResponseBody
    public ResponseResult share(@PathVariable(value = "uuid") String uuid,
                                @RequestParam(value = "password") String password){
        ResponseResult failed = new ResponseResult(HttpStatus.FORBIDDEN.value(), "密码错误");
        ResponseResult<List> ok = new ResponseResult<List>(HttpStatus.OK.value(), "密码正确");
        List<ShareDTO> shareList = shareMapper.selectList(new LambdaQueryWrapper<ShareDTO>()
                .eq(ShareDTO::getShareId,uuid));

        if(!shareList.get(0).getPasswords().equals(password)) return failed;
        List<Long> userFileList = shareList.stream()
                .filter(shareDTO -> !shareDTO.isDirectory())
                .map(shareDTO -> shareDTO.getFileOrDirectoryId())
                .collect(Collectors.toList());
        List<Long> userDirectoryList = shareList.stream()
                .filter(shareDTO -> shareDTO.isDirectory())
                .map(shareDTO -> shareDTO.getFileOrDirectoryId())
                .collect(Collectors.toList());

        List<ContentVO> res = directoryMapper.getDirListById(userDirectoryList);
        res.addAll(userFileMapper.getUserFileListById(userFileList));
        ok.setData(res);
        return ok;




    }
    @RequestMapping("/shareDownloadAuth")
    public void shareDownloadAuth(HttpServletRequest request,HttpServletResponse response){
        String url = request.getHeader("X-Original-URI");
        String []splitOfurl = url.split("/");
        String uuid = splitOfurl[splitOfurl.length-1]
                .substring(0,splitOfurl[splitOfurl.length-1].lastIndexOf('?'));

        String password = splitOfurl[splitOfurl.length-1].substring(
                splitOfurl[splitOfurl.length-1].lastIndexOf("?")+1
        );

        List<ShareDTO> shareList = shareMapper.selectList(new LambdaQueryWrapper<ShareDTO>()
                .eq(ShareDTO::getShareId,uuid));

        if(shareList.isEmpty()){
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

        }else if(shareList.get(0).getPasswords().equals(password)){
            response.setStatus(HttpStatus.OK.value());
            log.info("|操作: 文件分享鉴权 |结果: 成功 |uuid: " + uuid+"|password: "+password+"|");
        }else{
            log.info("|操作: 文件分享鉴权 |结果: 失败 |uuid: " + uuid+"|password: "+password+"|");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @PostMapping("/createDir")
    @ResponseBody
    public ResponseResult createDir(@RequestBody String JSONString){
        ResponseResult ok = new ResponseResult(HttpStatus.OK.value(), "创建成功");
        ResponseResult failed = new ResponseResult(HttpStatus.FORBIDDEN.value(),  "创建失败");
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        String destPath = JSONObject.parseObject(JSONString).getString("destination");
        Long groupId = JSONObject.parseObject(JSONString).getLong("groupId");
        String dirName = JSONObject.parseObject(JSONString).getString("dirName");
        if(dirName.equals("/") || dirName.isEmpty() || destPath.isEmpty()) return failed;
        if(groupId != -1){
            GroupsDTO isUser = groupMapper.selectOne(new LambdaQueryWrapper<GroupsDTO>()
                    .eq(GroupsDTO::getUserId,requestUser.getId())
                    .eq(GroupsDTO::getGroupId,groupId));
            if(Objects.isNull(isUser)) return failed;
        }


        DirectoryDTO parentDir = directoryMapper.selectOne(new LambdaQueryWrapper<DirectoryDTO>()
                .eq(DirectoryDTO::getAbsolutePath,destPath)
                .eq(DirectoryDTO::getOwnerId,(groupId!=-1)?groupId:requestUser.getId())
                .eq(DirectoryDTO::isGroupDirectory,groupId!=-1));


        DirectoryDTO newDir =
                new DirectoryDTO(null,parentDir.getDirectoryId(),(groupId!=-1)?groupId:requestUser.getId(),
                        dirName,destPath+dirName.substring(1),groupId!=-1);

        directoryMapper.insert(newDir);
        return ok;

    }

    @PostMapping("/restorage")
    @ResponseBody
    public ResponseResult storageToSpace(@RequestBody String JSONString){

        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        String destPath = JSONObject.parseObject(JSONString).getString("destination");
        Long destGroupId = JSONObject.parseObject(JSONString).getLong("destGroupId");
        Long sourceGroupId = JSONObject.parseObject(JSONString).getLong("sourceGroupId");
        List<ContentVO> toCopyFiles = JSONArray.parseArray(JSON.toJSONString(JSONObject.parseObject(JSONString)
               .get("files")),ContentVO.class);

        DirectoryDTO destination = directoryMapper.selectOne(new LambdaQueryWrapper<DirectoryDTO>()
                .eq(DirectoryDTO::getAbsolutePath,destPath)
                .eq(DirectoryDTO::isGroupDirectory,destGroupId != -1)
                .eq(DirectoryDTO::getOwnerId,(destGroupId!=-1)?destGroupId:requestUser.getId()));
        ResponseResult res = null;
        for(ContentVO tmp : toCopyFiles){
            res = cloudFilesServices.restorage(requestUser,tmp,destination,sourceGroupId,destGroupId);
            if(res.getCode()!=HttpStatus.OK.value()) return res;
        }
        return res;

    }



    @GetMapping("/searchFiles")
    @ResponseBody
    public ResponseResult searchFile(@RequestParam String fileName,@RequestParam Long groupId){
        ResponseResult<List> ok = new ResponseResult(HttpStatus.OK.value(),"搜索文件");
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        List<ContentVO> res =  directoryMapper.searchDirByName((groupId != -1)?groupId:requestUser.getId(),fileName,groupId != -1);
        if(groupId == -1){
            res.addAll(userFileMapper.searchFilesByName(requestUser.getId(),fileName));
        }

        ok.setData(res);
        return ok;

    }

    @GetMapping("/groupByFileType")
    @ResponseBody
    public ResponseResult groupByFileType(@RequestParam String fileType){
        ResponseResult ok = new ResponseResult(HttpStatus.OK.value(),"文件类别");
        ResponseResult forbidden= new ResponseResult(HttpStatus.FORBIDDEN.value(),"非法请求");
        UserDTO requestUser = securityContextService.getUserFromContext().getUserDTO();
        if(!StaticValue.fileTypeGroup.contains(fileType)) return forbidden;
        List<ContentVO> res = null;
        if(fileType.equals("document")){
            res = userFileMapper.groupByType(requestUser.getId(),StaticValue.documentPrefixList);
        }else if(fileType.equals("video")){
            res = userFileMapper.groupByType(requestUser.getId(),StaticValue.videoPrefixList);
        }else if(fileType.equals("audio")){
            res = userFileMapper.groupByType(requestUser.getId(),StaticValue.audioPrefixList);
        }else if(fileType.equals("picture")){
            res = userFileMapper.groupByType(requestUser.getId(),StaticValue.picturePrefixList);
        }else{
            List<String> allPrefix = new ArrayList<String>();
            allPrefix.addAll(StaticValue.videoPrefixList);
            allPrefix.addAll(StaticValue.picturePrefixList);
            allPrefix.addAll(StaticValue.audioPrefixSet);
            allPrefix.addAll(StaticValue.documentPrefixList);
            res = userFileMapper.groupExcludeType(requestUser.getId(),allPrefix);
        }
        ok.setData(res);
        return ok;
    }

















}

