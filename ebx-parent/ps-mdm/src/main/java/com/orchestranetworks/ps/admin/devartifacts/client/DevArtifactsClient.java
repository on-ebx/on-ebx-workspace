package com.orchestranetworks.ps.admin.devartifacts.client;

import java.io.*;

import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.service.*;

/**
 * An interface for a client of Dev Artifacts
 */
public interface DevArtifactsClient
{
	/**
	 * Execute Dev Artifacts with the given DTO input and write the response to the given output stream
	 * 
	 * @param out the output stream to write the response to
	 * @param dto the DTO representing the input
	 * @throws OperationException if an error occurs while executing
	 */
	void execute(OutputStream out, DevArtifactsDTO dto) throws OperationException;
}
