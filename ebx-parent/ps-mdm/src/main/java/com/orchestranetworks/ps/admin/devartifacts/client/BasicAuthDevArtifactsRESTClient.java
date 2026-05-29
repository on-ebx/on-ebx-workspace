package com.orchestranetworks.ps.admin.devartifacts.client;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.auth.*;

import com.orchestranetworks.service.*;

/**
 * A REST client that uses Basic Auth to authenticate the user
 */
public class BasicAuthDevArtifactsRESTClient extends AbstractDevArtifactsRESTClient
{
	private String username;
	private String password;

	public BasicAuthDevArtifactsRESTClient(String serviceURL, String username, String password)
	{
		super(serviceURL);
		this.username = username;
		this.password = password;
	}

	@Override
	protected void authenticateRequest(HttpEntityEnclosingRequestBase request)
		throws OperationException
	{
		Header header;
		try
		{
			header = new BasicScheme()
				.authenticate(new UsernamePasswordCredentials(username, password), request, null);
		}
		catch (AuthenticationException ex)
		{
			throw OperationException.createError("Error authenticating HTTP request.", ex);
		}
		request.addHeader(header);
	}
}
