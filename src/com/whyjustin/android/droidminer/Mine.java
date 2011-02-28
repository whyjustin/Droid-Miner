package com.whyjustin.android.droidminer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class Mine extends Activity implements OnClickListener, OnFocusChangeListener {
	private Button btnRefresh;
	private Button btnCheckBalance;
	private EditText txtAccount;
	private CheckBox chkSaveAccount;
	private ListView lvAccount;
	private TextView txtHigh;
	private TextView txtLow;
	
	private ArrayList<Account> _accounts;
	private AccountAdapter _accountAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnRefresh = (Button)findViewById(R.id.refresh);
        btnCheckBalance = (Button) findViewById(R.id.addAccount);
        txtAccount = (EditText) findViewById(R.id.address);
        chkSaveAccount = (CheckBox)findViewById(R.id.saveAddress);
        lvAccount = (ListView)findViewById(R.id.addressList);
        txtHigh = (TextView)findViewById(R.id.high);
        txtLow = (TextView)findViewById(R.id.low);
        
        lvAccount.requestFocus();
        
        btnRefresh.setOnClickListener(this);
		btnCheckBalance.setOnClickListener(this);
		txtAccount.setOnFocusChangeListener(this);
		
		loadAccounts();
		_accountAdapter = new AccountAdapter(this, R.layout.account_list_item, _accounts);
		lvAccount.setAdapter(_accountAdapter);
		registerForContextMenu(lvAccount);
		
		loadMarketDetails();
    }

	@Override
    public void onSaveInstanceState(Bundle bundle) {
    	saveAccounts();
    }
    
    @Override
	public void onClick(View view) {
		switch (view.getId()) {
    		case R.id.addAccount:
    			addAccount();
    			break;
    		case R.id.refresh:
    			refreshAccounts();
    			break;
		}
    }

    @Override
	public void onFocusChange(View view, boolean hasFocus) {
		if (view instanceof TextView) {
			TextView text = (TextView)view;
			handleTextWatermark(text);
		}	
	}
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    	switch (view.getId()) {
    		case R.id.addressList:
    			createAddressContextMenu(menu, menuInfo);
    			break;
    	}
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		View view = info.targetView;
		switch (view.getId()) {
			case R.id.addressList:
				handleAddressItemSelected(item, info);
				break;
		}  
		return true;
	}

	private void handleAddressItemSelected(MenuItem item, AdapterContextMenuInfo info) {
		switch (item.getItemId()) {
			case 0:
				_accounts.remove(info.position);
				_accountAdapter.notifyDataSetChanged();
				break;
		}	
	}

	private void createAddressContextMenu(ContextMenu menu, ContextMenuInfo menuInfo) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
    	menu.setHeaderTitle(_accounts.get(info.position).getAddress());
    	String[] menuItems = getAccountsContextMenuItems();
    	for (int i = 0; i < menuItems.length; i++) {
	      menu.add(Menu.NONE, i, i, menuItems[i]);
	    }
	}

	private void handleTextWatermark(TextView text) {
    	String defaultText = null;
    	switch (text.getId()) {
			case R.id.address:
				defaultText = getDefaultAddress();
				break;
    	}
    	
    	if (defaultText != null) {
	    	if (text.isFocused()) {
	    		if (text.getText().toString().equals(defaultText)) {
	    			text.setText("");
	    		}
	    	} else {
	    		if (text.getText().toString().equals("")) {
	    			text.setText(defaultText);
	    		}
	    	}
    	}
	}
    
    private void loadAccounts() {
    	ArrayList<Account> accounts = new ArrayList<Account>();;
		FileInputStream fin;
		File accountFile = getFileStreamPath(getAccounts());
		try {
			if (accountFile.exists()) {
				fin = openFileInput(accountFile.getName());
	    	    ObjectInputStream ois = new ObjectInputStream(fin);
	    	    ArrayList<String> addresses = (ArrayList<String>)ois.readObject();
	    	    ois.close();
	    	    
	    	    for (String address : addresses) {
	    	    	Double balance = CheckBalance(address);
	    	    	Account account = new Account();
	    	    	account.setAddress(address);
	    	    	account.setBalance(balance);
	    	    	account.setSave(true);
	    	    	accounts.add(account);
	    	    }
			}
		} catch (Exception e) {
			accounts.clear();
			if (accountFile.exists()) {
				accountFile.delete();
			}
		}

		_accounts = accounts;
    }
    
    private void saveAccounts() {
    	ArrayList<String> addresses = new ArrayList<String>();
    	for (Account account : _accounts) {
    		if (account.getSave()) {
    			addresses.add(account.getAddress());
    		}
    	}
    	FileOutputStream fout;
		try {
			fout = openFileOutput(getAccounts(), Context.MODE_PRIVATE);
	        ObjectOutputStream oos = new ObjectOutputStream(fout);
	        oos.writeObject(addresses);
	        oos.close();
		} catch (FileNotFoundException e) {
			// TODO: handle exception
		} catch (IOException e) {
			// TODO: handle exception
		}
    }

    private void addAccount() {
    	String newAddress = txtAccount.getText().toString();
    	
    	if (newAddress.equals("") || newAddress.equals(getDefaultAddress())) {
    		new AlertDialog.Builder(this)
    			.setMessage("Please enter an address.")
    			.setPositiveButton("OK", null)
    			.setCancelable(false)
    			.show();
    		return;
    	}
    	if (_accounts.contains(newAddress)) {
    		new AlertDialog.Builder(this)
			.setMessage("Address already added.")
			.setPositiveButton("OK", null)
			.setCancelable(false)
			.show();
		return;
    	}
    	
    	double balance = CheckBalance(newAddress);
    	
        Account newAccount = new Account();
        newAccount.setAddress(newAddress);
        newAccount.setBalance(balance);
        newAccount.setSave(chkSaveAccount.isChecked());
        _accounts.add(newAccount);
        _accountAdapter.notifyDataSetChanged();
	}
    
	private void refreshAccounts() {
		for (Account account : _accounts) {
			Double balance = CheckBalance(account.getAddress());
			account.setBalance(balance);
		}
		_accountAdapter.notifyDataSetInvalidated();
	}
	
    private double CheckBalance(String address) {
    	double balance = 0.0;
    	HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(getUrlGetBalance() + address);
        try {
            HttpResponse response = client.execute(post);
            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while((current = bis.read()) != -1) {
                baf.append((byte)current);
            }
            String strBalance = new String(baf.toByteArray());
            balance = Double.parseDouble(strBalance);
        } catch (Exception e) {
        	// TODO: handle exception
        }
        return balance;
    }
    
    private void loadMarketDetails() {
    	HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://www.mtgox.com/code/data/ticker.php");
        try {
            HttpResponse response = client.execute(post);

            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);

            int current = 0;
            while((current = bis.read()) != -1) {
                baf.append((byte)current);
            }
            String text = new String(baf.toByteArray());
            JSONObject jObject = new JSONObject(text);
            JSONObject jTicker = jObject.getJSONObject("ticker");
            
            txtHigh.setText(jTicker.getString("high"));
            txtLow.setText(jTicker.getString("low"));
            
        } catch (Exception e) {
			// TODO: handle exception
		}
	}

	private String getDefaultAddress() {
		return getResources().getString(R.string.address);
	}
	
	private String getUrlGetBalance() {
		return getResources().getString(R.string.urlGetBalance);
	}
	
	private String getAccounts() {
		return getResources().getString(R.string.accounts);
	}
	
	private String[] getAccountsContextMenuItems() {
		return getResources().getStringArray(R.array.accountContextMenuItems);
	}
}