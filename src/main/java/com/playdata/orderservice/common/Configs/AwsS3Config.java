package com.playdata.orderservice.common.Configs;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

@Component
@Slf4j
public class AwsS3Config {

    // S3 버킷을 제어하는 객체
    private S3Client s3Client;

    @Value("${spring.cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;


    //S3에 연결해서 인증을 처리하는 로직
    @PostConstruct // 클래스를 기반으로 객체가 생성될 때 1번만 실행되는 아노테이션
    private void initializeAmazonS3Client() {

        // accessKey와 serretKey를 이용해 계정 인증 받기
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        // 지역 설정 및 인증 정보를 담은 S3Client 객체를 위의 s3변수에 세팅
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

    }

    /**
     * 버킷에 파일을 업로드하고, 업로드한 버킷의 url 정보를 리턴
     * @param uploadFile - 업로드 할 파일의 실제 raw 데이터
     * @param fileName - 업로드 할 파일명
     * @return - 버킷에 업로드 된 버킷 경로(url)
     */


    public String uploadToS3Bucket(byte[] uploadFile, String fileName) {

        // 업로드 할 파일을 s3의 오브젝트로 생성
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        // 오브젝트를 버킷에 업로드
        s3Client.putObject(request, RequestBody.fromBytes(uploadFile));

        // 업로드 되는 파일의 url을 리턴
        return s3Client.utilities()
                .getUrl(b -> b.bucket(bucketName).key(fileName)).toString();
    }

    // 버킷에 업로드된 이미지를 삭제하는 로직
    // 버킷에 오브젝트를 지우기 위해서는 키값을 줘야 하는데
    // 우리가 가지고 있는 건 키가 아니라 url입니다
    public void deleteFromS3Bucket(String fileName) throws Exception {
        log.info("Deleting file {}", fileName);

        URL url = new URL(fileName);

        // getPath()를 통해 key값 앞에 "/"까지 포함해서 제거.
        String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
        String key = decodingKey.substring(1);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

}
