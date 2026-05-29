package com.orchestranetworks.ps.admin.devartifacts.client;

import java.io.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;

import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.service.*;

/**
 * An abstract REST client that handles executing the request, but doesn't specify
 * an authentication mechanism
 */
public abstract class AbstractDevArtifactsRESTClient implements DevArtifactsClient
{
	private String serviceURL;

	protected AbstractDevArtifactsRESTClient(String serviceURL)
	{
		this.serviceURL = serviceURL;
	}

	/**
	 * Authenticate the request and throw an exception if not authenticated
	 * 
	 * @param request the request
	 * @throws OperationException if not authenticated or an error occurred while authenticating
	 */
	protected abstract void authenticateRequest(HttpEntityEnclosingRequestBase request)
		throws OperationException;

	@Override
	public void execute(OutputStream out, DevArtifactsDTO dto) throws OperationException
	{
		String jsonInput = createJSONInput(dto);
		HttpEntityEnclosingRequestBase request = createRequest(jsonInput);
		authenticateRequest(request);
		CloseableHttpResponse response = executeRequest(request);

		try
		{
			// Get the status code and if it indicates anything other than OK, throw exception
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			if (statusCode != HttpStatus.SC_OK)
			{
				throw OperationException.createError(
					"Received error in HTTP response: " + statusCode + " "
						+ statusLine.getReasonPhrase());
			}

			// Write the response out
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				try
				{
					entity.writeTo(out);
				}
				catch (IOException ex)
				{
					throw OperationException
						.createError("Error writing response to output stream.", ex);
				}
			}
		}
		finally
		{
			try
			{
				response.close();
			}
			catch (IOException ex)
			{
				throw OperationException.createError("Error closing HTTP response.", ex);
			}
		}
	}

	protected String createJSONInput(DevArtifactsDTO dto)
	{
		return dto.toJSON();
	}

	private HttpEntityEnclosingRequestBase createRequest(String jsonInput)
	{
		HttpEntityEnclosingRequestBase request = new HttpPost(serviceURL);

		if (jsonInput != null)
		{
			StringEntity requestEntity = new StringEntity(jsonInput, ContentType.APPLICATION_JSON);
			request.setEntity(requestEntity);
		}
		return request;
	}

	private static CloseableHttpResponse executeRequest(HttpEntityEnclosingRequestBase request)
		throws OperationException
	{
		CloseableHttpClient client = HttpClientBuilder.create().build();
		CloseableHttpResponse response;
		try
		{
			response = client.execute(request);
		}
		catch (IOException ex)
		{
			throw OperationException.createError("Error executing HTTP request.", ex);
		}
		finally
		{
			try
			{
				client.close();
			}
			catch (IOException ex)
			{
				throw OperationException.createError("Error closing HTTP client.", ex);
			}
		}
		return response;
	}
}
