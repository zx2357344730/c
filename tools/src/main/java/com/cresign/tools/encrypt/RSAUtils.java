package com.cresign.tools.encrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * author : jackson
 * RSA公钥/私钥/签名工具包
 */
public class RSAUtils {

	/** */
	/**
	 * 加密算法RSA
	 */
	public static final String KEY_ALGORITHM = "RSA";

	/** */
	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	/** */
	/**
	 * 获取公钥的key
	 */
	private static final String PUBLIC_KEY = "RSAPublicKey";

	/** */
	/**
	 * 获取私钥的key
	 */
	private static final String PRIVATE_KEY = "RSAPrivateKey";

	/** */
	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 245;

	/** */
	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 256;

	/** */
	/**
	 * RSA 位数 如果采用2048 上面最大加密和最大解密则须填写:  245 256
	 */
	private static final int INITIALIZE_LENGTH = 2048;

	private static String publicKey;
	private static String privateKey;
	public static String getPublicKey(){
		return publicKey;
	}
	public static String getPrivateKey(){
		return privateKey;
	}

	static {
		try {
			Map<String, Object> keyMap = genKeyPair();
			publicKey = getPublicKey(keyMap);
			privateKey = getPrivateKey(keyMap);
		} catch (Exception e) {
			System.out.println("获取加密出现错误!");
		}
	}

