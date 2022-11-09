package com.outzone.controller;

import com.outzone.entity.MultipartFileParams;
import com.outzone.entity.ResponseResult;
import com.outzone.entity.UploadFileInfo;
import com.outzone.service.FileUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@Controller
@RequestMapping("/file")
public class FileController {

    private final static String utf8 = "UTF-8";

    @Resource
    private FileUploadService fileUploadService;

    /**
     * 上传前调用(只调一次)，判断文件是否已经被上传完成，如果是，跳过，
     * 如果不是，判断是否传了一半，如果是，将缺失的分片编号返回，让前端传输缺失的分片即可
     */
    @GetMapping("/checkFile")
    @ResponseBody
    public ResponseResult checkFile (@RequestBody  String JSONString) throws IOException {

        return fileUploadService.checkFile(JSONString);
    }


    /**
     * 上传调用
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseResult upload(MultipartFileParams file){

        return fileUploadService.upload(file);
    }

    /**
     * 上传完成调用，进行分片文件合并
     */
    @PostMapping("/mergeQQ")
    @ResponseBody
    public ResponseResult uploadSuccess(UploadFileInfo file){
        return fileUploadService.uploadSuccess(file);
    }
//    @RequestMapping("/upload1")
//    @ResponseBody
//    public ResponseResult uploadSimple(HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
//
//        String path = "/home/re1ife/netdisk";
//        Integer currentChunk = null;
//        Integer totalChunks = null;
//        String filename = null;
//        String uploadPath = "/home/re1ife/netdisk";
//        String tmpFilePath = path+"/tmp";
//
//        BufferedOutputStream outputStream = null;
//        CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
//        MultipartHttpServletRequest mulitPartRequest = null;
//
//
//        if(resolver.isMultipart(request)){
//
//            currentChunk = Integer.valueOf(request.getParameter("chunkNumber"));
//            totalChunks = Integer.valueOf(request.getParameter("totalChunks"));
//            filename = request.getParameter("filename");
//            mulitPartRequest = resolver.resolveMultipart(request);
//            Iterator<String> iterator = mulitPartRequest.getFileNames();
//
//
//
//            while(iterator.hasNext()){
//
//                MultipartFile chunkMultiFile = mulitPartRequest.getFile(iterator.toString());
//
//                System.out.println(iterator.toString());
//                String tmpFileName = currentChunk+"_"+filename;
//
//                if(currentChunk != null){
//                    File nowChunkFile = new File(tmpFilePath+"_"+tmpFileName);
//                    if(!nowChunkFile.exists()){
//                        chunkMultiFile.transferTo(nowChunkFile);
//                        System.out.println("write success");
////                        outputStream = new BufferedOutputStream(new FileOutputStream(nowChunkFile));
////                        byte[] bytes = chunkMultiFile.getBytes();
////                        outputStream.write(bytes);
////                        outputStream.flush();
//                    }
//                }
//
//
//
//            }
//            //开始合并
//            if(currentChunk != null & currentChunk == totalChunks){
//
//                File FileToWrite = new File(tmpFilePath+"/"+filename);
//                outputStream  = new BufferedOutputStream(new FileOutputStream(FileToWrite));
//                for(int i = 1; i <= totalChunks;i++){
//                    File nowChunkFile = new File(tmpFilePath,i+"_"+filename);
//                    while(!nowChunkFile.exists()){
//                        Thread.sleep(100);
//                    }
//                    byte[] chunkFileBytes = FileUtils.readFileToByteArray(nowChunkFile);
//                    outputStream.write(chunkFileBytes);
//                    outputStream.flush();
//                    outputStream.close();
//                    nowChunkFile.delete();
//                }
//                return new ResponseResult(HttpStatus.OK.value(),"上传成功" + filename);
//            }
//
//
//
//
//        }
//
//
//        return new ResponseResult(HttpStatus.NOT_FOUND.value(), "上传失败");
//
//
//    }
//
//    @RequestMapping("/upload")
//    @ResponseBody
//    public ResponseResult upload(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//        //文件分片
//        response.setCharacterEncoding(utf8);
//        Integer currentChunk = null;
//        Integer totalChunk = null;
//        String filename = null;
//        String uploadPath = "/home/re1ife/netdisk";
//        String tmpFilePath = uploadPath+"/tmp";
//
//        BufferedOutputStream os = null;
//        try{
//
//            DiskFileItemFactory factory = new DiskFileItemFactory();
//            //内存缓冲区
//            factory.setSizeThreshold(1024);
//            //临时目录
//            factory.setRepository(new File(uploadPath));
//            //解析上传request
//            ServletFileUpload upload = new ServletFileUpload(factory);
//
//            //单个分片最大
//            upload.setFileSizeMax(5L * 1024L * 1024L * 1024L);
//            //总文件最大值
//            upload.setSizeMax(10L * 1024L * 1024L * 1024L);
//            List<FileItem> items = upload.parseRequest(request);
//            for(FileItem item : items){
//
//                //如果是表单域
//                if(item.isFormField()){
//                    if (item.getFieldName().equals("chunk")){
//                        currentChunk = Integer.valueOf(item.getString(utf8));
//                    }
//                    if (item.getFieldName().equals("chunks")){
//                        totalChunk = Integer.valueOf(item.getString(utf8));
//                    }
//                    if (item.getFieldName().equals("chunk")){
//                        currentChunk = Integer.valueOf(item.getString(utf8));
//                    }
//                    if (item.getFieldName().equals("name")){
//                        filename = item.getString(utf8);
//                    }
//
//
//                }
//            }
//
//            for(FileItem item : items){
//                String tmpFileName = filename;
//                //如果是表单域
//                if(!item.isFormField()){
//
//                    if(filename != null){
//                        if(currentChunk != null){
//                            tmpFileName = currentChunk +"_" +filename;
//                        }
//                        File tmpFile = new File(tmpFilePath,tmpFileName);
//                        //断点续传 看看是不是已经存在了
//                        if(!tmpFile.exists()){
//
//                            item.write(tmpFile);
//                        }
//                    }
//                }
//            }
//            //文件 合并
//            if(currentChunk != null & currentChunk == totalChunk - 1){
//                File FileToWrite = new File(tmpFilePath,filename);
//                os = new BufferedOutputStream(new FileOutputStream(FileToWrite));
//                for(int i = 0; i < totalChunk;i++){
//                    File chunkFile = new File(tmpFilePath,i+"_"+filename);
//                    while(! chunkFile.exists()){
//                        Thread.sleep(100);
//                    }
//                    byte[] bytes = FileUtils.readFileToByteArray(chunkFile);
//                    os.write(bytes);
//                    os.flush();
//                    chunkFile.delete();
//
//                }
//                os.flush();
//            }
//            return new ResponseResult(HttpStatus.OK.value(),"上传成功" + filename);
//        } catch (FileUploadException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            try{
//                if(os != null){
//                    os.close();
//                }
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//
//
//        }
//

//
//    }




}
