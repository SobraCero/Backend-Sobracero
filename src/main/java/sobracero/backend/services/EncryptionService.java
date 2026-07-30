package sobracero.backend.services;

public interface EncryptionService {
    byte[] encrypt(byte[] data) throws Exception;
    byte[] decrypt(byte[] encryptedData) throws Exception;
}
