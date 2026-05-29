package com.orchestranetworks.ps.ui.annotations;

import java.util.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;

public class FieldMetadata
{
	private BeanElement element;
	private Path path;
	private boolean group;
	private Map<Path, FieldMetadata> fileds = new HashMap<>();
	private Property property;

	public BeanElement getElement()
	{
		return element;
	}
	public Path getPath()
	{
		return path;
	}
	public boolean isGroup()
	{
		return group;
	}

	public void addField(Path path, FieldMetadata fieldMetadata)
	{
		fileds.put(path, fieldMetadata);
	}

	public Map<Path, FieldMetadata> getFileds()
	{
		return fileds;
	}
	public void setElement(BeanElement element)
	{
		this.element = element;
	}
	public void setPath(Path path)
	{
		this.path = path;
	}
	public void setGroup(boolean group)
	{
		this.group = group;
	}
	public void setFileds(Map<Path, FieldMetadata> fileds)
	{
		this.fileds = fileds;
	}
	public void setAnootationProperty(Property property)
	{
		this.property = property;
	}

	public Property getAnootationProperty()
	{
		return this.property;
	}

}
