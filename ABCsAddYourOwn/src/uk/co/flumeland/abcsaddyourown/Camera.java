package uk.co.flumeland.abcsaddyourown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Camera extends Activity implements View.OnClickListener {

	protected static final int PHOTO_PICKED = 0;
	protected static final int PHOTO_TAKEN = 1;
	protected static final int PIC_CROP = 2;
	protected static final int OUTPUT = 720;
	protected static final int ASPECT = 1;
	private static final String JPEG_FILE_PREFIX = "ABCsAYO_temp";
	private static final String JPEG_FILE_SUFFIX = ".jpg";	
	private static final String PICS_DIRECTORY = "/dcim/";
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final int SOURCE_USER = 1;
	private static final int NO_SOUND = 0;


	Button butTakePic;
	Button butAddPict;
	Button butSaveToDB;
	EditText objEntName;
	AutoCompleteTextView objEntCat;
	ImageView imagePhotoToAdd;
	Typeface tf;
	Uri newUri;
	private String tempFilePath;
	private Bitmap bmNewPhoto;
	private String itemNewName = "";
	private String itemNewCategory = "";
	private String newFileName = "";
	private String newFileNamePath = "";
	private boolean pictWaiting = false;
	File newPhoto;
	File fPictDir;
	private DatabaseHelper db;
	Bitmap bitmapFinal;
	private List<String> categories = new ArrayList<String>();
	




	@Override
	/**
	 * A method to inflate and prepare activity_photo and set up various variables
	 */
	public void onCreate(Bundle savedInstanceState) {
		Log.i("ABCsCamera","onCreate() called");
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);

		db = new DatabaseHelper(getApplicationContext());
		tf = Typeface.createFromAsset(getAssets(), "fonts/abeezee/ABeeZee-Regular.otf");
		butTakePic = (Button) findViewById(R.id.bCVTakePict);
		butAddPict = (Button) findViewById(R.id.bCVFilePict);
		butSaveToDB = (Button) findViewById(R.id.bCVSaveToDB);
		objEntName = (EditText) findViewById(R.id.etCVObjectName);
		objEntCat = (AutoCompleteTextView) findViewById(R.id.etCVCatName);
		imagePhotoToAdd = (ImageView) findViewById(R.id.ivCVImportPict);
		butTakePic.setTypeface(tf, 1);
		butAddPict.setTypeface(tf, 1);
		butSaveToDB.setTypeface(tf, 1);
		objEntName.setTypeface(tf, 1);
		objEntCat.setTypeface(tf, 1);
		butTakePic.setOnClickListener(this);
		butAddPict.setOnClickListener(this);
		butSaveToDB.setOnClickListener(this);
		objEntName.setOnClickListener(this);
		objEntCat.setOnClickListener(this);
		
		prepareCategoriesForAutoFill();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, categories);
        objEntCat.setAdapter(adapter);
	}

	/**
	 * This method loads the Class Arraylist with the current category names
	 * These are given as autoprompts when entering the caterory name for saving
	 * It should help the user from unintentionally saving an object with a slightly
	 * different category name
	 */
	private void prepareCategoriesForAutoFill() {
		categories.clear();
		db.loadCategories();
		for (int i=0; i<db.categories.size(); i++) {
			categories.add(i, db.categories.get(i).getCategory());
			Log.i("ABCsCamera","categories i " + i + " = " + categories.get(i));
		}
	}

	@Override
	/**
	 * A method to handle receipt of a Click notification from the onClickListener
	 */
	public void onClick(View v) {
		Log.i("ABCsCamera","onClick() called");
		switch (v.getId()) {
		case R.id.bCVTakePict:
			takeCameraPict();
			break;
		case R.id.bCVFilePict:
			getFilePict();
			break;
		case R.id.bCVSaveToDB:
			initSaveToDB();
			break;
			// TODO Auto-generated method stub
		}
	}

	/**
	 * A method that begins preparations for saving to the database
	 * It checks that a picture has been picked or taken
	 * It performs limited checks that a valid name for the object and category has been entered
	 * It checks if a new category has been entered or an existing one
	 * It creates and empty file location and passes this on to the saving method
	 */
	private void initSaveToDB() {
		Log.i("ABCsCamera","initSaveToDB() called, pictWaiting = " + pictWaiting);
		if (pictWaiting == false)  {
			Toast t = Toast.makeText(getApplicationContext(), "Please take or choose a picture before saving", Toast.LENGTH_LONG);
			t.setGravity(Gravity.TOP, 0, 50);
			t.show();
		} else {
			prepareNames();
			File itemPicFile = getPermStorageDir();

			//Toast t = Toast.makeText(getApplicationContext(), "Name length = " + itemNewName.length() + ", Category length = " + itemNewCategory.length(), Toast.LENGTH_LONG);
			//t.setGravity(Gravity.CENTER, 0, 0);		
			//t.show();
			if (itemNewName.length() < 2 || itemNewCategory.length() < 2) {
				Toast t2 = Toast.makeText(getApplicationContext(), "Please fill in the item name and category name to allow saving", Toast.LENGTH_LONG);
				t2.setGravity(Gravity.TOP, 0, 50);		
				t2.show();
				resetNames();
			} else {
				Toast t2 = Toast.makeText(getApplicationContext(), "Name = " + itemNewName + ", Category = " + itemNewCategory, Toast.LENGTH_LONG);
				t2.setGravity(Gravity.TOP, 0, 50);		
				t2.show();
				int catId = -1;
				catId = db.findCategory(itemNewCategory);
				Log.i("ABCsCamera","initSaveToDB() return from DatabaseHelper");
				if (catId != -1) {
					Log.i("ABCsCamera","initSaveToDB() found catId = " + catId);
					Toast t = Toast.makeText(getApplicationContext(), "Category found saving item", Toast.LENGTH_SHORT);
					t.setGravity(Gravity.TOP, 0, 50);
					t.show();
					completeSave(itemPicFile, catId);
				} else {
					Log.i("ABCsCamera","initSaveToDB() found catId = " + catId);
					Toast t = Toast.makeText(getApplicationContext(), "Category not found, adding new category and saving", Toast.LENGTH_SHORT);
					t.setGravity(Gravity.TOP, 0, 50);
					t.show();
					catId = (int) db.addNewCategory (itemNewCategory);
					completeSave(itemPicFile, catId);				
				}		
			}
		}
	}

	/**
	 * A method to create a empty File for permanent storage
	 * @returns the new file created
	 */
	private File getPermStorageDir() {
		File itemPicFile = new File(getExternalFilesDir(null), itemNewName + itemNewCategory + MainPlay.totalItems+1);
		return itemPicFile;
	}
	
	/**
	 * A method to lock the screen orientation
	 */
	private void lockOrient() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
		   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
		else {
		   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}
	
	/**
	 * A method to unlock the screen orientation
	 */
	private void unlockOrient() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
	}

	/**
	 * A method to complete the saving of the file
	 * @param itemPicFile	The empty file where the image will be placed
	 * @param catId The category id of the new image
	 */
	private void completeSave(File itemPicFile, int catId) {
		saveNewObjectImage(itemPicFile);
		addObjectToDBObjectTable(itemPicFile, catId);
		unlockOrient();
		String gTSAD = "gTSAD";
		new GoToSndAlertDialog().show(getFragmentManager(), gTSAD);	
	}

	/**
	 * A method to initiate placing a record of the image in the object database
	 * @param itemPicFile	The file where the image resides
	 * @param catId 		The category id of the new image	 
	 */
	private void addObjectToDBObjectTable(File itemPicFile, int catId) {
		newFileNamePath = itemPicFile.getAbsolutePath();
		Log.i("ABCsCamera","initSaveToDB() newFilePathname = " + newFileNamePath);
		db.addNewObject(itemNewName, String.valueOf(itemNewName.charAt(0)), catId, newFileNamePath, SOURCE_USER, NO_SOUND, null);
		MainPlay.totalItems++;
		MainPlay.currItems++;
		resetAll();
	}

	/**
	 * A method to handle saving of the taken image
	 * @param itemPicFile	The location to save the file to
	 */
	private void saveNewObjectImage(File itemPicFile) {
		try {
			FileOutputStream out = new FileOutputStream(itemPicFile);
			bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (IOException e) {
			// Unable to create file
			Toast t = Toast.makeText(getApplicationContext(), "File not saved, check external storage is mounted", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 50);
			t.show();
			Log.i("Camera", "initSaveToDB() Error writing file, " + e);
		}
	}

		/**
		 * A method to clear the picture screen to its default state
		 */
		private void resetAll() {
			resetNames();;
			imagePhotoToAdd.setImageResource(R.drawable.abcs_add_your_own_squ_icon);
			pictWaiting = false;
			butSaveToDB.setEnabled(false);
			prepareCategoriesForAutoFill();
		}

		/**
		 * A method to retrieve the entered name and category of the new image
		 * To convert everything to lower case and trim space characters
		 */
		private void prepareNames() {
			itemNewName = objEntName.getText().toString();
			itemNewCategory = objEntCat.getText().toString();
			itemNewName = itemNewName.toLowerCase(Locale.UK);
			itemNewName = itemNewName.trim();
			Log.i("ABCsCamera","prepareNames() itemNewname = " + itemNewName);
			itemNewCategory = itemNewCategory.toLowerCase(Locale.UK);
			itemNewCategory = itemNewCategory.trim();
			Log.i("ABCsCamera","prepareNames() itemNewCategory = " + itemNewCategory);
			newFileName = itemNewName + JPEG_FILE_SUFFIX;
			Log.i("ABCsCamera","prepareNames() Filename = " + newFileName);
		}
		
		/**
		 * Method to get picture file using implicit intent
		 */
		private void getFilePict() {
			Log.i("ABCsCamera","getFilePict() called");

			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			newPhoto = null;			
			try {
				newPhoto = setUpPhotoFile();
				tempFilePath = newPhoto.getAbsolutePath();
				intent.setType("image/*");
				intent.putExtra("crop", true);
				intent.putExtra("aspectX", ASPECT);
				intent.putExtra("aspectY", ASPECT);
				intent.putExtra("outputX", OUTPUT);
				intent.putExtra("outputY", OUTPUT);
				intent.putExtra("scale", true);
				intent.putExtra("noFaceDetection", true);
				intent.putExtra("return-data", false);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newPhoto));
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				newPhoto = null;
				tempFilePath = null;
			}
			startActivityForResult(Intent.createChooser(intent, "Select Picture"), PHOTO_PICKED);
		}


		/**
		 * Method to clear the text fields
		 */
		private void resetNames() {
			itemNewName = "";
			itemNewCategory = "";
			objEntName.setText("");
			objEntCat.setText("");
		}

		/**
		 * Method to initiate taking a camera picture using an implicit intent
		 */
		private void takeCameraPict() {
			Log.i("ABCsCamera","takeCameraPict() called");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			lockOrient(); // Lock the screen orientation to preserve data
			newPhoto = null;			
			try {
				newPhoto = setUpPhotoFile();
				tempFilePath = newPhoto.getAbsolutePath();
				intent.putExtra("crop", true);
				intent.putExtra("aspectX", ASPECT);
				intent.putExtra("aspectY", ASPECT);
				intent.putExtra("outputX", OUTPUT);
				intent.putExtra("outputY", OUTPUT);
				intent.putExtra("scale", true);
				intent.putExtra("noFaceDetection", false);
				intent.putExtra("return-data", false);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newPhoto));
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				newPhoto = null;
				tempFilePath = null;
			}
			startActivityForResult(intent, PHOTO_TAKEN);
		}

		/**
		 * A method to prepare a temp image file and an absolute path to it
		 * @return 	File f
		 * @throws IOException
		 */
		private File setUpPhotoFile() throws IOException {
			Log.i("ABCsCamera","setUpPhotoFile() called");
			File f = createTempImageFile();
			tempFilePath = f.getAbsolutePath();

			return f;
		}

		/**
		 * A method to prepare a temp file with a unique name
		 * @return
		 * @throws IOException
		 */
		private File createTempImageFile() throws IOException {
			Log.i("ABCsCamera","createTempImageFile() called");
			// Create an image file name
			String imageFileName = JPEG_FILE_PREFIX;
			fPictDir = getPictDir();
			Log.i("ABCsCamera","createTempImageFile(): " + imageFileName + JPEG_FILE_SUFFIX + " " + fPictDir);
			File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, fPictDir);
			return imageF;
		}

		@Override
		/**
		 * A method to receive result & request codes from the implicit intents
		 */
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			Log.i("ABCsCamera","onActivityResult() called");
			if (resultCode == RESULT_OK) {
				if (requestCode == PHOTO_TAKEN) {
					processPhoto();
				} else {
					processPhoto();
				}
			}
		}

		/**
		 * A method to initiate handling of the photo
		 */
		private void processPhoto() {
			Log.i("ABCsCamera","processPhoto() called");
			if (tempFilePath != null) {
				setPic();
				galleryAddPic();
				pictWaiting = true;
				butSaveToDB.setEnabled(true);
				tempFilePath = null;
			}
		}

		/**
		 * This method makes sure the image is rotated correctly and scaled and proportioned
		 * appropriately
		 */
		private void setPic() {
			Log.i("ABCsCamera","setPic() called");		
			// find rotation
			int rotate = 0;
			try {
				getContentResolver().notifyChange(Uri.fromFile(newPhoto), null);
				File imageFile = new File(tempFilePath);
				ExifInterface exif = new ExifInterface(
						imageFile.getAbsolutePath());
				int orientation = exif.getAttributeInt(
						ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);

				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;
				}
				Log.i("onActivityResult", "Exif orientation: " + orientation);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// pre-scale the target bitmap into which the file is decoded 
			// 1. Get the size of the ImageView
			int targW = imagePhotoToAdd.getMeasuredWidth();
			int targH = imagePhotoToAdd.getMeasuredHeight();
			Log.i("setPic", "targetWidth: " + targW);
			Log.i("setPic", "targetHeight: " + targH);

			// 2.Get the size of the image
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(tempFilePath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;
			Log.i("setPic", "photoW: " + photoW);
			Log.i("setPic", "photoH: " + photoH);

			// Figure out which way needs to be reduced less
			int scaleFactor = 1;
			if ((targW > ((photoW>photoH) ? photoW : photoH) || (targH > ((photoW>photoH) ? photoW : photoH)))) {
				scaleFactor = Math.min(photoW/targW, photoH/targH);
				Log.i("setPic", "scaleFactor: " + scaleFactor);
			}

			// Set bitmap options to scale the image decode target
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			//bmOptions.inPurgeable = true;

			// Decode the JPEG file into a Bitmap
			Bitmap bitmap = BitmapFactory.decodeFile(tempFilePath, bmOptions);
			//Log.i("setPic", "bitmapW: " + bitmap.getWidth());
			//Log.i("setPic", "bitmapH: " + bitmap.getHeight());
			
			// rotate the file if necessary
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);


			if (bitmap.getHeight()>bitmap.getWidth()) {
				bitmapFinal = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight()-bitmap.getWidth())/2, bitmap.getWidth(), bitmap.getWidth(), matrix, true);
			} else if (bitmap.getHeight()<bitmap.getWidth()) {
				bitmapFinal = Bitmap.createBitmap(bitmap, (bitmap.getWidth()-bitmap.getHeight())/2, 0, bitmap.getHeight(), bitmap.getHeight(), matrix, true);
			} else {
				bitmapFinal = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			}

			imagePhotoToAdd.setImageBitmap(bitmapFinal);
			
		}

		/**
		 * A method to add the original photo to the mediastore
		 */
		private void galleryAddPic() {
			Log.i("ABCsCamera","galleryAddPic() called");
			Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(tempFilePath);
			Uri contentUri = Uri.fromFile(f);
			mediaScanIntent.setData(contentUri);
			this.sendBroadcast(mediaScanIntent);
		}

		/**
		 * A method to save a file in the External Storage Picture Directory
		 * @return The file saved
		 */
		public File getTempStorageDir() {
			Log.i("ABCsCamera","getTempStorageDir() called");
			return new File (Environment.getExternalStorageDirectory()
					+ PICS_DIRECTORY);		
		}

		/**
		 * A method to test if the external storage can be written too
		 * @return	the external storage directory
		 */
		private File getPictDir() {
			Log.i("ABCsCamera","getAlbumDir() called");
			File storageDir = null;
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				storageDir = getTempStorageDir();
				if (storageDir != null) {
					if (! storageDir.mkdirs()) {
						if (! storageDir.exists()){
							Log.i("ABCsCamera", "failed to create a directory for photo storage");
							return null;
						}
					}
				}
			} else {
				Log.i(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
			}

			return storageDir;
		}

	}
