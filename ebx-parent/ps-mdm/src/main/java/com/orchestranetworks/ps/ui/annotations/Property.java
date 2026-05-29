package com.orchestranetworks.ps.ui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
	String propertyName();
	String label();
	String description();
	int minAccure() default 0;
	int maxAccure() default 1;
	String dataType() default "string";
	int order();
	String possibleValuesClassName() default "";
	String defaultValue() default "";
}
