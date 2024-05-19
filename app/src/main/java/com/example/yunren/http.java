package com.example.yunren;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class http {
    public static String sendPostRequest(String urlString, String postData) throws IOException {
        // 创建 URL 对象
        URL url = new URL(urlString);
        // 创建 HttpURLConnection 对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 设置请求方法为 POST
        connection.setRequestMethod("POST");
        // 允许输出
        connection.setDoOutput(true);

        // 设置请求头
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");

        // 构建 POST 数据
        byte[] postDataBytes = postData.getBytes("UTF-8");

        // 获取输出流，并写入 POST 数据
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(postDataBytes);
        outputStream.flush();
        outputStream.close();

        // 获取响应状态码
        int responseCode = connection.getResponseCode();

        // 读取响应内容
        BufferedReader reader;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // 关闭连接
        connection.disconnect();

        // 返回响应内容
        return response.toString();
    }


}
