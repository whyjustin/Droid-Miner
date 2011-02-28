package com.whyjustin.android.droidminer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AccountAdapter extends ArrayAdapter<Account> {
    private ArrayList<Account> _accounts;
    private LayoutInflater _inflater;
    private int _layoutResource;
    
    public AccountAdapter(Context context, int textViewResourceId, ArrayList<Account> options) {
        super(context, textViewResourceId, options);
        this._accounts = options;
        this._inflater = LayoutInflater.from(context);
        this._layoutResource = textViewResourceId;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AccountView accountView;
        View v = convertView;
        if (v == null) {
            v = _inflater.inflate(_layoutResource, null);
            accountView = new AccountView();
            accountView.account = (TextView) v.findViewById(R.id.firstLineTextView);
            accountView.balance = (TextView) v.findViewById(R.id.secondLineTextView);
            v.setTag(accountView);
        }else {
        	accountView = (AccountView) v.getTag();
        }
        Account account = _accounts.get(position);
        if (account != null) {
        	accountView.account.setText(account.getAddress());
        	accountView.account.setVisibility(View.VISIBLE);
        	accountView.balance.setText(Double.toString(account.getBalance()));
        	accountView.balance.setVisibility(View.VISIBLE);
        }
        else {
        	accountView.account.setVisibility(View.GONE);
        }

        return v;
    }
}
