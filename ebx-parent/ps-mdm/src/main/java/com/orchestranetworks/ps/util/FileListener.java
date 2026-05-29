package com.orchestranetworks.ps.util;

import java.nio.file.WatchEvent.*;

public interface FileListener
{
	void processEvent(Kind<?> kind);
	String getFileName();
}
