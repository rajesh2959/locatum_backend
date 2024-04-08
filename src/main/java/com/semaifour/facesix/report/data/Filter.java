package com.semaifour.facesix.report.data;

import java.util.List;

public class Filter {

	private String fieldname;
	private String operation;
	private List<String> value;

	public String getFieldname() {
		return fieldname;
	}

	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Filter [fieldname=" + fieldname + ", operation=" + operation + ", value=" + value + "]";
	}

}
