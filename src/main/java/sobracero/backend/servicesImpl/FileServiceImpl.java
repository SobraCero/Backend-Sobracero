package sobracero.backend.servicesImpl;

import sobracero.backend.services.FileService;
import sobracero.backend.services.EncryptionService;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import io.minio.GetObjectArgs;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private EncryptionService encryptionService;

    @Value("${minio.bucket.name}")
    private String bucketName;

    @Value("${minio.bucket.docs:documentos-privados}")
    private String docsBucketName;

    @PostConstruct
    @Override
    public void initBucket() {
        try {
            boolean foundImages = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!foundImages) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            boolean foundDocs = minioClient.bucketExists(BucketExistsArgs.builder().bucket(docsBucketName).build());
            if (!foundDocs) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(docsBucketName).build());
                System.out.println("MinIO: Bucket '" + docsBucketName + "' inicializado como PRIVADO.");
            }

            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": "s3:GetObject",
                          "Resource": "arn:aws:s3:::%s/*"
                        }
                      ]
                    }
                    """.formatted(bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );

            System.out.println("MinIO: Bucket '" + bucketName + "' inicializado y configurado como PÚBLICO.");

        } catch (Exception e) {
            System.err.println("Error inicializando el bucket de MinIO: " + e.getMessage());
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        // --- INICIO VALIDACIÓN IMAGEN ---
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Formato inválido. El archivo subido debe ser una imagen (JPG, PNG, etc.).");
        }
        // --- FIN VALIDACIÓN IMAGEN ---

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir la imagen a MinIO: " + e.getMessage());
        }
    }

    @Override
    public String uploadEncryptedDocument(MultipartFile file) {
        // --- INICIO VALIDACIÓN PDF ---
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new RuntimeException("Formato inválido. El documento adjunto debe ser estrictamente un archivo PDF.");
        }
        // --- FIN VALIDACIÓN PDF ---

        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename() + ".enc";
            byte[] fileBytes = file.getBytes();
            byte[] encryptedBytes = encryptionService.encrypt(fileBytes);

            InputStream inputStream = new ByteArrayInputStream(encryptedBytes);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(docsBucketName)
                            .object(fileName)
                            .stream(inputStream, encryptedBytes.length, -1)
                            .contentType("application/octet-stream")
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Error al subir el documento encriptado a MinIO: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadEncryptedDocument(String fileName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(docsBucketName)
                            .object(fileName)
                            .build()
            );

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] encryptedBytes = buffer.toByteArray();

            return encryptionService.decrypt(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar o desencriptar el documento: " + e.getMessage());
        }
    }
}