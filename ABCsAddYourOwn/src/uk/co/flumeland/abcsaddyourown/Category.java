package uk.co.flumeland.abcsaddyourown;
import java.util.ArrayList;

import android.util.Log;


public class Category {

	private String category;
	private int catId;
	private static int noCats = 4;
	
	
	
	//Constructor
	//public Category(String category) {
	//	this.category = category;	
	//}
	
	//Constructor setting catId
	public Category(String category, int catId) {
		Log.i("Category", "new cat constructor, category = " + category + ", catId = " + catId);
		this.category = category;
		this.catId = catId;	
	}

	/**
	 * A method to return the id number of the category
	 * @return	the cat Id number
	 */
	public int getCatId() {
		return catId;
	}

	/**
	 * A method to set the category Id number
	 * @param catId	The number to set to
	 */
	public void setCatId(int catId) {
		this.catId = catId;
	}

	/**
	 * A method to get the category name
	 * @return	the name of the category
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * A method to set the category name
	 * @param category The name of the category
	 */
	public void setCategory(String category) {
		category = category;
	}
	
}
