package uk.co.flumeland.abcsaddyourown;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment {

	protected static final int RESULT_OK = -1;
	private DatabaseHelper db;
	private List<String> categories = new ArrayList<String>();
	protected static CharSequence catsCS[];
	protected static CharSequence valuesCS[];

	@Override
	/**
	 * A method to inflate the preference screen
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DatabaseHelper(getActivity().getApplicationContext());
		prepareCategoriesForCatSelect();

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		Preference addPict = (Preference)findPreference("abcayo_add_photo");
		addPict.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent("uk.co.flumeland.ABCsAddYourOwn.Camera"));
				getActivity().finish();
				return true;
			}
		});

		Preference quit = (Preference)findPreference("abcayo_quit");
		quit.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				getActivity().setResult(RESULT_OK, null);
				getActivity().finish();
				getActivity().finish();
				return true;
			}
		});

		Preference addSound = (Preference)findPreference("abcayo_add_sound");
		addSound.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent("uk.co.flumeland.ABCsAddYourOwn.Sound"));
				getActivity().finish();
				return true;
			}
		});	
		
		Preference aboutInfo = (Preference)findPreference("abcayo_info");
		aboutInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent("uk.co.flumeland.ABCsAddYourOwn.About"));
				return true;
			}
		});	
		

		MultiSelectListPreference mSLPref = (MultiSelectListPreference) findPreference("catChoice");
		setMultiSelectListPreferenceData(mSLPref);
		mSLPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
			
				return true;
			}
		});
	}

	/**
	 * A method to obtain a list of categories
	 */
	private void prepareCategoriesForCatSelect() {
		Log.i("SettingsFragment", "prepareCategoriesForCatSelect() called");
		categories.clear();
		db.loadCategories();
		for (int i=0; i<db.categories.size(); i++) {
			categories.add(i, db.categories.get(i).getCategory());
			Log.i("SettingsFragment","categories i " + i + " = " + categories.get(i));
		}
		int size = categories.size();
		catsCS = new CharSequence[size];
		valuesCS = new CharSequence[size];
		for (int i=0; i<categories.size(); i++) {
			catsCS[i] = categories.get(i);
			valuesCS[i] = Integer.toString(i);
		}			
	}

	/**
	 * A method to handle the multi select categories list
	 * @param mSLPref
	 */
	protected static void setMultiSelectListPreferenceData(MultiSelectListPreference mSLPref) {
		mSLPref.setEntries(catsCS);
        mSLPref.setDefaultValue("1");
        mSLPref.setEntryValues(valuesCS);
	}
}