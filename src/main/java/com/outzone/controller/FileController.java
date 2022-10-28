package com.outzone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/file")
public class FileController {

    private final static String utf8 = "UTF-8";
    @RequestMapping("/upload")
    @ResponseBody
    public void upload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filename = request.getParameter("filename");
        Integer totalChunks = Integer.valueOf(request.getParameter("totalCHunks"));
        Integer chunkNumber = Integer.valueOf(request.getParameter("chunkNumber"));
        MultipartHttpServletRequest params = (MultipartHttpServletRequest) request;
        MultipartFile file = (MultipartFile) params.getFiles("file");
        BufferedInputStream inputStream = (BufferedInputStream) file.getInputStream();
        if(chunkNumber <= 1){

        }




    }




}
