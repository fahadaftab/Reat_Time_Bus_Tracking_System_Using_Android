package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
	// LogCat tag
	private static String TAG = SessionManager.class.getSimpleName();

	// Shared Preferences
	SharedPreferences pref;

	Editor editor;
	Context _context;

	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Shared preferences file name
	private static final String PREF_NAME = "RECGO";
	
	private static final String KEY_USER_NAME = "user_name";
	private static final String KEY_USER_EMAIL = "user_email";
	private static final String KEY_USER_PASSWORD = "user_password";



	public SessionManager(Context context) {
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public void setLogin(String username,String email,String password) {
		editor.putString(KEY_USER_NAME, username);
		editor.putString(KEY_USER_EMAIL, email);
		editor.putString(KEY_USER_PASSWORD, password);
		// commit changes
		editor.commit();

		Log.d(TAG, "User login session modified!");
	}

	public  String getKeyUserName() {
		return pref.getString(KEY_USER_NAME,"");
	}

	public  String getKeyUserEmail() {
		return pref.getString(KEY_USER_EMAIL,"");
	}

	public  String getKeyUserPassword() {
		return pref.getString(KEY_USER_PASSWORD,"");
	}
}
