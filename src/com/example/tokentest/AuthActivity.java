package com.example.tokentest;

import java.io.IOException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.UserRecoverableNotifiedException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AuthActivity extends Activity {
	 
	/* Access to common Network functions like checking if the user is online */
	private NetworkManager 							_networkManager;
	/* Used to store and retrieve the user account and token associated with it globally */
	private AuthPreferences 						_authPreferences;
	/* Needed to access google accounts on the phone */
	private AccountManager 							_accountManager;
	/* A drop down selector for the user to select an account from accountManager */
	private Spinner 								_accountTypesSpinner;
	/* Used to store the names (emails) of the google accounts */
	private String[] 								_accountNames;
	/* Debug TAG */
	private static final String TAG = 				"CC AuthActivity";
<<<<<<< HEAD
	/* AUTH Scope, used to give access to part of user's information and access to parts of Google
	 *  used 'ah' as per http://blog.notdot.net/2010/05/Authenticating-against-App-Engine-from-an-Android-app */
	private static final String SCOPE = 			"ah";//"oauth2:https://www.googleapis.com/auth/userinfo.profile";
	/* AUTH code used to test against if authorization successful, can be anything */
=======
	private static final String SCOPE = 			"oauth2:https://www.googleapis.com/auth/userinfo.profile";
>>>>>>> parent of ef80766... changed auth scope, simplecta url, and response code handling
  	private static final int AUTHORIZATION_CODE = 	1993;
  	/* Account code used to test against if account retrieval is  successful, can be anything */
	private static final int ACCOUNT_CODE = 		1601;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		
		_networkManager = NetworkManager.getInstance();
		
		// Need online access to sign in, if there's none, do not proceed
		if ( _networkManager.isOnline( this.getApplicationContext() ) ){
			finish();
		}
		_accountManager = AccountManager.get(this);
		_accountNames = getAccountNames( this );
		_authPreferences = new AuthPreferences(this);
		
		_accountTypesSpinner = initializeSpinner(
                R.id.accounts_tester_account_types_spinner, _accountNames );
		/*
		//if (_authPreferences.getUser() != null
			//	&& _authPreferences.getToken() != null) {
			//doCoolAuthenticatedStuff();
		//} else {
			//initializeFetchButton();
		//}
		 * 
		 */
		initializeFetchButton();
		
	}
 
	private Spinner initializeSpinner( int id, String[] values ) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AuthActivity.this,
                android.R.layout.simple_spinner_item, values);
        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        return spinner;
    }
	
	public String[] getAccountNames( Activity activity ) {
        Account[] accounts = _accountManager.getAccountsByType( GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE );
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
	
	private void doCoolAuthenticatedStuff() {
		Log.d("AuthApp", _authPreferences.getToken());
		// Launch the feed activity
		Intent intent = new Intent( this, FeedActivity.class );
		startActivity( intent );
	}

 
	/**
	   * Get a authentication token if one is not available. Throws an exception or non empty string
	   */
	  public String fetchToken( Activity activity, String strEmail, String strScope ) {
	      try {
	    	   return GoogleAuthUtil.getToken(
	        		  activity, strEmail, strScope);
	      } catch (UserRecoverableNotifiedException userRecoverableException) {
	          // Unable to authenticate, but the user can fix this.
	          // Forward the user to the appropriate activity.
	          Log.e( TAG, "Could not fetch token.", null);
	      } catch (UserRecoverableAuthException userRecoverableException ) {
	    		  activity.startActivityForResult(userRecoverableException.getIntent(), AUTHORIZATION_CODE );
	      }
	      catch (GoogleAuthException fatalException) {
	    	  Log.e( TAG, "Unrecoverable error " + fatalException.getMessage(), fatalException);
	      }
	      catch (Exception e) {
	    	  Log.e( TAG, e.getMessage() );
	      }
	      return null;
	  }
	
	private void initializeFetchButton() {
        Button getToken = (Button) findViewById(R.id.btnLogin);
        getToken.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int accountIndex = _accountTypesSpinner.getSelectedItemPosition();
                if (accountIndex < 0) {
                    // this happens when the sample is run in an emulator which has no google account
                    // added yet.
                    //show("No account available. Please add an account to the phone first.");
                    return;
                }
                _authPreferences.setUser( _accountNames[accountIndex] );
                getTokenFromAccount();
            }
        });
    }

 
	private void getTokenFromAccount() {
		AbstractGetTokenTask taskFetchToken = new AbstractGetTokenTask( this, _authPreferences.getUser(), SCOPE, 101 );
		taskFetchToken.execute();	
	}
	
	public  class AbstractGetTokenTask extends AsyncTask<Void, Void, Void>{
	    private static final String TAG = "TokenInfoTask";
	    //private static final String NAME_KEY = "given_name";
	    protected AuthActivity activity;

	    protected String strScope;
	    protected String strEmail;
	    protected int nRequestCode;
	    protected AuthHelper auth;
	    protected String strToken;
	    
	    AbstractGetTokenTask( AuthActivity activity, String email, String scope, int requestCode) {
	        this.activity = activity;
	        this.strScope = scope;
	        this.strEmail = email;
	        this.nRequestCode = requestCode;
	        auth = new AuthHelper();
	    }

	    @Override
	    protected Void doInBackground(Void... params) {

	    	strToken = auth.fetchToken( activity, strEmail, strScope );
	    	_authPreferences.setToken( strToken );
	    	Simplecta simplecta = Simplecta.getInstance();
			simplecta.init( activity, strToken );
			
	    	return null;
	    }
	    
	    @Override
	    public void onPostExecute(Void result) {
	    	if ( strToken != null ) {
	    		Toast.makeText(getApplicationContext(), "token: " + strToken, Toast.LENGTH_SHORT  ).show();
	    		finish();
	    	}
        }
	}
}
