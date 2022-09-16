package com.random.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

@Controller
public class RandomController {

    // 图片保存路径
    private final String IMAGE_SAVE_PATH = Config.IMAGE_SAVE_PATH;
    // 随机数
    private final Random R = new Random();
    // 图片地址
    private final String WALLHAVEB_CC = "https://wallhaven.cc/search?q=id%3A1&sorting=random&atleast=1920x1080&order=desc&page=";

    Logger logger = LoggerFactory.getLogger(RandomController.class);

    /**
     * 随机图片接口
     */
    @RequestMapping(value = "/", produces = {MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public void index(HttpServletResponse response){
        try {
            OutputStream outputStream = response.getOutputStream();
            String url = randomImageUrl();
            String fileName = url.substring(url.lastIndexOf("/"));
            File file = new File(IMAGE_SAVE_PATH + fileName);
            InputStream inputStream;
            if(file.exists()){
                inputStream = new FileInputStream(file);
            }else{
                inputStream = new FileInputStream(saveImage(this.getInputStream(url), fileName));
            }

            int len; byte[] b = new byte[4096];
            while ((len = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, len);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/directory")
    public void dir(HttpServletResponse response){
        try {
            PrintWriter writer = response.getWriter();

            if(Config.files.isEmpty()){
                writer.println("is empty!");
                return;
            }
            for(String fileName : Config.files){
                writer.println("<a href='/" + fileName + "' target='_blank' > ");
                writer.println(fileName);
                writer.println(" </a></br>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/{fileName}", produces = {MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public void preview(HttpServletResponse response, @PathVariable String fileName){
        try{
            File file = new File(IMAGE_SAVE_PATH + fileName);
            if(!file.exists()){
                response.getWriter().println("File is not exists!");
                return;
            }
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = response.getOutputStream();
            int len; byte[] b = new byte[4096];
            while ((len = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, len);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取随机的图片路径
     * @return 图片路径
     */
    public String randomImageUrl() throws Exception {
        long t1 = new Date().getTime();
        int page = R.nextInt(100); // 获取1-100的随机页数
        URL url = new URL(WALLHAVEB_CC + page);
        URLConnection conn = url.openConnection();
        conn.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36");
        conn.connect();
        InputStream inputStream = conn.getInputStream();
        StringBuilder tempHtml = new StringBuilder();
        byte[] by = new byte[4096];
        int temp;
        while((temp=inputStream.read(by)) != -1){
            tempHtml.append(new String(by,0, temp));
        }
        String html = tempHtml.toString();
        List<String> list = new ArrayList<>();
        // 通过页面拆去原图地址
        String sp = "<img alt=\"loading\" class=\"lazyload\" data-src=\"";
        for (int i; (i = html.indexOf(sp))>-1; ){
            html = html.substring(i+sp.length());
            String imageUrl = html.substring(0, html.indexOf("\""));
            //String small = imageUrl;//略缩图网址
            String tempUrl = imageUrl.replace("th.", "w.").replace("small", "full");
            String full = tempUrl.substring(0, 31) + "wallhaven-" + tempUrl.substring(31);//原图网址
            html = html.substring(html.indexOf("\""));
            list.add(full);
        }
        inputStream.close();
        logger.debug("randomImageUrl耗时：" + (new Date().getTime() - t1));
        return list.get(R.nextInt(list.size()));
    }

    /**
     * 获取图片数据
     * @param uri 图片网络路径
     * @return 图片数据
     */
    public InputStream getInputStream(String uri) throws Exception{
        long t1 = new Date().getTime();
        URL url = new URL(uri);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36");
        int responseCode = conn.getResponseCode();
        logger.debug("{uri：'" + uri + "'}");
        if(responseCode == 404){ // 如果响应404后缀名不对，应为略缩图后缀名统一为.jpg，而原图可能是.png
            logger.debug("{uri：'" + uri.replace(".jpg", ".png") + "'}");
            URL url2 = new URL(uri.replace(".jpg", ".png"));
            conn = (HttpURLConnection) url2.openConnection();
        }
        logger.debug("getInputStream耗时：" + (new Date().getTime() - t1));
        return conn.getInputStream();
    }

    /**
     * 将图片保存到本地
     * @param inputStream 图片
     * @param fileName 文件名
     * @return 保存路径
     */
    public String saveImage(InputStream inputStream, String fileName) throws Exception{
        long t1 = new Date().getTime();
        String savePath;
        File file = new File(IMAGE_SAVE_PATH + "/" + fileName.substring(0, fileName.lastIndexOf("/")));
        if(!file.exists()){
            file.mkdirs();
        }
        savePath = IMAGE_SAVE_PATH + fileName.replace(".png", ".jpg");
        FileOutputStream outputStream = new FileOutputStream(savePath);
        int len;
        byte[] bytes = new byte[4096];
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        logger.info("图片保存至  ==>  " + savePath);
        logger.debug("saveImage耗时：" + (new Date().getTime() - t1));

        Config.files.add(fileName.substring(1).replace(".png", ".jpg"));
        return savePath;
    }
}
