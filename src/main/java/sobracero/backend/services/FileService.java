package sobracero.backend.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    void initBucket();
    String uploadImage(MultipartFile file);
    String uploadEncryptedDocument(MultipartFile file);
    byte[] downloadEncryptedDocument(String fileName);
}
