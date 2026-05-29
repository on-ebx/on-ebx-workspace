package com.orchestranetworks.ps.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.WatchEvent.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class FileWatcher extends Thread
{
	private File file;
	private AtomicBoolean stop = new AtomicBoolean(false);
	private FileListener listener = null;

	public FileWatcher()
	{
	}

	public boolean isStopped()
	{
		return stop.get();
	}
	public void stopThread()
	{
		stop.set(true);
	}

	public void doOnChange(Kind<?> kind)
	{
		listener.processEvent(kind);
	}

	public void setListener(FileListener listner)
	{
		String fileName = listner.getFileName();
		if (file == null)
		{
			file = new File(fileName);
		}
		listener = listner;
	}

	@Override
	public void run()
	{
		try (WatchService watcher = FileSystems.getDefault().newWatchService())
		{
			Path path = file.toPath().getParent();
			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
			while (!isStopped())
			{
				WatchKey key;
				try
				{
					key = watcher.poll(5, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					return;
				}
				if (key == null)
				{
					Thread.yield();
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents())
				{
					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();

					if (kind == StandardWatchEventKinds.OVERFLOW)
					{
						Thread.yield();
						continue;
					}
					else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
						&& filename.toString().equals(file.getName()))
					{
						doOnChange(kind);
					}
					boolean valid = key.reset();
					if (!valid)
					{
						break;
					}
				}
				Thread.yield();
			}
		}
		catch (Throwable e)
		{
			// Log or rethrow the error
		}
	}
}