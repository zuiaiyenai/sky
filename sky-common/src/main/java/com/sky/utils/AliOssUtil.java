package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(byte[] bytes, String objectName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            throw new IllegalArgumentException("OSS对象名称不能为空");
        }

        String normalizedEndpoint = endpoint.startsWith("http://") || endpoint.startsWith("https://")
                ? endpoint
                : "https://" + endpoint;
        OSS ossClient = new OSSClientBuilder().build(normalizedEndpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } finally {
            ossClient.shutdown();
        }

        String endpointHost = normalizedEndpoint.replaceFirst("^https?://", "");
        String url = "https://" + bucketName + "." + endpointHost + "/" + objectName;
        log.info("文件上传成功：{}", url);
        return url;
    }
}
