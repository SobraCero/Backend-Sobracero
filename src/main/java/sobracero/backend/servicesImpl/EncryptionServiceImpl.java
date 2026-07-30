package sobracero.backend.servicesImpl;

import sobracero.backend.services.EncryptionService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;
import java.security.MessageDigest;


@Service
public class EncryptionServiceImpl implements EncryptionService {

    @Value("${encryption.secret.key:SobraCeroSecretKey123!_@123456789}")
    private String secretKeyString;

    private static final String ALGORITHM = "AES";

    private Key generateKey() throws Exception {
        byte[] key = secretKeyString.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, ALGORITHM);
    }

    @Override
    public byte[] encrypt(byte[] data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(data);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(encryptedData);
    }
}
