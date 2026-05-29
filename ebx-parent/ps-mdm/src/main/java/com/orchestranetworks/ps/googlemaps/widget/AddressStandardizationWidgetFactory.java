package com.orchestranetworks.ps.googlemaps.widget;

import org.apache.commons.lang3.StringUtils;

import com.onwbp.base.text.Severity;
import com.orchestranetworks.ps.googlemaps.message.Messages;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.SchemaTypeName;
import com.orchestranetworks.ui.form.widget.UIWidgetFactory;
import com.orchestranetworks.ui.form.widget.WidgetFactoryContext;
import com.orchestranetworks.ui.form.widget.WidgetFactorySetupContext;
/**
 * Factory class of AddressStandardizationWidget
 * @author noiritabera
 *
 */
public class AddressStandardizationWidgetFactory implements UIWidgetFactory<AddressStandardizationWidget> {
	public static final String ADDRESS_TYPE_NAME = "GoogleMapsAddress";
	private String google_api_key;
	SchemaNode widget_group_node;

	@Override
	public void setup(final WidgetFactorySetupContext context) {
		final SchemaNode schemaNode = context.getSchemaNode();

		final SchemaTypeName schemaTypeName = schemaNode.getXsTypeName();
		
		this.widget_group_node = schemaNode;
		if (!schemaTypeName.getNameWithoutPrefix().equals(ADDRESS_TYPE_NAME)) {
			context.addMessage(
					Messages.get(AddressStandardizationWidgetFactory.class, Severity.ERROR, "TheAttributeMustHaveTheAddressType"));
		}
		if(this.google_api_key==null || StringUtils.EMPTY.equals(this.google_api_key)) {
			context.addMessage(
					Messages.get(AddressStandardizationWidgetFactory.class, Severity.ERROR, "googleapikeyismandatory"));
		}
		
	}

	@Override
	public AddressStandardizationWidget newInstance(final WidgetFactoryContext context) {
		return new AddressStandardizationWidget(context, this.google_api_key, widget_group_node);
	}

	public String getGoogle_api_key() {
		return google_api_key;
	}

	public void setGoogle_api_key(String google_api_key) {
		this.google_api_key = google_api_key;
	}
}