	/** */
	/**
	 * <p>
	 * 生成密钥对(公钥和私钥)
	 * </p>
	 *
	 * ##return:
	 * ##exception:
	 */
	public static Map<String, Object> genKeyPair() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(INITIALIZE_LENGTH);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/** */
	/**
	 * <p>
	 * 用私钥对信息生成数字签名
	 * </p>
	 *
	 * ##Params: data
	 *            已加密数据
	 * ##Params: privateKey
	 *            私钥(BASE64编码)
	 *
	 * ##return:
	 * ##exception:
	 */
	public static String sign(byte[] data, String privateKey) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateK);
		signature.update(data);
		return Base64.encodeBase64String(signature.sign());
	}

	/** */
	/**
	 * <p>
	 * 校验数字签名
	 * </p>
	 *
	 * ##Params: data
	 *            已加密数据
	 * ##Params: publicKey
	 *            公钥(BASE64编码)
	 * ##Params: sign
	 *            数字签名
	 *
	 * ##return:
	 * ##exception:
	 *
	 */
	public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PublicKey publicK = keyFactory.generatePublic(keySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(Base64.decodeBase64(sign));
	}

	/** */
	/**
	 * <P>
	 * 私钥解密
	 * </p>
	 *
	 * ##Params: encryptedData
	 *            已加密数据
	 * ##Params: privateKey
	 *            私钥(BASE64编码)
	 * ##return:
	 * ##exception:
	 */
	public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws Exception {

		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateK);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/** */
	/**
	 * <p>
	 * 公钥解密
	 * </p>
	 *
	 * ##Params: encryptedData
	 *            已加密数据
	 * ##Params: publicKey
	 *            公钥(BASE64编码)
	 * ##return:
	 * ##exception:
	 */
	public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(publicKey);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicK = keyFactory.generatePublic(x509KeySpec);
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicK);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/** */
	/**
	 * <p>
	 * 公钥加密
	 * </p>
	 *
	 * ##Params: data
	 *            源数据
	 * ##Params: publicKey
	 *            公钥(BASE64编码)
	 * ##return:
	 * ##exception:
	 */
	public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(publicKey);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicK = keyFactory.generatePublic(x509KeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/** */
	/**
	 * <p>
	 * 私钥加密
	 * </p>
	 *
	 * ##Params: data
	 *            源数据
	 * ##Params: privateKey
	 *            私钥(BASE64编码)
	 * ##return:
	 * ##exception:
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/** */
	/**
	 * <p>
	 * 获取私钥
	 * </p>
	 *
	 * ##Params: keyMap
	 *            密钥对
	 * ##return:
	 * ##exception:
	 */
	public static String getPrivateKey(Map<String, Object> keyMap) throws Exception {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		return Base64.encodeBase64String(key.getEncoded());
	}

	/** */
	/**
	 * <p>
	 * 获取公钥
	 * </p>
	 *
	 * ##Params: keyMap
	 *            密钥对
	 * ##return:
	 * ##exception:
	 */
	public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
		Key key = (Key) keyMap.get(PUBLIC_KEY);
		return Base64.encodeBase64String(key.getEncoded());
	}

	/**
	 * java端公钥加密
	 */
	public static String encryptedDataOnJava(String data, String PUBLICKEY) {
		try {
			data = Base64.encodeBase64String(encryptByPublicKey(data.getBytes(), PUBLICKEY));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * java端私钥解密
	 */
	public static String decryptDataOnJava(String data, String PRIVATEKEY) {
		System.out.println("PRIVATEKEY:");
		System.out.println(PRIVATEKEY);
		String temp = "";
		try {
			byte[] rs = Base64.decodeBase64(data);
			temp = new String(RSAUtils.decryptByPrivateKey(rs, PRIVATEKEY),"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}

//	public static void main(String[] args) throws  Exception{
//		Map<String, Object> stringObjectMap = RSAUtils.genKeyPair();
//		(getPrivateKey(stringObjectMap));
//		(getPublicKey(stringObjectMap));
//
//		String privateKey1 = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCMzqqZ3w9aJE2hn4X7dawkf2wZ3BMgbrFmbGJxWoZYjQD5En0oDd6polA4RQgUC7vKLDifdfXMzAzC6EWadW5P17HWj+FlzJjwqoa8MidpNiCKqb4uoxh8DV5gZtHxDDCkyiChdnU7d4euYSJ8dpq4TTbOD6cLcQJyZRwsk9um5/76xZHFC4CPvRMVFR1wY01sGYpadtEMm8EvcJX0mnpYAgtk7PqljTx/7w1DeQcIb+Q+7MeYGrgLXxeppyTl8zUSRAgdXiy1ZHpnLGBb1Dkt0TatsNofxSi4vr2E8zPn0HuPr2/R8mNFgIhDJpOPu2EiGusuzaFBtkze17EvfvsZAgMBAAECggEAfHy2vcVE2GgiLlNP7Cpz3+y70P0N9+2Fb81BF4B2KFG5W3uqJUBl7EmxbJ4zby2UgECqauiqQL7iRWt/JfjOZ06GTvDz0MGbMaNB7Z7V0yHkkVNS7f6Jxhs3pjkhQWCKnRaK1NVa/hFGZrg/+hQN5AV3/6Q9BlOKW0LjLtImLx+5ldC1uShtTaK5QhuTYTVRRGK3Hd7rDx7WjFOX5I99fGOZH1FCc3sSw/mz7gEDLJdu01kAJNjmNK4n/DJBn5VGocXFskEB1RQU9H2YrgscFlR+BeqGoU+fyU46rK4fhAuZo/Ot2v3VvAxP0I5ByYcD6nT3IWWFWXZxMRBExEF29QKBgQDR/i5KE9OsDW0QymrAodvop0edLmXZF0+GkJKe2f0FrLLRnfMav/bVY/smRwa8yZ4CVRJ6x4VxVL81/1U62nsPol0VsVAuIs3Vvmox34e7lWxSxu2o3iB4iYteupDY1MpAKsyhSgqDbTChA03AzXvQ5oxFCnrsHCkZPeugUiJciwKBgQCrqBcCpxYXDFS0wF+yG29XwP1Mbqd1zvubBJ6daFpnUnLRMT0WLBZ6n+KZxyB2aY9HhHQscDBrdAs0BCbK5GT5bkZAdA63+PA+9N/U+0E6UgxIyoydhLh7UeN5dtORsCI7aeq/TXCfIX30qxd8gGLAy99NX1paNIRpKswUBp+HawKBgAHoD92GTo6qVIopfEyha5CyLBlGOZB+AK/VEi+1BPIPPlRe3b0pbwFC9h3D2VDPd8rHoPJSa2i6/z1rQwfUwfdDXxr6UCMLF9lP8EDhqabPVXtu6Ot2kM7nWMeUZApD7E4m1VLFjFDjJeTgVc1hUDV2UmGyFamdG9Zp1IDRHhS/AoGAUWPX1TgoLzF3kGSOe0Kq3m7xKyZkXGWRwwTDuFrcUpUzP6EbF8I7vlTE+qkB6WNPvlc9RjzDmI9jKHy3cAgvIF17DeB5gh2UvCUbZD7lxKNWk8UGO0HMvxwqwVLoKfHbqVQlLCOpvKoCsNnWvv+Y01VXQbZufsrb5YmXTf998JUCgYASJZ/rlM/aareySHQvGC2WFOjwyRtgy+w3yxvX+4rnlITkk8wpbs7Hb6sVijNqsosVTsDlG5lcPUep4ReDclixDPOyXU5ENIVpMmoBXXdPUr5PQ4I2smFUZXAui7gpEsXHkIiY3ClG5KI1K/KhRqTAuB1x31mawhs8nvCRijlRmw==";
//		String publicKey1="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjM6qmd8PWiRNoZ+F+3WsJH9sGdwTIG6xZmxicVqGWI0A+RJ9KA3eqaJQOEUIFAu7yiw4n3X1zMwMwuhFmnVuT9ex1o/hZcyY8KqGvDInaTYgiqm+LqMYfA1eYGbR8QwwpMogoXZ1O3eHrmEifHaauE02zg+nC3ECcmUcLJPbpuf++sWRxQuAj70TFRUdcGNNbBmKWnbRDJvBL3CV9Jp6WAILZOz6pY08f+8NQ3kHCG/kPuzHmBq4C18Xqack5fM1EkQIHV4stWR6ZyxgW9Q5LdE2rbDaH8UouL69hPMz59B7j69v0fJjRYCIQyaTj7thIhrrLs2hQbZM3texL377GQIDAQAB";
////
////		String privateKey2 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWGdO6C5Ypk++ZOEF1Krb3k6E5yij324N1aQUb4oeEQY0xpkdEptwlvm2rxsxLZdkRxqY3A0x5m9uxTkxgNrWsziHR9p27s3sCjo0p6SsIVsxeV0Xjc1a2RH4XQMWl2FcsN4al4J9K8L23hGKR4WjKxX+H4+LjG74nD9b2rUHiXTIO+hbnvmmp/AURCafjk4zNiepLS1lBX709+1KFvZlkaXfTWUM4RTM1oanKeXDQN5wdJESUmYSm170rz7cpb8OzlFwvMlwUWIvSo+S5D0HXfO2yT21Yuf4K8yzZnVnFllrpMPhfQ9S0ErnpL8T8rMmfgsT4HbX7DYuQEolNF6ynAgMBAAECggEAQvsBdUsIVkNyrt/greVCwpX5Yy1oPLqTHgigRAJBUO5jkF1P/j2rjZg9nmLW9WpchEcuEuyA6t93GNcE1AZ16cgOZYB0gLcl1E1ChwpKrqz1A8BUKpxkfG9yCPRZfplcxRl+F3V1/94P+t2zkr+QoLOjJkO2EnxlzU6PMYK9HMKkuejp3y5nWEJitFmURzQEvkcemMs+AtyzSOJiSnV8O8NwURblcI2U8BAVTD+nWBsE22awMXDN5k0fehubBRDJOuyF+Stf4QqaviXOC2uAdLm1WN2YokMHLveZh399b1igF5EbWaomFizlDMpQoOTrldq3eR6Ht9RotTkHb+j1IQKBgQDJUfuozmC4hdPVwuLMN6MgVWA/W1pVKvGYUxq5TOqC0C5m12n59Dh94ezDtNPJ2TdS4Ztsa6UvYwrXoU1oqHnn61oQa8pXMPgThn4r227SUc9bR+SE8knaEztMvcBSj9S8ZBzkpKU7h/EXlGEdXjS6MGhG55Al0/7CTWnI0CxvcQKBgQC+3oGEhCznQleEGGSrtl7ykowjcMdiqTR3GajfEY7HDNLaiCqCNoiqCbQHbJspAI6RZNIW7mAI5+5I8f5lpHm/Nntc06rjPozp+6liQ5oL693SQnmrut6rMZ3fbxe/hOf5Sxe15ponVlmugGfo4l/3Udigkqq9iXlHNfx5gVqBlwKBgEx3vGww/6H3CzBtlvHUzDxS0X0fON+SsTOXlVX+9jB73LQpbFKJxg9iiikH/U31GMN9eCildpfaOdsPpLR1EeDaj1ofZzrZGdFoy3HjJmPyuR1F4HHzCcQwe9y5UlzJzxdDu4nJVA2ZpCS9smmSR9rU2jbca+9CZ8jr2JtzHv2xAoGAPCoDBXRsexEMaRUPVpkL4MaU6e74yn6vjQNHmdj8+n1uUXgufhEWjxKz7ssrNYd5aRcwqg3Fs8j38YJmQD/SKJXR2PbXnP3wiSjMwa6xdiSJLWfK1nj3C1t+ehmYkMQiIxCqjqn72X58kmbjWiNRQLJJ6IOS0l/tHKdFyD8f+k0CgYEArU6u2kDPUfXOLEeQvW+jzD9um+aVQct9VHVQLpkhWSbBRAPpDlhxPW0Fxpq36K7E7exgU5a42mT4D8ULwbo+Gtpfr5MPEPbxkwkVtfKbBIpUS7I4dALkEePY3qltSxdwvniIoGCuaJbMFVXNWg8koWJZSUaZyjGG3dmZEfZh45s=";
////		String publicKey2 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlhnTuguWKZPvmThBdSq295OhOcoo99uDdWkFG+KHhEGNMaZHRKbcJb5tq8bMS2XZEcamNwNMeZvbsU5MYDa1rM4h0fadu7N7Ao6NKekrCFbMXldF43NWtkR+F0DFpdhXLDeGpeCfSvC9t4RikeFoysV/h+Pi4xu+Jw/W9q1B4l0yDvoW575pqfwFEQmn45OMzYnqS0tZQV+9PftShb2ZZGl301lDOEUzNaGpynlw0DecHSRElJmEpte9K8+3KW/Ds5RcLzJcFFiL0qPkuQ9B13ztsk9tWLn+CvMs2Z1ZxZZa6TD4X0PUtBK56S/E/KzJn4LE+B21+w2LkBKJTRespwIDAQAB";
//////		String data = encryptedDataOnJava("asa", publicKey);
//////		String java = decryptDataOnJava(data, privateKey);
////
//		String data = encryptedDataOnJava("bbbb", publicKey1);
//		String java = decryptDataOnJava(data, privateKey1);
//		(java);
//
////		genKeyPair();
//	}


}