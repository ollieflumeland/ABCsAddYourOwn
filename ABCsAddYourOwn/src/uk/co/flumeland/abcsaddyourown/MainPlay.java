package uk.co.flumeland.abcsaddyourown;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainPlay extends Activity implements View.OnClickListener, OnInitListener, View.OnSystemUiVisibilityChangeListener {

	Button butOption1;
	Button butOption2;
	Button butOption3;
	Button butLeft;
	Button butRight;
	Button butReveal;
	Button butAbc;
	Button butRand;
	Button butObSound;
	ImageView imageObject1;
	ImageView imageObject2;
	TextView textAlphabet;
	TextView textLetterAlpha1;
	TextView textLetterAlpha2;
	TextView textAni;
	private ViewFlipper viewFlipper;
	private float lastXPos;
	private float currentXPos;
	private float lastYPos;


	public static Item currObject;
	private static final char[] LETS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	private static final int AUTO_HIDE_DELAY_MILLIS = 4000;
	private static final int REQUEST_FINISH = 321;
	private static final int ALPHABET_LENGTH = 26;
	private static final int DESCENDING = -1;
	private static final int ASCENDING = 1;
	private static final int RANDOM = 3;
	private boolean revealed = false;
	private static boolean autoHide = false;
	private boolean disabled = false;
	private boolean choices = true;
	private boolean positions = false;
	private boolean aniRunning = false;
	private boolean aniWaiting = false;
	private String nextAniLetters;
	private int optChoiceRand;
	private int option1ID;
	private int option2ID;
	private int option3ID;
	public static int totalItems = 0;
	public static int currItems;
	public static String[] catsChosen;
	private String letter;
	private String phonicWord;
	private float xDest;
	private float yLine;
	private float xStart;
	private View decorView;
	private DatabaseHelper db;
	public static SharedPreferences settings;

	private MediaPlayer sndEffect;
	private Typeface tf;
	// Text To Speech variables
	private TextToSpeech myTTS;
	private int MY_DATA_CHECK_CODE = 0;
	//-------------------
	Random rand = new Random();
	private SpannableString lca = new SpannableString(" a b c d e f g h i j k l m n o p q r s t u v w x y z ");  
	int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;

	@Override
	/**
	 * A method to inflate and prepare activity_main_play layout and set up various variables
	 */
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("ABCsMainPlay","onCreate() called");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_play);
		autoHide = true;

		// hiding bars-------------------------------
		hideNotificationBarBelowJellybean();

		/**
		 * get notification of UI visibility changes
		 */
		decorView = getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener
		(new View.OnSystemUiVisibilityChangeListener() {
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				Log.i("ABCsMainPlay","onSysUiVisiChange() called + visi = " + visibility);
				// Note that system bars will only be "visible" if none of the
				// LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
					// The system bars are visible.
					Thread delay = new Thread() {
						public void run(){
							try {
								sleep(AUTO_HIDE_DELAY_MILLIS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} finally {
								if (MainPlay.autoHide) {
								if (Build.VERSION.SDK_INT < 14) {
									getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
											WindowManager.LayoutParams.FLAG_FULLSCREEN);
								} else {
									decorView = getWindow().getDecorView();
									decorView.setSystemUiVisibility(uiOptions);
								}
								}
							}
						}
					};
					delay.start();
				} else {
					// The system bars are NOT visible.	        	
				}
			}
		});
		//--------------------------

		db = new DatabaseHelper(getApplicationContext());
		currItems = totalItems;
		tf = Typeface.createFromAsset(getAssets(), "fonts/abeezee/ABeeZee-Regular.otf");
		textLetterAlpha1 = (TextView)findViewById(R.id.tvMVItemLetter1);
		textLetterAlpha2 = (TextView)findViewById(R.id.tvMVItemLetter2);
		textAlphabet = (TextView)findViewById(R.id.tvMVAlpha);
		textAni = (TextView)findViewById(R.id.tvMVAniLetters);
		butOption1 = (Button)findViewById(R.id.bMVOption1);
		butOption2 = (Button)findViewById(R.id.bMVOption2);
		butOption3 = (Button)findViewById(R.id.bMVOption3);
		butLeft = (Button)findViewById(R.id.bMVdescend);
		butRight = (Button)findViewById(R.id.bMVascend);
		butReveal = (Button)findViewById(R.id.bMVReveal);
		butAbc = (Button)findViewById(R.id.bMVAbc);
		butRand = (Button)findViewById(R.id.bMVRandom);
		butObSound = (Button)findViewById(R.id.bMVObSound);
		textLetterAlpha1.setTypeface(tf, 1);
		textLetterAlpha2.setTypeface(tf, 1);
		textLetterAlpha1.setSingleLine();
		textLetterAlpha2.setSingleLine();
		textAlphabet.setTypeface(tf, 1);
		textAni.setTypeface(tf, 1);
		butOption1.setTypeface(tf, 1);
		butOption2.setTypeface(tf, 1);
		butOption3.setTypeface(tf, 1);
		butLeft.setTypeface(tf, 1);
		butRight.setTypeface(tf, 1);
		butReveal.setTypeface(tf, 1);
		butAbc.setTypeface(tf, 1);
		butRand.setTypeface(tf, 1);
		butObSound.setTypeface(tf, 1);
		imageObject1 = (ImageView)findViewById(R.id.ivMVPict1);
		imageObject2 = (ImageView)findViewById(R.id.ivMVPict2);
		viewFlipper = (ViewFlipper) findViewById(R.id.vfMVFlipper);
		butOption1.setOnClickListener(this);
		butOption2.setOnClickListener(this);
		butOption3.setOnClickListener(this);		
		butLeft.setOnClickListener(this);
		butRight.setOnClickListener(this);
		butReveal.setOnClickListener(this);
		butAbc.setOnClickListener(this);		
		butRand.setOnClickListener(this);
		butObSound.setOnClickListener(this);

		settings = PreferenceManager.getDefaultSharedPreferences(MainPlay.this);
		readCategoryChoices();

		//check for TTS data (SPEECH)
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

		setVolumeControlStream(AudioManager.STREAM_MUSIC); // defines volume controls

		//loadPhonics();
		// create a fallback currObject
		currObject = new Item(19,"swan", "animal", 0, "android.resource://uk.co.flumeland.abcsaddyourown/drawable/swan");
		// pick a rand currObject
		selectRandom();
		setAlphabetUp();
		setOptionsUp();
	}

	/**
	 *  Hide the notification bar if the 
	 *  Android version is lower than Jellybean for this activity only
	 */     
	private void hideNotificationBarBelowJellybean() { 
		Log.i("ABCsMainPlay","hideNotificationBarBelowJellybean() called");
		if (Build.VERSION.SDK_INT < 16) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	/**
	 * A method to read the category choices chosen
	 * @return	A comma separated String containing the category choices
	 */
	public static String readCategoryChoices() {
		String[] catsChosen;
		try {
			Set<String> choices = settings.getStringSet("catChoice", null);
			catsChosen = choices.toArray(new String[] {});
		} catch (Exception e) {
			Log.i("readCategory", "read failed + " + e);
			catsChosen = new String[] { "1, 2, 3, 4" };
		}
		String output = "";
		int i = 0;
		int ch = 0;
		if (catsChosen.length > 1) {
			for (i=0; i<catsChosen.length-1; i++) {
				Log.i("ABCsMainPlay", "readCategoryChoices(), cats = " + catsChosen[i]);
				try {
					ch = Integer.parseInt(catsChosen[i]);
				} catch(NumberFormatException nfe) {
					Log.i("readCategory", "Could not parse " + nfe);
				} 
				output += (ch+1) + ", ";
			}
			try {
				ch = Integer.parseInt(catsChosen[i]);
			} catch(NumberFormatException nfe) {
				Log.i("readCategory", "Could not parse " + nfe);
			} 
			output += ch+1;
			Log.i("ABCsMainPlay", "readCategoryChoices(), i = " + i + ", output = " + output);
		} else {
			try {
				ch = Integer.parseInt(catsChosen[0]);
			} catch(NumberFormatException nfe) {
				Log.i("readCategory", "Could not parse " + nfe);
			} 
			output += ch+1;
			Log.i("ABCsMainPlay", "readCategoryChoices(), single cat output = " + output);
		}
		return output;
	}

	/**
	 * A method to highlight the object letter in the alphabet string
	 */
	private void setAlphabetUp() {
		Log.i("ABCsMainPlay","setAlphabetUp() called");
		Object[] spansToRemove = lca.getSpans(0, lca.length()-1, Object.class);
		for(Object span : spansToRemove){
			lca.removeSpan(span);
		}
		int highlight = 0;
		for (int i=0; i<lca.length(); i++) {
			if (currObject.getLetter() == lca.charAt(i)) {
				highlight = i;
			}
		}

		lca.setSpan(new RelativeSizeSpan(1.5f), highlight-1, highlight+1, 0);
		lca.setSpan(new ForegroundColorSpan(Color.YELLOW), highlight, highlight+2, 0);
		textAlphabet.setText(lca);
	}

	/**
	 * A method to highlight the other letters in the object word
	 */
	private void addAlphabetLetters() {
		Log.i("ABCsMainPlay","addAlphabetLetters() called");
		char letter = ' ';
		// Skip the first letter
		for (int i=1; i<currObject.getName().length(); i++) {
			letter = currObject.getName().charAt(i);
			if (letter != ' ') {
				int highlight = 0;
				for (int j=0; j<lca.length(); j++) {
					if (letter == lca.charAt(j)) {
						highlight = j;
					}
				}
				lca.setSpan(new ForegroundColorSpan(Color.YELLOW), highlight, highlight+2, 0);
				textAlphabet.setText(lca);
			}
		}
	}



	/**
	 * Set the visibility to low profile on resuming
	 */
	protected void onResume( ) {
		Log.i("ABCsMainPlay","onResume() called");
		super.onResume();
		if (Build.VERSION.SDK_INT < 14) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	/**
	 * Method to handle touch gestures sweep left or right within image area
	 */
	public boolean onTouchEvent(MotionEvent touchevent) {
		Log.i("ABCsMainPlay","onTouchEvent() called");
		switch (touchevent.getAction()) {
		// when user first touches the screen to swap
		case MotionEvent.ACTION_DOWN: {
			lastXPos = touchevent.getX();
			lastYPos = touchevent.getY();
			break;
		}
		case MotionEvent.ACTION_UP: {
			currentXPos = touchevent.getX();

			// if touch is within image area
			if (lastYPos < identifyCurrFlipImage().getBottom() && lastXPos < identifyCurrFlipImage().getWidth() && disabled == false) {

				// if motion is a left to right swipe on screen
				if (lastXPos < currentXPos) {
					getPreceding();				
				}

				// if motion is a right to left swipe on screen
				if (lastXPos > currentXPos) {
					// Load next object into the unseen view
					getAscending();					
				}
			}			
			break;
		}
		}
		return false;
	}

	/**
	 * Method to move new viewflipper in from right and old viewflipper out left
	 */
	private void viewFlipInRightOutLeft() {
		Log.i("ABCsMainPlay","viewFlipInRightOutLeft() called");
		viewFlipper.setInAnimation(this, R.anim.in_from_right);
		viewFlipper.setOutAnimation(this, R.anim.out_to_left);
		// Show The next Screen
		viewFlipper.showNext();
	}

	/**
	 * Method to move new viewflipper in from left and old viewflipper out right
	 */
	private void viewFlipInLeftOutRight() {
		Log.i("ABCsMainPlay","viewFlipInLeftOutRight() called");
		viewFlipper.setInAnimation(this, R.anim.in_from_left);
		viewFlipper.setOutAnimation(this, R.anim.out_to_right);
		// Show the next Screen
		viewFlipper.showNext();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	/**
	 * A method to control handling of the short back press
	 */
	public void onBackPressed() {
		Log.i("ABCsMainPlay","onBackPressed() called");
		Toast t = Toast.makeText(getApplicationContext(), "SHORT PRESS DISABLED\n" +
				"LONG PRESS for options and to exit", Toast.LENGTH_LONG);
		t.setGravity(Gravity.CENTER, 0, 0 );		
		t.show();
		if (Build.VERSION.SDK_INT < 14) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			decorView = getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
			//decorView.buildLayer();
		}
	}

	@Override
	/**
	 * A method to end the app if the home button is pressed
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.i("ABCsMainPlay","onKeyUp() called");
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	/**
	 * A method to handle long key press on the back button
	 */
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		Log.i("ABCsMainPlay","onKeyLongPress() called");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//Toast t = Toast.makeText(getApplicationContext(), "You pressed it a long time", Toast.LENGTH_SHORT);
			//t.show();
			autoHide = false;
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, REQUEST_FINISH);
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	/**
	 * A method to load phonic pronunciation overrides for TTS
	 */
	private void loadPhonics() {
		Log.i("ABCsMainPlay","loadPhonics() called");
		disAllButtons();
		myTTS.addSpeech("a", "uk.co.flumeland.abcsaddyourown", R.raw.a);
		myTTS.addSpeech("ae", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("ai", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("ay", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("ey", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("es", "uk.co.flumeland.abcsaddyourown", R.raw.s);
		myTTS.addSpeech("ei", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("eigh", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("aigh", "uk.co.flumeland.abcsaddyourown", R.raw.ae_ay_ai);
		myTTS.addSpeech("air", "uk.co.flumeland.abcsaddyourown", R.raw.air_are_ear);
		myTTS.addSpeech("are", "uk.co.flumeland.abcsaddyourown", R.raw.air_are_ear);
		myTTS.addSpeech("ere", "uk.co.flumeland.abcsaddyourown", R.raw.air_are_ear);
		myTTS.addSpeech("ar", "uk.co.flumeland.abcsaddyourown", R.raw.ar);		
		myTTS.addSpeech("b", "uk.co.flumeland.abcsaddyourown", R.raw.b);
		myTTS.addSpeech("bb", "uk.co.flumeland.abcsaddyourown", R.raw.b);
		myTTS.addSpeech("c", "uk.co.flumeland.abcsaddyourown", R.raw.c_k_ck);
		myTTS.addSpeech("ce", "uk.co.flumeland.abcsaddyourown", R.raw.s);
		myTTS.addSpeech("ch", "uk.co.flumeland.abcsaddyourown", R.raw.ch);
		myTTS.addSpeech("tch", "uk.co.flumeland.abcsaddyourown", R.raw.ch);
		myTTS.addSpeech("ck", "uk.co.flumeland.abcsaddyourown", R.raw.c_k_ck);
		myTTS.addSpeech("d", "uk.co.flumeland.abcsaddyourown", R.raw.d);
		myTTS.addSpeech("dd", "uk.co.flumeland.abcsaddyourown", R.raw.d);
		myTTS.addSpeech("e", "uk.co.flumeland.abcsaddyourown", R.raw.e);
		myTTS.addSpeech("ee", "uk.co.flumeland.abcsaddyourown", R.raw.ee);
		myTTS.addSpeech("eey", "uk.co.flumeland.abcsaddyourown", R.raw.ee);
		myTTS.addSpeech("er", "uk.co.flumeland.abcsaddyourown", R.raw.er_schwa1);
		myTTS.addSpeech("ey", "uk.co.flumeland.abcsaddyourown", R.raw.ee);
		myTTS.addSpeech("f", "uk.co.flumeland.abcsaddyourown", R.raw.f);
		myTTS.addSpeech("g", "uk.co.flumeland.abcsaddyourown", R.raw.g);
		myTTS.addSpeech("gg", "uk.co.flumeland.abcsaddyourown", R.raw.g);
		myTTS.addSpeech("ge", "uk.co.flumeland.abcsaddyourown", R.raw.ge_j);
		myTTS.addSpeech("h", "uk.co.flumeland.abcsaddyourown", R.raw.h);
		myTTS.addSpeech("i", "uk.co.flumeland.abcsaddyourown", R.raw.i);
		myTTS.addSpeech("i-", "uk.co.flumeland.abcsaddyourown", R.raw.ie_igh);
		myTTS.addSpeech("ie", "uk.co.flumeland.abcsaddyourown", R.raw.ie_igh);
		myTTS.addSpeech("j", "uk.co.flumeland.abcsaddyourown", R.raw.ge_j);
		myTTS.addSpeech("k", "uk.co.flumeland.abcsaddyourown", R.raw.c_k_ck);
		myTTS.addSpeech("l", "uk.co.flumeland.abcsaddyourown", R.raw.l);
		myTTS.addSpeech("m", "uk.co.flumeland.abcsaddyourown", R.raw.m);
		myTTS.addSpeech("n", "uk.co.flumeland.abcsaddyourown", R.raw.n);
		myTTS.addSpeech("ng", "uk.co.flumeland.abcsaddyourown", R.raw.ng);
		myTTS.addSpeech("o", "uk.co.flumeland.abcsaddyourown", R.raw.o);
		myTTS.addSpeech("oa", "uk.co.flumeland.abcsaddyourown", R.raw.oa_oe);
		myTTS.addSpeech("oe", "uk.co.flumeland.abcsaddyourown", R.raw.oa_oe);
		myTTS.addSpeech("oul", "uk.co.flumeland.abcsaddyourown", R.raw.oo_short);
		myTTS.addSpeech("oo", "uk.co.flumeland.abcsaddyourown", R.raw.oo_long);
		myTTS.addSpeech("o-", "uk.co.flumeland.abcsaddyourown", R.raw.oo_short);
		myTTS.addSpeech("ough", "uk.co.flumeland.abcsaddyourown", R.raw.oa_oe);
		myTTS.addSpeech("au", "uk.co.flumeland.abcsaddyourown", R.raw.or);
		myTTS.addSpeech("or", "uk.co.flumeland.abcsaddyourown", R.raw.or);
		myTTS.addSpeech("ow", "uk.co.flumeland.abcsaddyourown", R.raw.oa_oe);
		myTTS.addSpeech("-ow", "uk.co.flumeland.abcsaddyourown", R.raw.ow);
		myTTS.addSpeech("oy", "uk.co.flumeland.abcsaddyourown", R.raw.oi_oy);
		myTTS.addSpeech("p", "uk.co.flumeland.abcsaddyourown", R.raw.p);
		myTTS.addSpeech("pp", "uk.co.flumeland.abcsaddyourown", R.raw.p);
		myTTS.addSpeech("ph", "uk.co.flumeland.abcsaddyourown", R.raw.f);
		myTTS.addSpeech("q", "uk.co.flumeland.abcsaddyourown", R.raw.c_k_ck);
		myTTS.addSpeech("qu", "uk.co.flumeland.abcsaddyourown", R.raw.qu_kw);
		myTTS.addSpeech("r", "uk.co.flumeland.abcsaddyourown", R.raw.r);
		myTTS.addSpeech("rr", "uk.co.flumeland.abcsaddyourown", R.raw.r);
		myTTS.addSpeech("s", "uk.co.flumeland.abcsaddyourown", R.raw.s);
		myTTS.addSpeech("sh", "uk.co.flumeland.abcsaddyourown", R.raw.sh);
		myTTS.addSpeech("t", "uk.co.flumeland.abcsaddyourown", R.raw.t);
		myTTS.addSpeech("th", "uk.co.flumeland.abcsaddyourown", R.raw.th_e);
		myTTS.addSpeech("thh", "uk.co.flumeland.abcsaddyourown", R.raw.th_i);
		myTTS.addSpeech("u", "uk.co.flumeland.abcsaddyourown", R.raw.u);
		myTTS.addSpeech("ue", "uk.co.flumeland.abcsaddyourown", R.raw.ue_you);
		myTTS.addSpeech("v", "uk.co.flumeland.abcsaddyourown", R.raw.v);
		myTTS.addSpeech("w", "uk.co.flumeland.abcsaddyourown", R.raw.w);
		myTTS.addSpeech("wh", "uk.co.flumeland.abcsaddyourown", R.raw.w);
		myTTS.addSpeech("wr", "uk.co.flumeland.abcsaddyourown", R.raw.r);
		myTTS.addSpeech("x", "uk.co.flumeland.abcsaddyourown", R.raw.x_ks);
		myTTS.addSpeech("x-", "uk.co.flumeland.abcsaddyourown", R.raw.z);
		myTTS.addSpeech("y", "uk.co.flumeland.abcsaddyourown", R.raw.y);
		myTTS.addSpeech("z", "uk.co.flumeland.abcsaddyourown", R.raw.z);
		myTTS.addSpeech("zz", "uk.co.flumeland.abcsaddyourown", R.raw.z);
		myTTS.addSpeech("isfor", "uk.co.flumeland.abcsaddyourown", R.raw.is_for_question);
		enAllButtons();
	}

	@Override
	/**
	 * A method to handle receipt of a Click notification from the onClickListener
	 */
	public void onClick(View v) {
		Log.i("ABCsMainPlay","onClick() called");
		switch (v.getId()) {
		case R.id.bMVOption1:
			Log.i("ABCsMainPlay","option 1 button pressed");
			testOption(butOption1, 1);
			break;
		case R.id.bMVOption2:
			Log.i("ABCsMainPlay","option 2 button pressed");
			testOption(butOption2, 2);
			break;
		case R.id.bMVOption3:
			Log.i("ABCsMainPlay","option 2 button pressed");
			testOption(butOption3, 3);
			break;
		case R.id.bMVdescend:
			Log.i("ABCsMainPlay","left button pressed");
			getPreceding();
			break;
		case R.id.bMVascend:
			Log.i("ABCsMainPlay","right button pressed");
			getAscending();			
			break;
		case R.id.bMVReveal:
			Log.i("ABCsMainPlay","show button pressed");
			revealName();
			break;
		case R.id.bMVAbc:
			Log.i("ABCsMainPlay","letter sound button pressed");
			playWord();
			break;
		case R.id.bMVRandom:
			Log.i("ABCsMainPlay","random button pressed");
			selectRandom();
			break;
		case R.id.bMVObSound:
			Log.i("ABCsMainPlay","object sound button pressed");
			playSound();
			break;
		}
	}

	/**
	 * A method used to play sound effect files
	 */
	private void playSound() {
		Log.i("ABCsMainPlay","playSound() called");
		if (currObject.hasSoundEffect() == 1) {	
			Uri uriSound = Uri.parse(currObject.getSoundEffectFile());
			//Toast t = Toast.makeText(getApplicationContext(), "PhotoFile" + currObject.getPhotoFile(), Toast.LENGTH_LONG);
			//t.show();
			sndEffect = MediaPlayer.create(this, uriSound);
			sndEffect.start();
		}
	}

	/**
	 * A method to select a random letter of the alphabet and 
	 * initiate loading of a new object and to slide it into view
	 */
	private void selectRandom() {
		Log.i("ABCsMainPlay","selectRandom() called");
		int nextId;
		do {
			nextId = rand.nextInt(ALPHABET_LENGTH)+1;
		} while (nextId == currObject.getIdNo());
		setItem(nextId, identifyNextFlipImage(), identifyNextFlipText(), RANDOM);
		// if current view is imageObject1
		if (viewFlipper.getDisplayedChild() == 0) {
			viewFlipInRightOutLeft();
		} else {
			viewFlipInLeftOutRight();
		}
	}

	/**
	 * A method to load a new object into the hidden viewflipper
	 * @param id		the index of the letter chosen
	 * @param imageview	the imageview to load with the new object image
	 * @param textview	the textview to load with the new object letter
	 * @param call		an int value to id the call type (preceding=-1, next=1, rand=3)
	 */
	private void setItem(int id, ImageView imageview, TextView textview, int call) {
		Log.i("ABCsMainPlay","setItem() called");
		readCategoryChoices();
		currObject = new Item(db.findAndRetrieveLetter(id, call));
		setAlphabetUp();
		setOptionsUp();
		revealed = false;
		String tv = " " + String.valueOf(currObject.getLetter() + " ");
		textview.setText(tv);
		Uri uriPhoto = Uri.parse(currObject.getPhotoFile());
		//Toast t = Toast.makeText(getApplicationContext(), "PhotoFile" + currObject.getPhotoFile(), Toast.LENGTH_LONG);
		//t.show();
		imageview.setImageURI(uriPhoto);
		Log.i("ABCsMainPlay","setItem() hasSoundEffect = " + currObject.hasSoundEffect());
		if (currObject.hasSoundEffect() == 1) {
			butObSound.setEnabled(true);
		} else {
			butObSound.setEnabled(false);
		}
	}

	/**
	 * A method to intiate set of the the option choices play buttons
	 * A random number between 1 & 3 is selected and this number button is
	 * given the correct object answer. The remaing two buttons are forwarded to
	 * the set2nd3rdOption()
	 */
	private void setOptionsUp() {
		Log.i("ABCsMainPlay","setOptionsUp() called");
		if (choices) {
			butOption1.setEnabled(true);
			butOption2.setEnabled(true);
			butOption3.setEnabled(true);
			optChoiceRand = rand.nextInt(3)+1;
			Log.i("ABCsMainPlay","setOptionsUp() rand = " + optChoiceRand);
			switch (optChoiceRand) {
			case 1:
				butOption1.setText(currObject.getName());
				set2nd3rdOption(butOption2, butOption3);
				break;
			case 2:
				butOption2.setText(currObject.getName());
				set2nd3rdOption(butOption1, butOption3);
				break;
			case 3:
				butOption3.setText(currObject.getName());
				set2nd3rdOption(butOption1, butOption2);
				break;
			}
		}
	}

	/**
	 * A method to decide in which order to populate the second and third options buttons
	 * @param butOptionA	id of one of the unused option buttons
	 * @param butOptionB	id of the other unused option button
	 */
	private void set2nd3rdOption(Button butOptionA, Button butOptionB) {
		int opt = rand.nextInt(2)+1;
		Log.i("ABCsMainPlay", "set2nd3rdOption() called, rand = " + opt);
		if (opt == 1) {
			retrieveRandOptions(butOptionA, butOptionB);
		} else {
			retrieveRandOptions(butOptionB, butOptionA);
		}	
	}

	/**
	 * A method to add randomly selected names to the remaining option buttons
	 * @param butOptionA
	 * @param butOptionB
	 */
	private void retrieveRandOptions(Button butOptionA, Button butOptionB) {
		Log.i("ABCsMainPlay", "retrieveRandOptions() called");
		int opt1, opt2;
		do {
			opt1 = rand.nextInt(currItems)+1;
			Log.i("ABCsMainPlay", "opt1 = " + opt1 + "currID = " + currObject.getIdNo());
		} while (opt1 == currObject.getIdNo());
		Log.i("ABCsMainPlay", "retrieveRandOptions() 1st Option = " + opt1);
		butOptionA.setText(db.retrieveOptionName(opt1));
		do {
			opt2 = rand.nextInt(currItems)+1;
			Log.i("ABCsMainPlay", "opt1 = " + opt1 + "opt2 = " + opt2 + "currID = " + currObject.getIdNo());
		} while (opt2 == currObject.getIdNo() || opt2 == opt1);
		Log.i("ABCsMainPlay", "retrieveRandOptions() 2nd Option = " + opt2);
		butOptionB.setText(db.retrieveOptionName(opt2));	
	}

	/**
	 * a method to initiate the wordplayback sequence follow a reveal press
	 * checkLetters is called for each letter in the object word and
	 * aniLetters is called to begin the animation. TTS is used to speak the reveal 
	 */
	private void playWord() {
		Log.i("ABCsMainPlay","playWord() called Thread "  + Thread.currentThread());

		if (revealed) {
			phonicWord = " ";
			identifyCurrFlipText().setText(String.valueOf(currObject.getLetter()));
			Log.i("ABCsMainPlay","playWord() currObject.getLetter() = " + String.valueOf(currObject.getLetter()));
			identifyCurrFlipText().setText(String.valueOf(currObject.getLetter()));		
			for (int i=0; i<currObject.getName().length(); i++) {
				letter = String.valueOf(currObject.getName().charAt(i));
				Log.i("ABCsMainPlay","playWord() letter = " + letter + ", i = " + i);
				i = checkLetters(i);
				if (!((letter.contentEquals(" ")) || (letter.contentEquals("-")))) {
					phonicWord = phonicWord + letter + " ";
				}
				Log.i("ABCsMainPlay","playWord() letter = " + letter + ", i = " + i);
				myTTS.speak(letter, TextToSpeech.QUEUE_ADD, null);
			}
			aniLetters(phonicWord);
			//speak straight away
			String isFor = "isfor";
			myTTS.speak(isFor, TextToSpeech.QUEUE_ADD, null);
			myTTS.setPitch((float) 1.0);
			myTTS.setSpeechRate((float) 0.75);		
			String item = currObject.getName();
			Log.i("ABCsMainPlay", "playWord() item = " + item);
			myTTS.speak(item, TextToSpeech.QUEUE_ADD, null);

		} else {
			letter = String.valueOf(currObject.getName().charAt(0));
			myTTS.speak(letter, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	/**
	 * Method to check/assign phonic sound of each letter
	 * @param i	the index of the letter being checked
	 * @return	the index of the next letter to check
	 */
	private int checkLetters(int i) {
		Log.i("ABCsMainPlay", "checkLetters(), called index = " + i +  ", Thread "  + Thread.currentThread());
		String nxtLetter = null;

		if (i<(currObject.getName().length()-1)) {
			nxtLetter = String.valueOf(currObject.getName().charAt(i+1));
		}  else if (i==(currObject.getName().length()-1)) { // if last letter  check
			if (currObject.getName().equalsIgnoreCase("kiwi")) {
				letter = "ee";
				return i;
			} if (letter.equalsIgnoreCase("e")) {
				String forLetter = String.valueOf(currObject.getName().charAt(i-1));
				if (forLetter.equalsIgnoreCase("g")) {
					if (i>1) {
						String forLetter2 = String.valueOf(currObject.getName().charAt(i-2));
						if (forLetter2.equalsIgnoreCase("n")) {
							letter = "ge";
							Log.i("ABCsMainPlay","checkLetters(), returning g = " + letter + " " + letter);	
							return i;
						}
					}
				}
				letter = "";
				Log.i("ABCsMainPlay","checkLetters(), returning = " + letter + " " + letter);	
				return i;
			} else if (letter.equalsIgnoreCase("o")) {
				String forLetter = String.valueOf(currObject.getName().charAt(i-1));
				if (forLetter.equalsIgnoreCase("t")) {
						letter = "oe";
						Log.i("ABCsMainPlay","checkLetters(), returning o = " + letter + " " + letter);	
						return i;
				}
			} else if (letter.equalsIgnoreCase("y")) {
				i = ifLetterEqualsY(i);		
			} else return i;
		}
		int vwls = findNumOfVwls(currObject.getName());
		Log.i("ABCsMainPlay","checkLetters(), vwls = " + vwls);
		Log.i("ABCsMainPlay","checkLetters(), nextLetter = " + nxtLetter);
		if (letter.equalsIgnoreCase("a")) {			
			Log.i("ABCsMainPlay","checkLetters(), check = a next = " + nxtLetter);		
			if (nxtLetter.equalsIgnoreCase("e")) {
				letter = "ae";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning ae = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("y")) {
				letter = "ay";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning ay = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("p")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("e")) {
						letter = "ai";
						Log.i("ABCsMainPlay","checkLetters(), returning are = " + letter + " " + letter);	
						return i;
					}
				}
			} else if (nxtLetter.equalsIgnoreCase("r")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("e")) {
						letter = "are";
						i = i + 2;
						Log.i("ABCsMainPlay","checkLetters(), returning are = " + letter + " " + letter);	
						return i;
					}
				}
				letter = "ar";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning ar = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("i")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("r")) {
						letter = "air";
						i = i + 2;
						Log.i("ABCsMainPlay","checkLetters(), returning air = " + letter + " " + letter);	
						return i;
					}
				}
				letter = "ai";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning 692 = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("u")) {
				letter = "au";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning 663 = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("w")) {
				if (i!=0) {
					letter = "aw";
					i++;
					Log.i("ABCsMainPlay","checkLetters(), returning 663 = " + letter + " " + letter);	
					return i;
				}
			} else {
				//Do not change
				Log.i("ABCsMainPlay","checkLetters(), returning 698 = " + letter + " " + letter);	
				return i;
			}
		} else if (letter.equalsIgnoreCase("b")) {
			Log.i("ABCsMainPlay","checkLetters(), check = b next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("b")) {
				letter = "bb";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("c")) {
			Log.i("ABCsMainPlay","checkLetters(), check = c next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("e")) {
				letter = "ce";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("h")) {
				letter = "ch";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("k")) {
				letter = "ck";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("d")) {
			Log.i("ABCsMainPlay","checkLetters(), check = b next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("d")) {
				letter = "dd";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("e")) {
			Log.i("ABCsMainPlay","checkLetters(), check = e next = " + nxtLetter);
			if (i==(currObject.getName().length()-2)) { // if last letter but 1 check
				if (nxtLetter.equalsIgnoreCase("s")) {
					letter = "es";
					i++;
					Log.i("ABCsMainPlay","checkLetters(), returning es = " + letter + " " + letter);	
					return i;
				}
			}
			if (nxtLetter.equalsIgnoreCase("a")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("r")) {
						letter = "ear";
						i = i + 2;
						Log.i("ABCsMainPlay","checkLetters(), returning ear = " + letter + " " + letter);	
						return i;
					} else if (nxtLetter2.equalsIgnoreCase("m")) {
						letter = "ee";
						i++;
						Log.i("ABCsMainPlay","checkLetters(), returning ee = " + letter + " " + letter);	
						return i;
					} else if (nxtLetter2.equalsIgnoreCase("d")) {
						letter = "e";
						i = i++;
						Log.i("ABCsMainPlay","checkLetters(), returning e = " + letter + " " + letter);	
						return i;
					}
				}
			} else if (nxtLetter.equalsIgnoreCase("e")) {
				letter = "ee";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("r")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("r")) {	
						return i;
					}
				} else {
					letter = "er";
					i++;
					return i;
				}
			} else if (nxtLetter.equalsIgnoreCase("y")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("e")) {
						letter = "i-";
						i = i + 2;
						return i;
					}
				} else {
					letter = "ee";
					i++;
					return i;
				}
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("g")) {
			Log.i("ABCsMainPlay","checkLetters(), check = g next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("e")) {
				letter = "ge";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("g")) {
				letter = "gg";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("i")) {
			Log.i("ABCsMainPlay","checkLetters(), check = i next = " + nxtLetter);
			if (currObject.getName().equalsIgnoreCase("kiwi")) {
				letter = "ee";
				return i;
			} else if (nxtLetter.equalsIgnoreCase("e")) {
				if (currObject.getName().equalsIgnoreCase("callie")) {
					letter = "ee";
				} else {
				letter = "ie";
				}
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("c")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					if (nxtLetter2.equalsIgnoreCase("e")) {
						letter = "i-";
						Log.i("ABCsMainPlay","checkLetters(), returning i+ = " + letter + " " + letter);	
						return i;
					}
				}
			} else if (i<(currObject.getName().length()-2)) {
				String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
				if (nxtLetter2.equalsIgnoreCase("e")) {
					letter = "i-";
					Log.i("ABCsMainPlay","checkLetters(), returning i+ = " + letter + " " + letter);	
					return i;
				}
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("k")) {
			Log.i("ABCsMainPlay","checkLetters(), check = k next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("n")) {
				letter = "n";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}	
		} else if (letter.equalsIgnoreCase("l")) {
			Log.i("ABCsMainPlay","checkLetters(), check = l next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("l")) {
				letter = "ll";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}	
		} else if (letter.equalsIgnoreCase("n")) {
			Log.i("ABCsMainPlay","checkLetters(), check = n next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("g")) {
				letter = "ng";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("o")) {
			Log.i("ABCsMainPlay","checkLetters(), check = o next = " + nxtLetter);
			if (nxtLetter.equalsIgnoreCase("a")) {
				letter = "oa";
				if (currObject.getName().equalsIgnoreCase("koala")) {
					return i;
				} else {
					i++;
					Log.i("ABCsMainPlay","checkLetters(), returning oe = " + letter + " " + letter);	
					return i;
				}
			} else if (nxtLetter.equalsIgnoreCase("e")) {
				letter = "oe";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning oe = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("o")) {
				letter = "oo";
				i++;
				Log.i("ABCsMainPlay","checkLetters(), returning oo = " + letter + " " + letter);	
				return i;
			} else if (nxtLetter.equalsIgnoreCase("r")) {
				letter = "or";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("s")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					Log.i("ABCsMainPlay","checkLetters(), nxtlet = u next+1 = " + nxtLetter2);
					if (nxtLetter2.equalsIgnoreCase("e")) {
						letter = "oa";
						//i = i + 2;
						Log.i("ABCsMainPlay","checkLetters(), returning oul = " + letter + " " + letter);	
						return i;
					}
				}
			} else if (nxtLetter.equalsIgnoreCase("u")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					Log.i("ABCsMainPlay","checkLetters(), nxtlet = u next+1 = " + nxtLetter2);
					if (nxtLetter2.equalsIgnoreCase("l")) {
						letter = "oul";
						i = i + 2;
						Log.i("ABCsMainPlay","checkLetters(), returning oul = " + letter + " " + letter);	
						return i;
					} else if (nxtLetter2.equalsIgnoreCase("g")) {
						Log.i("ABCsMainPlay","checkLetters(), nxtlet2 = g");
						if (i<(currObject.getName().length()-3)) {
							String nxtLetter3 = String.valueOf(currObject.getName().charAt(i+3));
							Log.i("ABCsMainPlay","checkLetters(), nxtlet2 = g nxt3 = " + nxtLetter3);
							if (nxtLetter3.equalsIgnoreCase("h")) {				
								letter = "ough";
								i = i + 3;
								Log.i("ABCsMainPlay","checkLetters(), returning ough = " + letter + " " + letter);	
								return i;
							}
						}
					} else if (nxtLetter2.equalsIgnoreCase("p")) {
						letter = "oo";
						i++;
						Log.i("ABCsMainPlay","checkLetters(), returning ar = " + letter + " " + letter);	
					} else if (nxtLetter2.equalsIgnoreCase("t")) {
						letter = "ou";
						i++;
						Log.i("ABCsMainPlay","checkLetters(), returning ar = " + letter + " " + letter);	
					}
				}
			} else if (nxtLetter.equalsIgnoreCase("w")) {
				if (currObject.getName().equalsIgnoreCase("cow")) {
					letter = "-ow";
				} else {
					letter = "ow";
				}
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("y")) {
				letter = "oy";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("p")) {
			Log.i("ABCsMainPlay","checkLetters(), check = p next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("p")) {
				letter = "pp";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("h")) {
				letter = "ph";
				i++;
				return i;
			} else{
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("q")) {
			Log.i("ABCsMainPlay","checkLetters(), check = q next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("u")) {
				letter = "qu";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("r")) {
			Log.i("ABCsMainPlay","checkLetters(), check = r next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("r")) {
				letter = "rr";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("s")) {
			Log.i("ABCsMainPlay","checkLetters(), check = s next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("h")) {
				letter = "sh";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("t")) {
			Log.i("ABCsMainPlay","checkLetters(), check = t next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("h")) {
				letter = "th";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("u")) {
			Log.i("ABCsMainPlay","checkLetters(), check = u next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("e")) {
				letter = "ue";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("i")) {
				if (i<(currObject.getName().length()-2)) {
					String nxtLetter2 = String.valueOf(currObject.getName().charAt(i+2));
					Log.i("ABCsMainPlay","checkLetters(), nxtlet = i next+1 = " + nxtLetter2);
					if (nxtLetter2.equalsIgnoreCase("t")) {
						if (currObject.getName().equalsIgnoreCase("guitar")) {
							return i;
						} else {
							letter = "oo";
							i++;
							return i;
						}
					}
				}
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("x")) {
			Log.i("ABCsMainPlay","checkLetters(), check = u next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("y")) {
				letter = "x-";
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("w")) {
			Log.i("ABCsMainPlay","checkLetters(), check = w next = " + nxtLetter);	
			if (nxtLetter.equalsIgnoreCase("h")) {
				letter = "wh";
				i++;
				return i;
			} else if (nxtLetter.equalsIgnoreCase("r")) {
				letter = "wr";
				i++;
				return i;
			} else {
				// Do not change
				return i;
			}
		} else if (letter.equalsIgnoreCase("y")) {
			Log.i("ABCsMainPlay","checkLetters(), check = y next = " + nxtLetter);
			i = ifLetterEqualsY(i);		
		} else if (letter.equalsIgnoreCase("z")) {
			Log.i("ABCsMainPlay","checkLetters(), check = y next = " + nxtLetter);
			if (nxtLetter.equalsIgnoreCase("z")) {
				letter = "zz";
				i++;
				return i;
			}
		}
		return i;
	}

	/**
	 * A method to handle currect letter y
	 * @param i the index of the letter in the word
	 * @return the index of the next letter to examine
	 */
	private int ifLetterEqualsY(int i) {
		Log.i("ABCsMainPlay","ifLetterEqualsY() called");
		if (i>0) {
			String forLetter = String.valueOf(currObject.getName().charAt(i-1));
			if (forLetter.equalsIgnoreCase("l")) {
				if (i>2) {
					String forLetter2 = String.valueOf(currObject.getName().charAt(i-2));
					Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter + "for2 = " + forLetter2);
					if (forLetter2.equalsIgnoreCase("l")) {
						letter = "ey";
						return i;
					} else if (forLetter2.equalsIgnoreCase("f")) {
						letter = "i-";
						return i;
					}
				}
			} else if (forLetter.equalsIgnoreCase("d")) {
				Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter);
				if (i>2) {
					String forLetter2 = String.valueOf(currObject.getName().charAt(i-2));
					Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter + "for2 = " + forLetter2);
					if (forLetter2.equalsIgnoreCase("d")) {
						letter = "ey";						
						return i;
					}
				}
			} else if (forLetter.equalsIgnoreCase("m")) {
				Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter);
				if (i>2) {
					String forLetter2 = String.valueOf(currObject.getName().charAt(i-2));
					Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter + "for2 = " + forLetter2);
					if (forLetter2.equalsIgnoreCase("m")) {
						letter = "ey";						
						return i;
					}
				}
			} else if (forLetter.equalsIgnoreCase("p")) {
				Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter);
				if (i>2) {
					String forLetter2 = String.valueOf(currObject.getName().charAt(i-2));
					Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter + "for2 = " + forLetter2);
					if (forLetter2.equalsIgnoreCase("p")) {
						letter = "ey";						
						return i;
					}
				}
			} else if (forLetter.equalsIgnoreCase("r")) {
				Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter);
				if (i>2) {
					String forLetter2 = String.valueOf(currObject.getName().charAt(i-2));
					Log.i("ABCsMainPlay","checkLetters(), check = y for = " + forLetter + "for2 = " + forLetter2);
					if (forLetter2.equalsIgnoreCase("r")) {
						letter = "ey";						
						return i;
					}
				}
			} else {
				// Do not change
				return i;
			}
		}
		return i;
	}



	/**
	 * act on result of TTS data check
	 * prepare the TTS engine if available
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("ABCsMainPlay","onActivityResult() called");
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				//the user has the necessary data - create the TTS
				myTTS = new TextToSpeech(this, this);
			} else {
				//no data - install it now
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		} else if (requestCode == REQUEST_FINISH) {
			if (resultCode == RESULT_OK) {
				this.finish();
			}
		}
	}

	/**
	 * Find the number of vowels in a word
	 * @param name	String of word to check
	 * @return		the number of vowels (int)
	 */
	private int findNumOfVwls(String name) {
		int vwlCnt = 0;
		for (int i=0;i<name.length();i++) {
			if (name.charAt(i) == 'a'
					|| name.charAt(i) == 'i' 
					|| name.charAt(i) == 'e'
					|| name.charAt(i) == 'o' 
					|| name.charAt(i) == 'u') {
				vwlCnt++;
			}
		}
		return vwlCnt;
	}

	/**
	 * A method to initial object reveal
	 */
	private void revealName() {
		Log.i("ABCsMainPlay","revealName() called");
		revealed = true;
		disAllButtons();
		dimOptions(optChoiceRand);
		playWord();
		addAlphabetLetters();
	}

	/**
	 * A method to enable buttons following a reveal
	 */
	private void enAllButtons() {
		butLeft.setEnabled(true);
		butRight.setEnabled(true);
		butRand.setEnabled(true);
		butAbc.setEnabled(true);
		butReveal.setEnabled(true);
		if (currObject.hasSndEffect == 1) {
			butObSound.setEnabled(true);
		}
		disabled = false;
	}

	/**
	 * A method to disable buttons during reveal etc
	 */
	private void disAllButtons() {
		butLeft.setEnabled(false);
		butRight.setEnabled(false);
		butRand.setEnabled(false);
		butAbc.setEnabled(false);
		butReveal.setEnabled(false);
		butObSound.setEnabled(false);
		disabled = true;
	}

	/**
	 * A method to prepare TTS with locale etc after its initialisation
	 */
	public void onInit(int initStatus) {
		Log.i("ABCsMainPlay","onInit() called");
		//check for successful instantiation
		if (initStatus == TextToSpeech.SUCCESS) {
			Log.i("ABCsMainPlay","onInit() locale = " + Locale.getDefault());
			if (myTTS.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE) {
				myTTS.setLanguage(Locale.UK);
			}
			loadPhonics();
		}
		else if (initStatus == TextToSpeech.ERROR) {
			Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Takes the pronunciation string and places it in the animation textview,
	 * which is then animated to slide left and merge with the letter box
	 * @param word	contains the pronunciation string
	 */
	private synchronized void aniLetters(String word) {
		Log.i("ABCsMainPlay","aniLetters() called");	
		xDest = identifyCurrFlipText().getLeft();
		yLine = identifyCurrFlipText().getTop();
		if (positions == false) {			
			xStart = textAni.getLeft();
			positions = true;
		}

		ObjectAnimator animation1 = ObjectAnimator.ofFloat(textAni,
				"x", xDest-5);     
		animation1.setDuration(currObject.getName().length()*500 + 500);
		ObjectAnimator animation2 = ObjectAnimator.ofFloat(textAni,
				"y", yLine);     
		animation2.setDuration(currObject.getName().length()*500 + 500);
		ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textAni, "alpha",
				0f, 1f);
		fadeIn.setDuration(300);

		AnimatorSet aniSet = new AnimatorSet(); 
		aniSet.playTogether(animation1, animation2, fadeIn);
		if (!(aniRunning)) {
			textAni.setText(phonicWord);
			textAni.setVisibility(View.VISIBLE);
		} else {
			aniWaiting = true;
			while (aniSet.isRunning()) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					Log.i("ABCsMainPlay","aniLetters() wait failed");	
					e.printStackTrace();
				}
			}
			nextAniLetters = letter;
		}
		aniSet.addListener(new Animator.AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				Log.i("ABCsMainPlay","onAnimationStart() called Thread "  + Thread.currentThread());
				aniRunning = true;
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				Log.i("ABCsMainPlay","onAnimationRepeat() called");
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				Log.i("ABCsMainPlay","onAnimationEnd() called Thread "  + Thread.currentThread());
				ObjectAnimator fadeOut = ObjectAnimator.ofFloat(textAni, "alpha", 1f, 0f);
				fadeOut.setDuration(200);
				ObjectAnimator animation3 = ObjectAnimator.ofFloat(textAni, "x", xStart);
				animation3.setDuration(10);
				AnimatorSet resetTB = new AnimatorSet();
				resetTB.play(fadeOut).after(animation3);
				resetTB.start();
				textAni.setVisibility(View.INVISIBLE);
				textAni.setTop((int)yLine);
				textAni.setLeft((int)xStart);
				aniRunning = false;
				addLettersToBox(identifyCurrFlipText());
				enAllButtons();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
			}
		});

		aniSet.start();
	}

	/**
	 * A method to insert a the wholeWord String into a textView after aniLetters()
	 * @param tv
	 */
	private void addLettersToBox(TextView tv) {
		//Log.i("ABCsMainPlay","addLettersToBox() called);
		//tv.setWidth(newSize);
		String wholeWord = " " + currObject.getName() + " ";
		tv.setText(wholeWord);
		// TODO Auto-generated method stub
	}

	/**
	 * A method to identify the imageView in use
	 * @return 	The imageView in use
	 */
	private ImageView identifyCurrFlipImage() {
		Log.i("ABCsMainPlay","identifyCurrFlipImage() called");
		// if current view is imageObject1
		if (viewFlipper.getDisplayedChild() == 0) {
			return imageObject1;
			// if current view is imageObject2
		} else {
			return imageObject2;
		}
	}

	/**
	 * A method to identify the imageView not in use
	 * @return 	The imageView not in use
	 */
	private ImageView identifyNextFlipImage() {
		Log.i("ABCsMainPlay","identifyNextFlipImage() called");
		// if current view is imageObject1
		if (viewFlipper.getDisplayedChild() == 0) {
			return imageObject2;
			// if current view is imageObject2
		} else {
			return imageObject1;
		}
	}

	/**
	 A method to identify the Viewflipper TextView in use
	 * @return 	The imageView in use
	 */
	private TextView identifyCurrFlipText() {
		Log.i("ABCsMainPlay","identifyCurrFlipText() called");
		// if current view is textLetterAlpha1
		if (viewFlipper.getDisplayedChild() == 0) {
			return textLetterAlpha1;
			// if current view is textLetterAlpha2
		} else {
			return textLetterAlpha2;
		}
	}

	/**
	 * method to identify the Viewflipper TextView not in use
	 * @return 	The imageView not in use
	 */
	private TextView identifyNextFlipText() {
		Log.i("ABCsMainPlay","identifyNextFlipText() called");
		// if current view is textLetterAlpha1
		if (viewFlipper.getDisplayedChild() == 0) {
			return textLetterAlpha2;
			// if current view is textLetterAlpha2
		} else {
			return textLetterAlpha1;
		}
	}

	/**
	 * Method to find the next alphabetic letter 
	 * @param letter	the current letter
	 * @return			the next letter in the alphabet or a if letter = z
	 */
	private int findAscendingLetter(char letter) {
		Log.i("ABCsMainPlay","findAscendingLetter() called");
		int j = 1;
		for (int i=0; i<LETS.length; i++) {
			if (letter == LETS[i]) {
				j = i+2;	
			}
		}
		return j;
	}

	/**
	 * Method to find the next alphabetic letter 
	 * @param letter	the current letter
	 * @return			the preceding letter in the alphabet or z if letter = a
	 */
	private int findPrecedingLetter(char letter) {
		Log.i("ABCsMainPlay","findAscendingLetter() called");
		int j = ALPHABET_LENGTH;
		for (int i=0; i<LETS.length; i++) {
			if (letter == LETS[i]) {
				j = i;	
			}
		}
		return j;
	}

	/**
	 * A method to initiate loading a next letter object
	 */
	private void getAscending() {
		Log.i("ABCsMainPlay","getAscending() called");
		int nextId;
		if (currObject.getLetter() == 'z') {
			nextId = 1;
		} else {
			nextId = findAscendingLetter(currObject.getLetter());
		}
		setItem(nextId, identifyNextFlipImage(), identifyNextFlipText(), ASCENDING);
		// set the required Animation type to ViewFlipper
		// The Next screen will come in from Right and current Screen will go OUT from Left
		viewFlipInRightOutLeft();
	}

	/**
	 * A method to initiate loading a preceding letter object
	 */
	private void getPreceding() {
		Log.i("ABCsMainPlay","getPreceding() called");
		int nextId;
		if (currObject.getLetter() == 'a') {
			nextId = ALPHABET_LENGTH;
		} else {
			nextId =  findPrecedingLetter(currObject.getLetter());
		}
		setItem(nextId, identifyNextFlipImage(), identifyNextFlipText(), DESCENDING);	
		// set the required Animation type to ViewFlipper
		// The Next screen will come in from Left and current Screen will go OUT from Right
		viewFlipInLeftOutRight();
	}

	/**
	 * A method to test if an option button pressed is correct
	 * @param button	the button that has been pressed
	 * @param opt		the number of the button pressed
	 */
	private void testOption(Button button, int opt) {
		Log.i("ABCsMainPlay","testOption() called");
		String test = button.getText().toString();
		if (test.equalsIgnoreCase(currObject.getName())) {
			correct(opt);
		}
		else { incorrect(button);
		}
	}

	/**
	 * A method to initiate an incorrect response to an option button press
	 * @param button
	 */
	private void incorrect(Button button) {
		Log.i("ABCsMainPlay","incorrect() called");
		Toast t = Toast.makeText(getApplicationContext(), "Bad Luck", Toast.LENGTH_SHORT);
		t.setGravity(Gravity.TOP, 0, 100);
		t.show();
		soundIncorrect();
		button.setEnabled(false);
		// TODO Auto-generated method stub
	}

	/**
	 * A method to choose and play an incorrect audio sound
	 */
	private void soundIncorrect() {
		int r = rand.nextInt(2)+1;
		Log.i("ABCsMainPlay","soundIncorrect() called r = " + r);		
		switch (r) {
		case 1:
			sndEffect = MediaPlayer.create(this, R.raw.thats_not_right);
			break;
		case 2:
			sndEffect = MediaPlayer.create(this, R.raw.try_again);
			break;
		}
		sndEffect.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				finishPlaying(); // clear up play
			}
		});
		sndEffect.start();	
	}

	/**
	 * A method to choose and play a correct audio sound
	 */
	private void soundCorrect() {
		int r = rand.nextInt(3)+1;
		Log.i("ABCsMainPlay","soundCorrect() called r = " + r);		
		switch (r) {
		case 1:
			sndEffect = MediaPlayer.create(this, R.raw.you_got_it);
			break;
		case 2:
			sndEffect = MediaPlayer.create(this, R.raw.well_done);
			break;
		case 3:
			sndEffect = MediaPlayer.create(this, R.raw.bbbrilliant);
			break;
		}
		sndEffect.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				finishPlaying(); // clear up play
				revealName();
			}
		});
		sndEffect.start();	
	}

	/**
	 * A method to stop and release the mediaPlayer
	 */
	private void finishPlaying() {
		if (sndEffect.isPlaying()) {
			sndEffect.stop();
			sndEffect.release();
			sndEffect = null;
		}
	}

	/**
	 * A method to initiate a correct response to an option button press
	 * @param opt	The number of the option button pressed
	 */
	private void correct(int opt) {
		Log.i("ABCsMainPlay","correct() called");
		soundCorrect();
		Toast t = Toast.makeText(getApplicationContext(), "Found it! - Well done!", Toast.LENGTH_SHORT);
		t.setGravity(Gravity.TOP, 0, 100);		
		t.show();
		dimOptions(opt);
	}

	/**
	 * A method to dim incorrect option buttons on reveal/correct guess
	 * @param opt
	 */
	private void dimOptions(int opt) {
		switch (opt) {
		case 1:
			butOption2.setEnabled(false);
			butOption3.setEnabled(false);
			break;
		case 2:
			butOption1.setEnabled(false);
			butOption3.setEnabled(false);
			break;
		case 3:
			butOption1.setEnabled(false);
			butOption2.setEnabled(false);
			break;	
		}
	}

	@Override
	/**
	 * Clean up on destroy
	 */
	public void onDestroy() {
		Log.i("ABCsMainPlay","onDestroy() called");
		super.onDestroy();
		// Shutdown the TTS engine and mediaPlayer
		//finishPlaying();
		if (myTTS != null) {
			myTTS.stop();
			myTTS.shutdown();
		}
	}

	@Override
	/**
	 * method to dim the Navigation
	 */
	public void onSystemUiVisibilityChange(int visibility) {
		Log.i("ABCsMainPlay","onSystemUiVisibilityChange() called visibility = " + visibility);
		if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
			// The system bars are visible.
			if (Build.VERSION.SDK_INT < 14) {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else {
				decorView = getWindow().getDecorView();
				decorView.setSystemUiVisibility(uiOptions);
			}
		} else {
			// The system bars are NOT visible.	        	
		}
	}
}
