package com.random.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {

    public static String IMAGE_SAVE_PATH;
    public static List<String> files = new ArrayList<>();
    static Logger logger = LoggerFactory.getLogger(Config.class);

    static{
        String soname = System.getProperty("os.name");
        if(soname.startsWith("Windows")){
            IMAGE_SAVE_PATH = "D:/Random/Image/";
        }else{
            IMAGE_SAVE_PATH = "./data";
        }
        logger.info("当前系统为 " + soname + ", 图片将保存到：" + IMAGE_SAVE_PATH);

        try{
            File file = new File(IMAGE_SAVE_PATH);
            if(!file.exists()){
                file.mkdirs();
            }
            File[] arr = file.listFiles();
            for(File f : arr){
                files.add(f.getName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
