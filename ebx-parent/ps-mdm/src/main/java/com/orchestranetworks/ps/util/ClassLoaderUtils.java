package com.orchestranetworks.ps.util;

import java.lang.reflect.*;

public class ClassLoaderUtils
{
	public static Class<?> loadClass(String className) throws ClassNotFoundException
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Class<?> loadedClass = loadClass(classLoader, className);
		if (loadedClass == null)
		{
			loadedClass = loadClass(ClassLoaderUtils.class.getClassLoader(), className);
			if (loadedClass == null)
			{
				throw new ClassNotFoundException("Class " + className + " Not found.");
			}
		}
		return loadedClass;
	}

	private static Class<?> loadClass(ClassLoader loader, String className)
	{
		try
		{
			return loader.loadClass(className);
		}
		catch (ClassNotFoundException e)
		{
			return null;
		}
	}

	public static Object newInstance(String className)
	{
		return newInstance(className, null, null);
	}

	@SuppressWarnings("rawtypes")
	public static Object newInstance(
		String className,
		Class[] constructorTypes,
		Object[] constructorObjects)
	{
		try
		{
			Class<?> loaedClass = loadClass(className);
			Constructor<?> clientConstructor = loaedClass.getConstructor(constructorTypes);
			return clientConstructor.newInstance(constructorObjects);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(
				"There was an error instantiating the class: " + className
					+ " with constructor arguments types " + constructorTypes + " and values of "
					+ constructorObjects,
				ex);
		}
	}

}
