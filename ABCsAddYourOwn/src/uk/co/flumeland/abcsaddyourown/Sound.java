package uk.co.flumeland.abcsaddyourown;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Sound extends Activity implements View.OnClickListener {

	private static final String SND_FILE_SUFFIX = ".3gp";	

	private Button butRec;
	private Button butStop;
	private Button butPlay;
	private Button butSave;
	private Button butSearch;
	private TextView textObject;
	private TextView textCategory;
	private ImageView imageObject;
	private AutoCompleteTextView textSearchName;
	private DatabaseHelper db;
	private MediaRecorder sndEffRec;
	private MediaPlayer sndEffPlayer;
	private Typeface tf;
	private List<String> savedObjects = new ArrayList<String>();
	private String sndTempFileName;
	private File sndPermFile;
	private boolean recording = false;
	private boolean soundCaptured = false;
	private boolean playing = false;
	private String searchItem;
	private Item selectObject;

	@Override
	/**
	 * A method to inflate and prepare activity_sound layout and set up various variables
	 */
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound);

		butStop = (Button) findViewById(R.id.bSVStop);
		butRec = (Button) findViewById(R.id.bSVRecord);
		butPlay = (Button) findViewById(R.id.bSVPlay);
		butSave = (Button) findViewById(R.id.bSVSave);
		butSearch = (Button) findViewById(R.id.bSVSearch);
		imageObject = (ImageView) findViewById(R.id.ivSVObjImage);
		textObject = (TextView) findViewById(R.id.tvSVName);
		textCategory = (TextView) findViewById(R.id.tvSVCategory);
		textSearchName = (AutoCompleteTextView) findViewById(R.id.tvSVSearch);
		db = new DatabaseHelper(getApplicationContext());
		tf = Typeface.createFromAsset(getAssets(), "fonts/abeezee/ABeeZee-Regular.otf");
		butStop.setTypeface(tf, 1);
		butRec.setTypeface(tf, 1);
		butPlay.setTypeface(tf, 1);
		butSave.setTypeface(tf, 1);
		butSearch.setTypeface(tf, 1);
		textObject.setTypeface(tf, 1);
		textCategory.setTypeface(tf, 1);
		textSearchName.setTypeface(tf, 1);
		butStop.setOnClickListener(this);
		butRec.setOnClickListener(this);
		butPlay.setOnClickListener(this);
		butSave.setOnClickListener(this);
		butSearch.setOnClickListener(this);

		setUpFilePath();	
		loadLastObject();

		db.getAllItems();		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, db.allItems);
		textSearchName.setAdapter(adapter);
	}

	/**
	 * A method to load the last object in the database by default
	 */
	private void loadLastObject() {
		Log.i("ABCsSound","loadLastObject() called");
		int last = MainPlay.totalItems;
		selectObject = db.findAndRetrieveId(last);
		Log.i("ABCsSound","loadLastObject() obj name = " + selectObject.getName() + " obj cat = " + selectObject.getCategory());
		Toast t = Toast.makeText(getApplicationContext(), "Last item added is loaded automatically\nUse search to load alternative item", Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
		checkEligibility(selectObject);
	}

	/**
	 * A method to check if the object can have a new sound effect added
	 * @param obj	The object select for sound effect
	 */
	private void checkEligibility(Item obj) {
		Log.i("ABCsSound","checkEligibility() called");
		if (!(obj.getSource() == 0 && obj.hasSndEffect == 1)) {
			// If object is not default with a sound effect
			textObject.setText(obj.getName());
			textCategory.setText(obj.getCategory());
			Uri uriPhoto = Uri.parse(obj.getPhotoFile());
			imageObject.setImageURI(uriPhoto);
		} else {
			Toast t = Toast.makeText(getApplicationContext(), "Default object with sound effect cannot be altered", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		}
	}

	/**
	 * A method to prepare for recorder for recording
	 */
	private void prepareAudioSources() {
		Log.i("ABCsSound","prepareAudioSources() called");
		sndEffRec = new MediaRecorder();
		sndEffRec.setAudioSource(MediaRecorder.AudioSource.MIC);
		sndEffRec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		sndEffRec.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
		sndEffRec.setOutputFile(sndTempFileName);
	}

	/**
	 * A method to prepare a File path for the sound recording
	 */
	public void setUpFilePath() {
		Log.i("ABCsSound","setUpFilePath() called");
		sndTempFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		sndTempFileName += "/temp.3gp";
	}


	@Override
	/**
	 * A method to handle receipt of a Click notification from the onClickListener
	 */
	public void onClick(View v) {
		Log.i("ABCsSound","onClick() called");
		switch (v.getId()) {
		case R.id.bSVRecord:
			recordSound();
			break;
		case R.id.bSVPlay:
			playSound();
			break;	
		case R.id.bSVStop:
			StopPlayOrRec();
			break;
		case R.id.bSVSearch:
			findObject();
			break;
		case R.id.bSVSave:
			initSaveSoundToDB();
			break;
		}		
	}

	/**
	 * A method to initiate saving of the recording
	 */
	private void initSaveSoundToDB() {
		Log.i("ABCsSound","initSaveSoundToDB() called, soundCaptured = " + soundCaptured);
		if (soundCaptured == false)  {
			Toast t = Toast.makeText(getApplicationContext(), "Please record a sound before saving", Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		} else {
			copySndToPermLocation();
			String newSndFilePath = sndPermFile.getAbsolutePath();
			selectObject.setSoundEffectFile(newSndFilePath);
			db.updateObject(selectObject);
			Toast t = Toast.makeText(getApplicationContext(), "SAVED", Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			finish();
			finish();
		}
	}

	/**
	 * A method to save the recorded sound to a permentant location
	 */
	private void copySndToPermLocation() {
		Log.i("ABCsSound","copySndToPermLocation() called, SndName = " + selectObject.getName());
		String fileName = setUpPermFileName();
		Log.i("ABCsSound","copySndToPermLocation(), fileName = " + fileName);
		sndPermFile = new File(getExternalFilesDir(null), fileName);
		try {
			FileOutputStream out = new FileOutputStream(sndPermFile);
			FileInputStream in = new FileInputStream(sndTempFileName);
			// copy in to out
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			// close the input file
			in.close();
			in = null;
			// final write the output file
			out.flush();
			out.close();
			out = null;
			// delete input file
			new File(sndTempFileName).delete();
			soundCaptured = false;
			butSave.setEnabled(false);
		} 
		catch (FileNotFoundException fnfe) {
			Log.i("copySndToPermLocation()", fnfe.getMessage());
		}			   
		catch (IOException ioe) {
			// Unable to create file
			Toast t = Toast.makeText(getApplicationContext(), "File not saved, check external storage is mounted", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			Log.i("copySndToPermLocation()", ioe.getMessage());
		}
		catch (Exception e) {
			Log.i("copySndToPermLocation()", e.getMessage());
		}
	}

	/**
	 * A method to prepare a permanent file name
	 * @return
	 */
	private String setUpPermFileName() {
		String fileName = selectObject.getName() + selectObject.getCategory() + selectObject.getIdNo() + SND_FILE_SUFFIX;
		return fileName;
	}

	/**
	 * A method to search for a specific file name
	 */
	private void findObject() {
		Log.i("ABCsCamera","findObject() called");
		searchItem = textSearchName.getText().toString();
		selectObject  = new Item(db.findAndRetrieveName(searchItem));
		if (selectObject.getName().equalsIgnoreCase("NotFound")) {
			Toast t = Toast.makeText(getApplicationContext(), "No results for search", Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
			textSearchName.setText("");
		} else if (selectObject != null) {
			checkEligibility(selectObject);
			textSearchName.setText("");
		}
	}

	/**
	 * A method to replay the recorded sound
	 */
	private void playSound() {
		if (soundCaptured == true) {
			try{
				Uri uriSound = Uri.parse(sndTempFileName);
				new MediaPlayer();
				sndEffPlayer = MediaPlayer.create(this, uriSound);
				sndEffPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					public void onCompletion(MediaPlayer mp) {
						finishPlaying(); // clear up play
					}
				});
				sndEffPlayer.start();
				int green = getResources().getColor(R.color.green_start);
				butPlay.setTextColor(green);
				butPlay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.av_play_grn, 0);
				butRec.setEnabled(false);
				Toast t = Toast.makeText(getApplicationContext(), "Playing Recording", Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				finishPlaying();
			}
		}
	}

	/**
	 * A method to stop either the playback or recording in progress
	 */
	private void StopPlayOrRec() {
		Log.i("ABCsSound","stopPlayOrRec() called recording = " + recording + " playing = " + playing
				);
		if (recording == true) {
			finishRecording();
			Toast t = Toast.makeText(getApplicationContext(), "Recording stopped", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		} else if (playing == true) {
			finishPlaying();
			Toast t = Toast.makeText(getApplicationContext(), "Play stopped", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();		
		}
	}

	/**
	 * A method to stop the media player playback
	 */
	private void finishPlaying() {
		sndEffPlayer.stop();
		sndEffPlayer.release();
		sndEffPlayer = null;
		playing = false;
		int black = getResources().getColor(R.color.black);
		butPlay.setTextColor(black);
		butPlay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.av_play, 0);
		butRec.setEnabled(true);
	}

	/**
	 * A method to stop the media recorder recording
	 */
	private void finishRecording() {
		sndEffRec.stop();
		sndEffRec.release();
		sndEffRec  = null;
		int black = getResources().getColor(R.color.black);
		butRec.setTextColor(black);
		butRec.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.av_record, 0);
		butPlay.setEnabled(true);
		recording = false;
		soundCaptured = true;
		butSave.setEnabled(true);
	}

	/**
	 * A method to start the media recorder recording for a max 3 seconds
	 */
	private void recordSound() {
		Log.i("ABCsSound","recordSound() called");
		if (!recording) {
			prepareAudioSources();
			try {
				sndEffRec.setMaxDuration(3000);
				sndEffRec.setOnInfoListener(new MediaRecorder.OnInfoListener() {
					@Override
					public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
						finishRecording();				
					}
				});
				sndEffRec.prepare();
				sndEffRec.start();
				recording = true;
				int red = getResources().getColor(R.color.red_start);
				butRec.setTextColor(red);
				butRec.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.av_record_red, 0);
				butPlay.setEnabled(false);
				butStop.setEnabled(true);
				Toast t = Toast.makeText(getApplicationContext(), "Recording started, limited to 3s max", Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Log.i("ABCsMainPlay","recordSound() prepare has failed");
				e.printStackTrace();
				finishRecording();
			}
		}
	}


	@Override
	/**
	 * method to release media player/recorder sources onPause
	 */
	public void onPause() {
		super.onPause();
		if (sndEffRec != null) {
			sndEffRec.release();
			sndEffRec = null;
		}
		if (sndEffPlayer != null) {
			sndEffPlayer.release();
			sndEffPlayer = null;
		}
	}

}
