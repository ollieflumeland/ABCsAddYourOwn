package uk.co.flumeland.abcsaddyourown;

import android.util.Log;

public class Item {

	private static final int USER_ADDED = 1;
	private static final int HAS_SOUND_EFFECT = 1;
	private static final int NO_SOUND_EFFECT = 0;
	
	private int idNo;				// ID number of Object
	private String name; 			// Objects Name
	private char letter;			// Objects First Letter
	private String category;		// Objects Category
	private int catId;				// Category ID
	private String photoFile;		// File Path of Photo
	private int source;				// Default 0 / User Added = 1
	private String soundEffectFile;	// File Path of Sound Effect
	public int hasSndEffect = NO_SOUND_EFFECT; // Is a sound effect associated
	
	private static int total = 26;  // Tracks total number of items in database

	/**
	 * Constructor:
	 * @param idNo	= id number of the object
	 * @param name	= object name
	 * @param category	= object's category
	 * @param catId	= category id no
	 * @param photoFile = filename of the photo 
	 */
	public Item(int idNo, String name, String category, int catId, String photoFile) {
		this.idNo = idNo;
		this.name = name;
		letter = name.charAt(0);
		this.category = category;
		this.catId = catId; 
		this.photoFile = photoFile;
		idNo = total;		
	}
	
	/**
	 * Constructor with sound effects
	 * @param idNo	= id number of the object
	 * @param name	= object name
	 * @param letter	= initial letter
	 * @param category	= object's category
	 * @param catId	= category id no
	 * @param photoFile = filename and path of the photo 
	 * @param source	= source of the item
	 * @param hasSndEffect	= indicator of sound effect
	 * @param soundEffectFile = filename and path of the sound
	 */
	public Item(int idNo, String name, char letter, String category, int catId, String photoFile, int source, int hasSndEffect, String soundEffectFile) {
		this.idNo = idNo;
		this.name = name;
		this.letter = name.charAt(0);
		this.category = category;
		this.catId = catId; 
		this.photoFile = photoFile;
		this.source = source;
		this.hasSndEffect = hasSndEffect;
		this.soundEffectFile = soundEffectFile;
	}
	
	/**
	 * Constructor
	 * @param item The item that the new item should copy
	 */
	public Item (Item item) {
		this.idNo = item.idNo;
		this.name = item.getName();
		this.letter = item.getLetter();
		this.category = item.getCategory();
		this.catId = item.getCatId();
		this.photoFile = item.getPhotoFile();
		this.source = item.getSource();
		this.hasSndEffect = item.hasSoundEffect();
		this.soundEffectFile = item.getSoundEffectFile();
	}


	/**
	 * A method to return the name of the item
	 * @return = items name
	 */
	public String getName() {
		return name;
	}

	/**
	 * A method to return the letter the item begins with
	 * @return	= The initial letter
	 */
	public char getLetter() {
		return letter;
	}

	/**
	 * A method to return the category name
	 * @return	= the name of the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * A method to return the cat id
	 * @return	= the cat id number
	 */
	public int getCatId() {
		return catId;
	}

	/**
	 * A method to set the initial letter of the object
	 * @param letter	the letter to be set
	 */
	public void setLetter(char letter) {
		this.letter = letter;
	}

	/**
	 * A method to set the category of the object
	 * @param category	= the name of the category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * A method to return the has sound indicator
	 * @return	the indicator
	 */
	public int hasSoundEffect() {
		Log.i("Item", "hasSoundEffect() called, int hasSndEffect = " + hasSndEffect);
		return hasSndEffect;
	}

	/**
	 * A method to set the sound effect indicator
	 * @param soundEffect	the indicator value
	 */
	public void setHasSoundEffect(int soundEffect) {
		Log.i("Item", "setHasSoundEffect() called, int soundEffect = " + soundEffect);
		hasSndEffect = soundEffect;
	}

	/**
	 * A method to return the path of the photo file
	 * @return	= the path of the photo file
	 */
	public String getPhotoFile() {
		return photoFile;
	}

	/**
	 * A method to return the source indicator
	 * @return	the source indicator
	 */
	public int getSource() {
		return source;
	}

	/**
	 * A method to set the name of the item
	 * @param name	= the name of the item
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * A method to set the photo file path
	 * @param photoFile	= the path of the photo
	 */
	public void setPhotoFile(String photoFile) {
		this.photoFile = photoFile;
	}

	/**
	 * A method to set the idNo of the item
	 * @param idNo
	 */
	public void setIdNo(int idNo) {
		this.idNo = idNo;
	}

	/**
	 * A method to set the item source
	 * @param source	the source indicator
	 */
	public void setSource(int source) {
		this.source = source;
	}

	/**
	 * A method to set the category id number
	 * @param catId	= the id no
	 */
	public void setCatId(int catId) {
		this.catId = catId;
	}

	/**
	 * A method to get the item id number
	 * @return = the id number
	 */
	public int getIdNo() {
		return idNo;
	}

	/**
	 * A method to get the path to the sound file
	 * @return	= the sound file path
	 */
	public String getSoundEffectFile() {
		return soundEffectFile;
	}

	/**
	 * A method to set the sound filepath
	 * @param soundEffect	the path to the sound file
	 */
	public void setSoundEffectFile(String soundEffect) {
		soundEffectFile = soundEffect;
		hasSndEffect = 1;
	}
}

