package com.outzone.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class StaticValue {


//    public static String url = "http://172.23.252.223:8085";
    public static String url = "http://file.re1ife.top";

    public static String userIconUploadPath = "/home/re1ife/icon/user/";
    public static String directoryIcon = url + "/icon/file/folder.png";
    public static String[] audio = {"wav", "flac", "ape", "alac", "wavpack", "mp3", "aac", "ogg", "opus"};
    public static String[] media = {"mp4", "wmv", "flv", "avi", "mkv", "rmvb", "mov", "ogv"};
    public static String[] compression = {"zip", "rar", "7z", "gz"};
    public static String[] documents = {"docx", "doc", "ppt", "pptx", "xlx", "xlxs", "pdf","txt"};
    public static String[] picture = {"jpg", "jpeg", "", "png", "gif", "tiff", "webp", "bmp"};
    public static List<String> fileTypeGroup = Arrays.asList("document", "other", "audio", "video", "picture");

    public static ArrayList<String> audioPrefixList = new ArrayList<>(Arrays.asList(audio));
    public static ArrayList<String> picturePrefixList = new ArrayList<>(Arrays.asList(picture));
    public static ArrayList<String> videoPrefixList = new ArrayList<>(Arrays.asList(media));
    public static ArrayList<String> documentPrefixList = new ArrayList<>(Arrays.asList(documents));

    public static HashSet<String> audioPrefixSet = new HashSet<>(Arrays.asList(audio));

    public static HashSet<String> mediaPrefixSet = new HashSet<>(Arrays.asList(media));

    public static HashSet<String> compressionPrefixSet = new HashSet<>(Arrays.asList(compression));

    //2TB
    public static Long userTotalCapacity = 2199023255552L;
}

