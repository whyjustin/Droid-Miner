package com.whyjustin.android.droidminer;

public class Account {
	private String _address;
	private double _balance;
	private boolean _save;
	
	public String getAddress() {
		return _address;
	}
	
	public void setAddress(String address) {
		_address = address;
	}
	
	public double getBalance() {
		return _balance;
	}
	
	public void setBalance(double balance) {
		_balance = balance;
	}
	
	public boolean getSave() {
		return _save;
	}
	
	public void setSave(Boolean save) {
		_save = save;
	}
}
