package com.orchestranetworks.ps.annotations;

import java.lang.annotation.*;

/**
 */
@Documented
public @interface NonProd {

	abstract String reason();

	abstract String todo();
}
