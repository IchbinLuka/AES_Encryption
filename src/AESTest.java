import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

class AESTEST{

    public static void main(String[] args) throws Exception {
        
        Cipher cipher1 = Cipher.getInstance("AES/CBC/NoPadding");
        Cipher cipher2 = Cipher.getInstance("AES/CBC/NoPadding");

        System.out.println(new String("aaaaaaaaaaaaaaaa".getBytes()));

        Key key = new SecretKeySpec("aaaaaaaaaaaaaaaa".getBytes(), "AES");
        Key key2 = new SecretKeySpec("bbbbbbbbbbbbbbbb".getBytes(), "AES");

        cipher1.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
        cipher2.init(Cipher.DECRYPT_MODE, key2, new IvParameterSpec(new byte[16]));

        SecureRandom secureRandom = new SecureRandom();

        byte[] data = new byte[32]; 
        
        secureRandom.nextBytes(data);

        System.out.println(new String(data));

        byte[] encData = cipher1.doFinal(data);
        byte[] decData = cipher2.doFinal(encData);

        System.out.println(new String(decData));

    }
}