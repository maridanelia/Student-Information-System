package helpers;

import java.security.MessageDigest;

public class Hasher {
	private static final String HASHING_ALGO = "SHA";
	
	/**
	 * 
	 * @param str
	 * @return returns hash string of str
	 */
	public static String hash(String str){
		try {
			MessageDigest md = MessageDigest.getInstance(HASHING_ALGO);
			md.update(str.getBytes());
			byte [] hash = md.digest();
			return toHexString(hash);
			
		}	catch (Exception e){
			return null;
		}
	}
	
	private static String toHexString(byte [] arr){
		StringBuilder build = new StringBuilder();
		for(byte b:arr){
			build.append(Integer.toHexString(Math.abs(b)));
		}
		return build.toString();
	}
	
}
