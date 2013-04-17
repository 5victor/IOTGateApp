package com.victor.iotgateapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
								implements OnPreferenceChangeListener, OnSharedPreferenceChangeListener {
	private static final String perf_host_ip = "perf_host_ip";
	private static final String perf_service_switch = "perf_service_switch";
	private static final String perf_auto_start = "perf_auto_start";
	
	private Preference service_switch;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_iot);
		
		Preference host_ip = findPreference(perf_host_ip);
		host_ip.setOnPreferenceChangeListener((OnPreferenceChangeListener) this);
		//initPreference(host_ip);
		
		service_switch = findPreference(perf_service_switch);
		service_switch.setOnPreferenceChangeListener((OnPreferenceChangeListener) this);
		//initPreference(service_switch);
		
		Preference auto_start = findPreference(perf_auto_start);
		auto_start.setOnPreferenceChangeListener((OnPreferenceChangeListener) this);
		//initPreference(auto_start);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

	}

	private void initPreference(Preference preference)
	{
		this.onPreferenceChange(preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
		String stringValue = value.toString();
		
		if (preference.getKey().equals(perf_service_switch)) {
			SwitchPreference service = (SwitchPreference) preference;
			service.setChecked((Boolean) value);
		}
		{
			preference.setSummary(stringValue);
		}
		
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals(perf_service_switch))
			this.onPreferenceChange(service_switch,
					sharedPreferences.getBoolean(perf_service_switch, false));
		
	}
}