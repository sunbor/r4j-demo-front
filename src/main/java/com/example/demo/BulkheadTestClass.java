package com.example.demo;

public class BulkheadTestClass {
	
	private String test1;
	private String test2;
	public BulkheadTestClass() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BulkheadTestClass(String test1, String test2) {
		super();
		this.test1 = test1;
		this.test2 = test2;
	}
	public String getTest1() {
		return test1;
	}
	public void setTest1(String test1) {
		this.test1 = test1;
	}
	public String getTest2() {
		return test2;
	}
	public void setTest2(String test2) {
		this.test2 = test2;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((test1 == null) ? 0 : test1.hashCode());
		result = prime * result + ((test2 == null) ? 0 : test2.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BulkheadTestClass other = (BulkheadTestClass) obj;
		if (test1 == null) {
			if (other.test1 != null)
				return false;
		} else if (!test1.equals(other.test1))
			return false;
		if (test2 == null) {
			if (other.test2 != null)
				return false;
		} else if (!test2.equals(other.test2))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "bulkheadTestClass [test1=" + test1 + ", test2=" + test2 + "]";
	}
	
	

}
