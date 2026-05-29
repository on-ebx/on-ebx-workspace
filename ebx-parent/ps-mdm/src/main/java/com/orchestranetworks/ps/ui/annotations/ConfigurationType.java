package com.orchestranetworks.ps.ui.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationType {
	String nameInMenu();
	String serviceDescription();
	String systemPropertyName();
	String serviceKey();
	/**
	 * @return a property name that needs to be set in ebx.properties in order for this configuration to work.
	 */
	String requiredPropertyName() default "";
	/**
	 * @return what the property value from @requiredPropertyName needs to be in order for this configuration to work. this can be a multiple value separated by a comma (,).
	 */
	String requiredPropertyValue() default "";
}
