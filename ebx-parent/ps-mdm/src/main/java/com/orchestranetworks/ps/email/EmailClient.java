package com.orchestranetworks.ps.email;

import java.util.*;

import com.orchestranetworks.service.*;

import jakarta.mail.*;
import jakarta.mail.Message.*;
import jakarta.mail.Session;
import jakarta.mail.internet.*;

public class EmailClient
{
	private String serverUrl;
	private int port;
	private String serverUser;
	private String serverPassword;

	private String emailCCs;
	private String emailTos;
	private String emailFrom;

	private boolean usingTLS = false;
	private boolean usingSSL = false;

	public void sendMail(String subjectString, String bodyString)
	{
		try
		{

			String serverAddress = getServerUrl();
			int serverPort = getPort();
			String serverUser = getServerUser();
			String serverPassword = getServerPassword();

			Properties prop = System.getProperties();
			prop.put("mail.smtp.host", serverAddress);
			prop.put("mail.smtp.port", String.valueOf(serverPort));

			if (isUsingSSL())
			{
				prop.put("mail.smtp.ssl.enable", "true");
			}
			else if (isUsingTLS())
			{
				prop.put("mail.smtp.starttls.enable", "true");
			}
			else
			{
				prop.put("mail.smtp.auth", "true");
			}

			Session session = Session.getInstance(prop, null);

			Message msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(getEmailFrom()));

			setRecipients(msg, getEmailCCs(), Message.RecipientType.CC);

			setRecipients(msg, getEmailTos(), Message.RecipientType.TO);

			msg.setSubject(subjectString);

			msg.setText(bodyString);

			Transport.send(msg, serverUser, serverPassword);
		}
		catch (MessagingException e)
		{
			LoggingCategory.getKernel().error("Error sending an email:", e);
		}
	}

	private void setRecipients(Message msg, String emailRecipients, RecipientType recipeintType)
	{
		if (emailRecipients == null)
		{
			return;
		}

		try
		{
			msg.setRecipients(recipeintType, InternetAddress.parse(emailRecipients, false));
		}
		catch (Exception e)
		{
			throw new RuntimeException(
				"Error setting recipients for: " + recipeintType.toString(),
				e);
		}

	}

	/**
	 * @return a comma delimited email addresses
	 */
	public String getEmailTos()
	{
		return emailTos;
	}

	/**
	 * @return a comma delimited email addresses
	 */
	public String getEmailCCs()
	{
		return emailCCs;
	}

	/**
	 * @return a single email address identifying the sender
	 */
	public String getEmailFrom()
	{
		return emailFrom;
	}

	public void setEmailCCs(String emailCCs)
	{
		this.emailCCs = emailCCs;
	}

	public void setEmailTos(String emailTos)
	{
		this.emailTos = emailTos;
	}

	public void setEmailFrom(String emailFrom)
	{
		this.emailFrom = emailFrom;
	}

	public String getServerUrl()
	{
		return serverUrl;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getServerUser()
	{
		return serverUser;
	}

	public String getServerPassword()
	{
		return serverPassword;
	}

	public void setServerUrl(String serverUrl)
	{
		this.serverUrl = serverUrl;
	}

	public void setServerUser(String serverUser)
	{
		this.serverUser = serverUser;
	}

	public void setServerPassword(String serverPassword)
	{
		this.serverPassword = serverPassword;
	}

	public boolean isUsingTLS()
	{
		return usingTLS;
	}

	public void setUsingTLS(boolean usingTLS)
	{
		this.usingTLS = usingTLS;
	}

	public boolean isUsingSSL()
	{
		return usingSSL;
	}

	public void setUsingSSL(boolean usingSSL)
	{
		this.usingSSL = usingSSL;
	}

}
