package com.outzone.pojo;

import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class StaticValue {



    public static String url = "http://172.23.252.223:8085";
    public static String directoryIcon = url+"/icon/folder.png";
    public static String []audio = {"wav","flac", "ape","alac","wavpack","mp3","aac","ogg","opus"};
    public static String []media = {"mp4","wmv", "flv","avi","mkv","rmvb","mov","ogv"};
    public static String []compression = {"zip","rar", "7z","gz"};
    public static String []documents = {"docx","doc","ppt","pptx","xlx","xlxs"};

    public static HashSet<String> audioPrefix = new HashSet<>(Arrays.asList(audio));

    public static HashSet<String> mediaPrefix = new HashSet<>(Arrays.asList(media));

    public static HashSet<String> compressionPrefix = new HashSet<>(Arrays.asList(compression));
}
