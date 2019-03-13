package cs455.scaling.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Hash {

	public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
		 MessageDigest digest = MessageDigest.getInstance("SHA1");
		 byte[] hash = digest.digest(data);
		 BigInteger hashInt = new BigInteger(1, hash);
		 //Pad the hash with 0's to ensure that every hash is 40 bytes long
		 return String.format("%040x", hashInt);		 
	}
}
