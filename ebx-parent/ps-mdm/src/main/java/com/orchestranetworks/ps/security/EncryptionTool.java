package com.orchestranetworks.ps.security;

import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * 
 * @author David Dahan
 *
 */
public class EncryptionTool
{

	public EncryptionTool()
	{
	}

	private final static String getSystemSaltOrDefault(final String defaultSalt)
	{
		return System.getProperties().getProperty("encryptionSalt", defaultSalt);
	}

	private final static Cipher factory(final int mode) throws Exception
	{
		final String username = "ebx5Security@orchestranetworks.com";
		final String password = "NEW KEY H3ll0B0nj0ur!!!";
		final String SALT2 = "HERE IS ANOTHER ONE ilovesaltJ@1m3L3S3l";
		final String SALT = getSystemSaltOrDefault(SALT2 + username + password);

		final byte[] key1 = SALT.getBytes("UTF-8");
		final MessageDigest sha = MessageDigest.getInstance("SHA-1");
		final byte[] key2 = sha.digest(key1);
		final byte[] key3 = Arrays.copyOf(key2, 16); // use only first 128 bit

		final SecretKeySpec aesKey = new SecretKeySpec(key3, "AES");
		final Cipher cipher = Cipher.getInstance("AES");
		// cipher.init(Cipher.ENCRYPT_MODE, aesKey);
		cipher.init(mode, aesKey);
		return cipher;
	}

	public static byte[] encrypt(final String text) throws Exception
	{
		final Cipher cipher = factory(Cipher.ENCRYPT_MODE);
		final byte[] encrypted = cipher.doFinal(text.getBytes());
		return encrypted;
	}

	public static String encryptReturnBase64(final String text) throws Exception
	{
		return base64Encoding(encrypt(text));
	}

	public static String base64Encoding(final byte[] input)
	{
		return new String(Base64.getEncoder().encodeToString(input));
	}

	public static byte[] base64Decoding(final String input)
	{
		return Base64.getDecoder().decode(input);
	}

	public static String decryptFromBase64(final String text)
	{
		Cipher cipher;
		try
		{
			cipher = factory(Cipher.DECRYPT_MODE);
			final byte[] input = base64Decoding(text);
			final byte[] original = cipher.doFinal(input);
			final String originalString = new String(original);
			return originalString;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getPlainText()
	{
		Scanner keyboard = new Scanner(System.in);
		System.out.println("enter your text");
		String theText = keyboard.nextLine();
		keyboard.close();
		return theText;
	}

	public static String getSecureText()
	{
		System.out.println("enter your text (input echoing disabled)");

		final char[] input = System.console().readPassword();
		final String theText = new String(input);

		return theText;
	}

	public static void main(String[] args)
	{
		// System.out.println(args.length);

		if (args.length == 1)
		{
			final String mode = args[0];
			// final String text = args[1];

			if (mode.equals("decrypt"))
			{

				//				try {
				//					final String text = getSecureText();
				////					System.out.println("you asked to decrypt:" + text);
				//					
				//					System.out.println("computing...");
				//					
				//					final String result = new EncryptionTool().decryptFromBase64(text);
				//					System.out.println("result is:");
				//					System.out.println(result);
				//				} catch (Exception e) {
				//					e.printStackTrace();
				//				}

			}
			else
			{
				try
				{
					final String text = getSecureText();
					//					System.out.println("you asked to encrypt:" + text);

					System.out.println("computing...");

					final String result = EncryptionTool.encryptReturnBase64(text);

					System.out.println("result is:");
					System.out.println(result);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
