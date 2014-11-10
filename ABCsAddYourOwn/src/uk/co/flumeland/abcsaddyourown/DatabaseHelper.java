package uk.co.flumeland.abcsaddyourown;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {

	// Logcat tag
	private static final String LOG = DatabaseHelper.class.getName();

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "objectsdb";

	// Table Names
	private static final String OBJECT_TABLE = "objects";
	private static final String CATEGORY_TABLE = "cats";

	// Category Table - column names
	public static final String KEY_CAT_ID = "cat_ID";
	public static final String KEY_CATEGORY = "category";

	// Objects Table - column names
	public static final String KEY_OBJ_ID = "obj_ID";
	public static final String KEY_OBJECT = "object_name";
	public static final String KEY_LETTER = "letter";
	public static final String KEY_CATEGORY_ID = "category_id";
	public static final String KEY_PHOTO = "photo";
	public static final String KEY_SOURCE = "object_source";
	public static final String KEY_HAS_SOUND = "has_sound";
	public static final String KEY_SOUND_EFF = "sound_effect";

	public static final int ID_COLUMN = 0;

	public static final int CATEGORY_COLUMN = 1;
	public static final int OBJECT_NAME_COLUMN = 1;

	public static final int LETTER_COLUMN = 2;
	public static final int CATEGORY_ID_COLUMN = 3;
	public static final int PHOTO_COLUMN = 4;
	public static final int SOURCE_COLUMN = 5;
	public static final int HAS_SOUND_COLUMN = 6;
	public static final int SOUND_EFFECT_COLUMN = 7;
	public static final int CAT_COL_COMBINED = 9;
	public static int numberObjects;
	private static final String[] LETTERS = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j","k", "l", "m", 
	"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", 
	"a", "b", "c", "d", "e", "f", "g", "h", "i", "j","k", "l", "m", 
	"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
	"a", "b", "c", "d", "e", "f", "g", "h", "i", "j","k", "l", "m", 
	"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
	"a", "a", "b", "b", "b", "c", "e", "e", "e", "f","f", "f", "h", 
	"h", "k", "l", "l", "n", "n", "s", "s", "t", "t", "t", "w", "x",
	"q" };
	private static final int ALPHABET_LENGTH = 26;

	public static List<Category> categories = new ArrayList<Category>();
	boolean found;
	public static List<String> allItems = new ArrayList<String>();
	private static SharedPreferences settings;


	// Table Create Statements
	// Category table create statement
	private static final String CREATE_CAT_TABLE = "CREATE TABLE "
			+ CATEGORY_TABLE + " (" + KEY_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_CATEGORY
			+ " TEXT"  + ")";

	// Tag table create statement
	private static final String CREATE_OBJECTS_TABLE = "CREATE TABLE " + OBJECT_TABLE
			+ " (" + KEY_OBJ_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_OBJECT
			+ " TEXT," + KEY_LETTER + " TEXT," + 
			KEY_CATEGORY_ID + " INTEGER, " + KEY_PHOTO + " TEXT,"
			+ KEY_SOURCE + " INT," + KEY_HAS_SOUND + " INT," + KEY_SOUND_EFF + " TEXT" + 
			", FOREIGN KEY (" + KEY_CATEGORY_ID + ") REFERENCES " + CATEGORY_TABLE + "(" + KEY_CAT_ID + "))";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	/**
	 * A method to create the database tables
	 */
	public void onCreate(SQLiteDatabase db) {	
		db.execSQL("PRAGMA foreign_keys = ON;");
		db.execSQL(CREATE_CAT_TABLE);
		db.execSQL(CREATE_OBJECTS_TABLE);
	}

	@Override
	/**
	 * A method to drop old table when a new version is available
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + OBJECT_TABLE);
		onCreate(db);
	}

	// ----------------------------- OBJECTS table methods ----------------------------- //

	/**
	 * Checks if the database exists and is populated
	 * @return	The number of objects in the database
	 * @throws Exception
	 */
	public int prepareDatabaseContent() throws Exception {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		String selectQuery = "SELECT  * FROM " + OBJECT_TABLE;
		Log.i(LOG, selectQuery);	 
		Cursor c = db.rawQuery(selectQuery, null);
		numberObjects = c.getCount();
		if (numberObjects == 0) {
			populateDatabase();
			c = db.rawQuery(selectQuery, null);
			numberObjects = c.getCount();
		}
		c.close();
		return numberObjects;
	}

	/**
	 * Add the default items to the database
	 */
	private void populateDatabase() {
		Log.i("DatabaseHelper","populateDatabase() called");
		String[] catsA = { "animal", "food", "object", "body", "people" };
		String[] names = { "ant", "butterfly", "cat", "dog", "elephant", "fish", 
				"goat", "horse", "iguana", "jellyfish", "koala", "lizard", "monkey", 
				"newt", "orangutan", "pig", "quail", "rabbit", "swan", "tortoise", 
				"uakari", "vulture", "wolf", "x-ray tetra", "yak", "zebra", 
				"apple", "banana", "cheese", "doughnut", "egg", "fish", "grapes", 
				"ham", "ice-cream", "jelly", "kiwi", "lemon", "milk", "nut", "orange", 
				"pizza", "quince", "raisins", "strawberry", "tomato", "ugli-fruit", 
				"vegetables", "watermelon", "x hot sauce", "yogurt", "zucchini",
				"aeroplanes", "ball", "camera", "door", "envelope", "feather", "guitar", 
				"hat", "ice", "jug", "kite", "leaf", "mobile", "nail", "oven",
				"pen", "quarter", "rocket", "swing", "train", "umbrella", "vest", 
				"watch", "xylophone", "yellow rose", "zip",
				"ankle", "arm", "back", "beard", "belly", "cheek", "ear", "elbow", "eye", 
				"face", "feet", "finger", "hair", "hand", "knee", "legs", "lips", "nail", 
				"nose", "shin", "shoulder", "teeth", "toes", "tongue", "wrist", "x-ray", 
				"queen" };

		String[] photo = { "android.resource://uk.co.flumeland.abcsaddyourown/drawable/ant", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/butterfly",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/cat", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/dog", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/elephant", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/fish", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/goat", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/horse", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/iguana", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/jellyfish", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/koala", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/lizard", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/monkey", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/newt", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/orangutan",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/pig", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/quail", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/rabbit", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/swan", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/tortoise", 	
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/uakari", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/vulture", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/wolf", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/x_ray_tetra", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/yak", 
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/zebra",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/apple",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/banana",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/cheese",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/doughnut",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/egg",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/fish_fd",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/grapes",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ham",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ice_cream",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/jelly",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/kiwi",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/lemon",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/milk",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/nut",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/orange",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/pizza",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/quince",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/raisins",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/strawberry",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/tomato",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ugli_fruit",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/vegetables",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/watermelon",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/x_hot_sauce",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/yogurt",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/zucchini",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/aeroplanes",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ball",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/camera",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/door",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/envelope",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/feather",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/guitar",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/hat",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ice",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/jug",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/kite",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/leaf",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/mobile",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/nail",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/oven",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/pen",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/quarter",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/rocket",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/swing",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/train",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/umbrella",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/vest",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/watch",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/xylophone",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/yellow_rose",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/zip",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ankle",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/arm",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/back",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/beard",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/belly",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/cheek",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/ear",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/elbow",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/eye",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/face",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/feet",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/finger",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/hair",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/hand",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/knee",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/legs",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/lips",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/nail_bd",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/nose",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/shin",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/shoulder",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/teeth",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/toes",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/tongue",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/wrist",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/x_ray",
				"android.resource://uk.co.flumeland.abcsaddyourown/drawable/queen"
				};
		int[] hasSounds = { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 1, 
				0, 1, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 
				1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0,
				1, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 0, 
				1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1,
				0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 
				1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0,
				0 };
		String[] soundPath = { "android.resource://uk.co.flumeland.abcsaddyourown/raw/ant",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/butterfly", 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/cat", 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/dog", 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/elephant", 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/fish", 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/goat", 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/horse", 
				null, null, null, null, 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/monkey", 
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/orangutan",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/pig",
				null, null, null, null, null, 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/vulture",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/fish",
				null, null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/apple",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/banana",
				null, null, 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/egg", 
				null, null, null, null, null, null, null, null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/nut",
				null, null, null, null, null, null, null, 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/crunch2",  
				null, null, null, null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/aeroplane",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/ball",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/camera",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/door",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/envelope",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/guitar",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/ice",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/flap_flag_umb",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/leaves",
				null, 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/hammering", 
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/pen",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/rocket",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/train",
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/flap_flag_umb",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/watch",
				null, null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/zip",
				null, null, null, null, null, 
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/cheek_pop",
				null, null, null, null, null, null, null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/claps",
				null, null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/kiss",
				null,
				"android.resource://uk.co.flumeland.abcsaddyourown/raw/nose_blow",
				null, null, null, null, null, null, null,
				null };
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		ContentValues catValues = new ContentValues();
		for (int i=0; i<catsA.length; i++) {
			Log.i("DatabaseHelper","populating database: adding " + catsA[i]);
			catValues.put(KEY_CATEGORY, catsA[i]);
			long catId = db.insert(CATEGORY_TABLE, null, catValues);
			Log.i("DatabaseHelper","populating database: added at " + catId);
		}
		ContentValues objValues = new ContentValues();
		for (int i=0; i<names.length; i++) {
			int j = (i/26)+1;
			Log.i("DatabaseHelper","populating database: i = " + i + " j = " + j);
			Log.i("DatabaseHelper","populating database: adding " + names[i] + " SE " + hasSounds[i]);
			objValues.put(KEY_OBJECT, names[i]);
			objValues.put(KEY_LETTER, LETTERS[i]);
			objValues.put(KEY_CATEGORY_ID, j);
			objValues.put(KEY_PHOTO, photo[i]);
			objValues.put(KEY_SOURCE, 0);
			objValues.put(KEY_HAS_SOUND, hasSounds[i]);
			objValues.put(KEY_SOUND_EFF, soundPath[i]);
			long objectId = db.insert(OBJECT_TABLE, null, objValues);
			Log.i("DatabaseHelper","populating database: added at " + objectId);
		}
	}

	/**
	 * Adds a new object to the database
	 * @param objectName	The name of the new object 
	 * @param letter		The first letter of the new object
	 * @param categoryID 	The category id number of the object
	 * @param photoFile		The path to the photofile
	 * @param objectSource	The objectSource (1 if user added 0 otherwise)
	 * @param hasSound		Indicates if a sound effect is included (1 for sound 0 otherwise)
	 * @param soundFile		The path to the soundeffect if present else null
	 * @return		The id number of the object saved
	 */
	public long addNewObject (String objectName, String letter, int categoryID, String photoFile, int objectSource, int hasSound, String soundFile) {
		Log.i("DataBaseHelper","addNewObject() called " + objectName);
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		ContentValues newRow = new ContentValues();
		newRow.put(KEY_OBJECT, objectName);
		newRow.put(KEY_LETTER, letter);
		newRow.put(KEY_CATEGORY_ID, categoryID);
		newRow.put(KEY_PHOTO, photoFile);
		newRow.put(KEY_SOURCE, objectSource);
		newRow.put(KEY_HAS_SOUND, hasSound);
		newRow.put(KEY_SOUND_EFF, soundFile);
		// insert row
		long objectId = db.insert(OBJECT_TABLE, null, newRow);
		return objectId;
	}
	
	/**
	 * Loads the name of all objects into the allItems String Arraylist
	 */
	public void getAllItems() {
		Log.i("DatabaseHelper","getAllItems() called");
		String selectQuery = "SELECT * FROM " + OBJECT_TABLE;	 
		Log.i(LOG, selectQuery); 
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		Cursor c = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		allItems.clear();
		if (c != null && c.getCount()>0) {
			c.moveToFirst(); 
			do {
				Log.i("DatabaseHelper","getAllItems() name = " + c.getString(OBJECT_NAME_COLUMN));
				String s = (c.getString(OBJECT_NAME_COLUMN));				
				allItems.add(s);
			} while (c.moveToNext());
			Log.i("DatabaseHelper","getAllItems() allItems.size = " + allItems.size());
		}
	}
	
	/**
	 * Retrieves the next item based on Object Id number
	 * @param id	The id number of the Object required
	 * @return		The next object found
	 */
	public Item findAndRetrieveId(int id) {
		Log.i("DatabaseHelper","findAndRetrieveId() called " + id);
		Item obj = null;
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		String selectQuery = "SELECT  * FROM " + OBJECT_TABLE + ", " + CATEGORY_TABLE + " WHERE "
				+ KEY_OBJ_ID + " = " + id + " AND " + KEY_CATEGORY_ID + " = " + KEY_CAT_ID;
		Log.i(LOG, selectQuery);	 
		Cursor c = db.rawQuery(selectQuery, null);
		if (c != null && c.getCount()>0) {
			c.moveToFirst();
			Log.i("DatabaseHelper","No of columns c = " + c.getColumnCount());
			Log.i("DatabaseHelper","No of rows c = " + c.getCount());
			for (int i=0; i<c.getColumnCount(); i++) {
			Log.i("DatabaseHelper","c = " + c.getColumnName(i));
			}
			do {
				Log.i("DatabaseHelper","id = " + id + " soundfile, " + c.getString(SOUND_EFFECT_COLUMN));
				Log.i("ObjConProv","id = " + id + "has sound, " + c.getString(HAS_SOUND_COLUMN));
				if (c.getInt(ID_COLUMN) == id) {
					Log.i("DatabaseHelper","id = " + id + " name " + c.getString(OBJECT_NAME_COLUMN));
					String name = (c.getString(OBJECT_NAME_COLUMN));
					Log.i("DatabaseHelper","id = " + id + " letter " + c.getString(LETTER_COLUMN));
					char letter = (c.getString(LETTER_COLUMN).charAt(0));
					Log.i("DatabaseHelper","id = " + id + " catId " + c.getString(CATEGORY_ID_COLUMN));
					int catId = (c.getInt(CATEGORY_ID_COLUMN));
					Log.i("DatabaseHelper","id = " + id + " source " + c.getString(SOURCE_COLUMN));
					int source = (c.getInt(SOURCE_COLUMN));
					String photoPath = (c.getString(PHOTO_COLUMN));
					int hasSound =(c.getInt(HAS_SOUND_COLUMN));
					String sndEffFile = (c.getString(SOUND_EFFECT_COLUMN));
					Log.i("DatabaseHelper","id = " + id + " category, " + c.getString(CAT_COL_COMBINED));
					String category = (c.getString(CAT_COL_COMBINED));
					obj = new Item(id, name, letter, category, catId, photoPath, source, hasSound, sndEffFile);		
				}
			}
			while (c.moveToNext());
		}
		return obj;
	}


	/**
	 * pre: id must be a number between 1 & 26
	 * post: the next item is returned
	 * Find next object by letter
	 * LETTERS[] is an array of the alphabet (i.e. where a=0, b=1, etc) 
	 * @param id The id number of the letter to be searched for
	 * @param call signifies the type of caller, ascending +1 descending -1 random 3, used if id does not return a result
	 * @return the next object found
	 */
	public Item findAndRetrieveLetter(int id, int call) {
		Log.i("DatabaseHelper","findAndRetrieveLetter() called " + id);
		String cChosen = MainPlay.readCategoryChoices();	
		Item obj = null;
		SQLiteDatabase db = this.getReadableDatabase();
		int choice = 0;
		db.execSQL("PRAGMA foreign_keys = ON;");
		String selectQuery = "SELECT  * FROM " + OBJECT_TABLE + ", " + CATEGORY_TABLE + " WHERE "
				+ KEY_LETTER + " = '" + LETTERS[id-1] + "' AND " 
				+ KEY_CATEGORY_ID + " = " + KEY_CAT_ID + " AND " 
				+ KEY_CAT_ID + " IN (" + cChosen + ")";				
		Log.i(LOG, selectQuery);	 
		Cursor c = db.rawQuery(selectQuery, null);
		if (c != null && c.getCount()>0) {
			c.moveToFirst();
			Log.i("DatabaseHelper","No of columns c = " + c.getColumnCount());
			Log.i("DatabaseHelper","No of rows c = " + c.getCount());
			for (int i=0; i<c.getColumnCount(); i++) {
				Log.i("DatabaseHelper","c = " + c.getColumnName(i));
			}
			if (c.getCount()>1) {
				Random rand = new Random();
				choice = rand.nextInt(c.getCount())+1;
				Log.i("DatabaseHelper","findAndRetrieveLetter() choice = " + choice);
				c.moveToPosition(choice-1);
			}

			Log.i("DatabaseHelper","id = " + id + " soundfile, " + c.getString(SOUND_EFFECT_COLUMN));
			Log.i("ObjConProv","id = " + id + " has sound, " + c.getString(HAS_SOUND_COLUMN));
			if (c.getString(LETTER_COLUMN).equalsIgnoreCase(LETTERS[id-1])) {
				Log.i("DatabaseHelper","idNo. = " + id);
				int idNo = id;
				Log.i("DatabaseHelper","id = " + id + " name " + c.getString(OBJECT_NAME_COLUMN));
				String name = (c.getString(OBJECT_NAME_COLUMN));
				Log.i("DatabaseHelper","id = " + id + " letter " + c.getString(LETTER_COLUMN));
				char letter = (c.getString(LETTER_COLUMN).charAt(0));
				Log.i("DatabaseHelper","id = " + id + " catId " + c.getString(CATEGORY_ID_COLUMN));
				int catId = (c.getInt(CATEGORY_ID_COLUMN));
				Log.i("DatabaseHelper","id = " + id + " source " + c.getString(SOURCE_COLUMN));
				int source = (c.getInt(SOURCE_COLUMN));
				String photoPath = (c.getString(PHOTO_COLUMN));
				int hasSound =(c.getInt(HAS_SOUND_COLUMN));
				String sndEffFile = (c.getString(SOUND_EFFECT_COLUMN));
				Log.i("DatabaseHelper","id = " + id + " category, " + c.getString(CAT_COL_COMBINED));
				String category = (c.getString(CAT_COL_COMBINED));
				obj = new Item(idNo, name, letter, category, catId, photoPath, source, hasSound, sndEffFile);		
			}
		} else {
			if ((id+call)>ALPHABET_LENGTH) {
				Log.i("DatabaseHelper","findAndRetrieveLetter() id+call = " + id+call);
				id = id-ALPHABET_LENGTH;
			} else if ((id+call) < 0) {
				id = ALPHABET_LENGTH+id;
			}
			return findAndRetrieveLetter(id+call, call);
		}
		return obj;
	}

	/**
	 * retrieves object by object name search
	 * @param itemName	The name of the object to search for
	 * @return returns the object found or a 'not found' object
	 */
	public Item findAndRetrieveName(String itemName) {
		Log.i("DatabaseHelper","findAndRetrieve() called " + itemName);
		Item obj = null;
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		String selectQuery = "SELECT  * FROM " + OBJECT_TABLE + ", " + CATEGORY_TABLE + " WHERE "
				+ KEY_OBJECT + " = '" + itemName + "' AND " + KEY_CATEGORY_ID + " = " + KEY_CAT_ID;
		Log.i(LOG, selectQuery);	 
		Cursor c = db.rawQuery(selectQuery, null);
		if (c != null && c.getCount()>0) {
			c.moveToFirst();
			Log.i("DatabaseHelper","No of columns c = " + c.getColumnCount());
			Log.i("DatabaseHelper","No of rows c = " + c.getCount());
			for (int i=0; i<c.getColumnCount(); i++) {
				Log.i("DatabaseHelper","c = " + c.getColumnName(i));
			}
			do {
				Log.i("DatabaseHelper", "name = " + itemName + " soundfile, " + c.getString(SOUND_EFFECT_COLUMN));
				Log.i("DatabaseHelper", "name = " + itemName + ", has sound, " + c.getString(HAS_SOUND_COLUMN));
				if (c.getString(OBJECT_NAME_COLUMN).equalsIgnoreCase(itemName)) {
					int id =  c.getInt(ID_COLUMN);
					Log.i("DatabaseHelper","id = " + c.getInt(ID_COLUMN) + " name " + c.getString(OBJECT_NAME_COLUMN));
					String name = (c.getString(OBJECT_NAME_COLUMN));
					Log.i("DatabaseHelper","id = " +  c.getInt(ID_COLUMN) + " letter " + c.getString(LETTER_COLUMN));
					char letter = (c.getString(LETTER_COLUMN).charAt(0));
					Log.i("DatabaseHelper","id = " + c.getInt(ID_COLUMN) + " catId " + c.getString(CATEGORY_ID_COLUMN));
					int catId = (c.getInt(CATEGORY_ID_COLUMN));
					Log.i("DatabaseHelper","id = " + c.getInt(ID_COLUMN) + " source " + c.getString(SOURCE_COLUMN));
					int source = (c.getInt(SOURCE_COLUMN));
					String photoPath = (c.getString(PHOTO_COLUMN));
					int hasSound =(c.getInt(HAS_SOUND_COLUMN));
					String sndEffFile = (c.getString(SOUND_EFFECT_COLUMN));
					Log.i("DatabaseHelper","id = " + c.getInt(ID_COLUMN) + " category, " + c.getString(CAT_COL_COMBINED));
					String category = (c.getString(CAT_COL_COMBINED));
					obj = new Item(id, name, letter, category, catId, photoPath, source, hasSound, sndEffFile);		
				}
			}
			while (c.moveToNext());
			c.close();
		} else {
			obj = new Item(-1, "NotFound", 'x', "NotFound", -1, "NotFound", -1, -1, "NotFound");
		}
		return obj;
	}
	
	/**
	 * Pre:	requires the id number to be valid object Id in object table
	 * Post: returns the name of an object in the database
	 * Find the name of a object from an id number given, used for the guess button incorrect options
	 * @param id	The id number of the object selected
	 * @return 		The name of the object found (String)
	 */
	public String retrieveOptionName(int id) {
		Log.i("DatabaseHelper","retrieveOptionName() called " + id);
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		String selectQuery = "SELECT  * FROM " + OBJECT_TABLE + " WHERE "
				+ KEY_OBJ_ID + " = " + id;
		Log.i(LOG, selectQuery);	 	    
		String found = "error";
		Cursor c = db.rawQuery(selectQuery, null);
		if (c != null && c.getCount()>0) {
			c.moveToFirst(); 
			do {
				Log.i("DatabaseHelper","id = " + id + "name, " + c.getString(OBJECT_NAME_COLUMN));
				if (c.getInt(ID_COLUMN) == id) {
					found = c.getString(OBJECT_NAME_COLUMN);
				}
			}
			while (c.moveToNext());
		}
		return found;
	}

	/**
	 * Gets the total number of objects in the objects table
	 * @return		The total number of objects
	 */
	public int findNumEnt() {
		String countQuery = "SELECT  * FROM " + OBJECT_TABLE;
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		Cursor c = db.rawQuery(countQuery, null);
		int numberObjects = c.getCount();
		c.close();
		// return count
		return numberObjects;
	}

	/**
	 * Replaces object in the database with object given
	 * replace row found by match made with object name
	 * @param object	The object that should replace the object in the database
	 * @return		The id number of the row updated
	 */
	public int updateObject(Item object) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		ContentValues updateRow = new ContentValues();
		updateRow.put(KEY_OBJECT, object.getName());
		updateRow.put(KEY_LETTER, String.valueOf(object.getLetter()));
		updateRow.put(KEY_CATEGORY_ID, object.getCatId());
		updateRow.put(KEY_PHOTO, object.getPhotoFile());
		updateRow.put(KEY_SOURCE, object.getSource());
		updateRow.put(KEY_HAS_SOUND, object.hasSoundEffect());
		updateRow.put(KEY_SOUND_EFF, object.getSoundEffectFile());
		// updating row
		return db.update(OBJECT_TABLE, updateRow, KEY_OBJECT + " = ?",
				new String[] { String.valueOf(object.getName()) });
	}

	/**
	 * Deletes an object from the database
	 * @param objectName	The name of the object to be deleted
	 */
	public void deleteObject(String objectName) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		db.delete(OBJECT_TABLE, KEY_OBJECT + " = ?",
				new String[] { objectName });
	}



	// ----------------------------- CATEGORIES table methods ----------------------------- //

	/**
	 * Finds the category id number of a category
	 * @param category		The name of the category to search for
	 * @return		The id number of the category
	 */
	public int findCategory(String category) {
		Log.i("DatabaseHelper","findCategory() called");
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		int cId = -1;
		String selectQuery = "SELECT  * FROM " + CATEGORY_TABLE + " WHERE "
				+ KEY_CATEGORY + " = '" + category + "';";
		Log.i(LOG, selectQuery);	 
		Cursor c = db.rawQuery(selectQuery, null);
		Log.i("DatabaseHelper","findCategory() called c = " + c.toString());
		if (c != null && c.getCount()>0) {
			c.moveToFirst(); 
			do {
				Log.i("DatabaseHelper","category = " + c.getString(CATEGORY_COLUMN));
				Log.i("DatabaseHelper","catId = " + c.getInt(ID_COLUMN));
				Log.i("DatabaseHelper","Category Found id = " + c.getInt(ID_COLUMN));
				cId = (c.getInt(ID_COLUMN));
				return cId;
			} while (c.moveToNext());
		} 
		return cId;
	}



	/**
	 * Loads all categories into the categories <Category> Arraylist
	 */
	public void loadCategories() {
		Log.i("DatabaseHelper","loadCategories() called");
		found = false;
		String selectQuery = "SELECT * FROM " + CATEGORY_TABLE;	 
		Log.i(LOG, selectQuery); 
		SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		Cursor c = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		int cId;
		categories.clear();
		if (c != null && c.getCount()>0) {
			c.moveToFirst(); 
			do {
				Log.i("DatabaseHelper","loadCategories() category = " + c.getString(CATEGORY_COLUMN));
				Log.i("DatabaseHelper","loadCategories() catId = " + c.getInt(ID_COLUMN));
				cId = (c.getInt(ID_COLUMN));
				String s = (c.getString(CATEGORY_COLUMN));
				Category cat = new Category(s, cId);
				categories.add(cat);
				Log.i("DatabaseHelper","loadCategories() adding catName = " + cat.getCategory() + ", catId = " + cat.getCatId());
			} while (c.moveToNext());
			Log.i("DatabaseHelper","loadCategories() categories.size = " + categories.size());
		}
	}

	/**
	 * Adds a new category to the category table
	 * @param catName
	 * @return
	 */
	public long addNewCategory (String catName) {
		Log.i("DataBaseHelper","addNewCategory() called " + catName);
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys = ON;");
		ContentValues newRow = new ContentValues();
		newRow.put(KEY_CATEGORY, catName);
		// insert row
		long catId = db.insert(CATEGORY_TABLE, null, newRow);
		return catId;
	}

}
