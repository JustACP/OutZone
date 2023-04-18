package com.outzone.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Slf4j
public class MergeUtil {
    /**
     * 判断上传分片目录是否存在, 不存在就创建
     * @Param filepath 文件路径
     * @return File对象
     * */
    public static File isUploadChunkParentPathExist(String filePath){
        File fileTmp  = new File(filePath);
        File parentFile =  fileTmp.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        return fileTmp;
    }
    /**
     * 合并文件
     * @param uploadPath 上传路径 D:/develop/video/
     * @param chunkPath 分片文件目录路径
     * @param mergePath 合并文件目录D:/develop/video/文件唯一id/merge/
     * @param fileName 文件名
     * @return file
     * */
    public static File mergeFile(String uploadPath,String chunkPath,String mergePath,String fileName){

        File file = new File(chunkPath);
        log.info("开始合并");
        List<File> chunkFileList = chunkFileList(file);
        File fileTemp = new File(mergePath);
        if(!fileTemp.exists()){
            fileTemp.mkdirs();
        }

        //合并文件路径
        File mergeFile = new File(mergePath + fileName);
        if(mergeFile.exists()){
            mergeFile.delete();
        }
        boolean newFile = false;
        try{
            newFile = mergeFile.createNewFile(); //创建文件,已存在返回false;不存在创建文件, 目录不存在跑异常
        }catch (IOException e){
            e.printStackTrace();
        }
        if(!newFile){
            return null;
        }
        try{
            //创建写文件对象
            RandomAccessFile rafWrite = new RandomAccessFile(mergeFile,"rw");
            byte[] bytes = new byte[1024];
            for(File chunkFile : chunkFileList){
                RandomAccessFile rafRead = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while((len = rafRead.read(bytes)) != -1){
                    rafWrite.write(bytes);
                }
                rafRead.close();
                chunkFile.delete();
            }
            file.delete();
            rafWrite.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
        log.info("合并完成");
        return mergeFile;
    }

    public static List<File> chunkFileList(File file){
        //获取目录所有文件
        File[] files = file.listFiles();
        if(files == null){
            return null;
        }
        //转换为list，方便排序
        List<File> chunkFileList = new ArrayList<>();
        chunkFileList.addAll(Arrays.asList(files));
        //排序
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });
        return chunkFileList;
    }
}

