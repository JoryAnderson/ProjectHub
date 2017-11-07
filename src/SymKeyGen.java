import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/*
 * Question 3: You should use symmetric encryption for 
 * confidentiality, as it is much faster than asymmetric crypto. 
 * I recommend AES - but you can use a different cipher. 
 * I suggest to create a new symmetric session key for 
 * each new session. This can be done on the client. 
 * The key can be sent to the server using 
 * asymmetric crypto (encrypted with the server's public key).
 * Question 5: Yes, AES works only with symmetrical keys. 
 * Use DES or another asymmetrical cipher for establishing 
 * the symmetrical session key 
 * and ensuring integrity in that process.
 */

public class SymKeyGen {
	
	static int SUB_KEY_SIZE = 128 / 8;
	static int MASTER_KEY_SIZE = 256 / 8;
	static String ALGO = "AES";
	static String FULL_ALGO = "AES/CBC/PKCS5Padding";
	static int NUM_BYTES_IV = 16;
	
	static byte[] generateMasterKey(){
		
		SecretKey master = generateKey(MASTER_KEY_SIZE * 8, ALGO);
		byte[] mKey = master.getEncoded();
		return mKey;
	}
	
	static byte[][] splitMasterKey(byte[] master){
		
		byte[][] subKeys = new byte[2][SUB_KEY_SIZE];
		subKeys[0] = Arrays.copyOfRange(master, 0, SUB_KEY_SIZE);
		subKeys[1] = Arrays.copyOfRange(master, SUB_KEY_SIZE, master.length);
		assert subKeys[0].length == SUB_KEY_SIZE && subKeys[1].length == SUB_KEY_SIZE;

		return subKeys;
	}
	
	static SecretKey[] convertKeyBytes(byte[][] subKeys){

		return new SecretKey[]{new SecretKeySpec(subKeys[0],ALGO),
							new SecretKeySpec(subKeys[1],ALGO)};
	}
	
	public static SecretKey convertKeyBytes(byte[] master){
		return new SecretKeySpec(master,ALGO);
	}
	
	
	static SecretKey generateKey(int keySize, String algo){
		KeyGenerator gen = null;
		SecretKey key = null;
		try {
			gen = KeyGenerator.getInstance(algo);
			gen.init(keySize);
			key = gen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	static IvParameterSpec generateInitVector(){
		SecureRandom rngIV = new SecureRandom();
		byte[] iv = new byte[NUM_BYTES_IV];
		rngIV.nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	static String encode64(byte[] arr){
		return Base64.encode(arr);
	}
	
	static byte[] decode64(String arr){
		return Base64.decode(arr);
	}
	
	static byte[] encrypt(String msg, SecretKey key, byte[] iv){
		SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), ALGO);
		Cipher cipher;
		byte[] encryptedMessage = null;
		
		try {
			cipher = Cipher.getInstance(FULL_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv)); 
			encryptedMessage = cipher.doFinal(msg.getBytes()); 
			//System.out.println("Encrypted bytes ["+ encryptedMessage.length +"]:" + SymKeyGen.encode64(encryptedMessage));
			
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Symmetric Encryption: Exception!");
			e.printStackTrace();
		}
		return encryptedMessage;
	}

	static String decrypt(byte[] eMsg, SecretKey key, byte[] iv){
		SecretKeySpec sessionKeySpec = new SecretKeySpec(key.getEncoded(), ALGO);
		Cipher cipher;
		String msg = null;
		
		try {
			cipher = Cipher.getInstance(FULL_ALGO);			
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, sessionKeySpec, ivspec); 
			byte[] decryptedMessageBytes = cipher.doFinal(eMsg); 
			msg = new String(decryptedMessageBytes);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Symmetric Decryption: Exception!");
			e.printStackTrace();
		}
		
		return msg;
		
	}
	
	public static byte[] generateSalt(int bytes){
		byte[] salt = new byte[bytes];
	    SecureRandom secureRandom = new SecureRandom();
	    secureRandom.nextBytes(salt);
		return salt;
	}
	
}


