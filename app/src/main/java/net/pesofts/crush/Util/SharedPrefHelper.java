package net.pesofts.crush.Util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {
	private static SharedPrefHelper instance;

	public static final String PREFERENCE_NAME = "PUBLIC_ENEMY_PREF";
	public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	public static final String USER_STATUS = "USER_STATUS";
	public static final String USER_ID = "USER_ID";
	public static final String POINT = "POINT";
	public static final String GENDER = "GENDER";
	public static final String NOTICE_ID = "NOTICE_ID";
	public static final String STORY_UPDATE_TIME = "STORY_UPDATE_TIME";

	public SharedPreferences prefs;

	protected SharedPrefHelper() {
	}

	public static SharedPrefHelper getInstance(Context context) {
		if (instance == null) {
			instance = new SharedPrefHelper();
			instance.prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		}

		return instance;
	}

	public boolean getSharedPreferences(String key, boolean defaultVal) {
		return prefs.getBoolean(key, defaultVal);
	}

	public String getSharedPreferences(String key, String defaultVal) {
		return prefs.getString(key, defaultVal);
	}

	public long getSharedPreferences(String key, long defaultVal) {
		return prefs.getLong(key, defaultVal);
	}

	public int getSharedPreferences(String key, int defaultVal) {
		return prefs.getInt(key, defaultVal);
	}

	public float getSharedPreferences(String key, float defaultVal) {
		return prefs.getFloat(key, defaultVal);
	}

	public void setSharedPreferences(String key, boolean val) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(key, val);
		edit.commit();
	}

	public void setSharedPreferences(String key, int val) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt(key, val);
		edit.commit();
	}

	public void setSharedPreferences(String key, String val) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(key, val);
		edit.commit();
	}

	public void setSharedPreferences(String key, long val) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putLong(key, val);
		edit.commit();
	}

	public void setSharedPreferences(String key, float val) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putFloat(key, val);
		edit.commit();
	}

	public void removeSharedPreferences(String key) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.remove(key);
		edit.commit();
	}

	public void removeAllSharedPreferences() {
		removeSharedPreferences(ACCESS_TOKEN);
		removeSharedPreferences(USER_STATUS);
		removeSharedPreferences(USER_ID);
		removeSharedPreferences(POINT);
	}


}
