package com.atguigu.gmall.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

public class MyFileUpload {

    public static String uploadImage(MultipartFile multipartFile)  {
        String url = "http://192.168.119.128";
        //获取tracker的路径
        String TrackerPath = MyFileUpload.class.getClassLoader().getResource("tracker.properties").getPath();

       try{

           //全局配置
           ClientGlobal.init(TrackerPath);

        //获取客户端
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer connection = trackerClient.getConnection();
        StorageClient storageClient = new StorageClient();

        //上传文件
        String originalFilename = multipartFile.getOriginalFilename();
        int i = originalFilename.lastIndexOf(".");
        String substring = originalFilename.substring(i + 1);
        String[] paths = storageClient.upload_file(multipartFile.getBytes(), substring,null);

        for (String path : paths) {
            url = url + "/" + path;


        }
       }catch (Exception e){
           //处理上传异常事件

       }finally {
           return url;
       }
    }



}
