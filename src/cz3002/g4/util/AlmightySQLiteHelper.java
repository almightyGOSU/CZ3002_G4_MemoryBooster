package cz3002.g4.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlmightySQLiteHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "memorybooster.db";
	private static final int DATABASE_VERSION = 2;
	
	public static final String FB_TABLE_NAME = "facebook_friends";
	public static final String FB_COLUMN_ID = "_id";
	public static final String FB_COLUMN_PROF_ID = "fb_id";
	public static final String FB_COLUMN_PROF_NAME = "fb_name";
	public static final String FB_COLUMN_PROF_PIC = "fb_pic";

	// Facebook table creation SQL statement
	private static final String FB_TABLE_CREATE = 
			"CREATE TABLE " + FB_TABLE_NAME + "(" 
			+ FB_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FB_COLUMN_PROF_ID + " TEXT NOT NULL, "
			+ FB_COLUMN_PROF_NAME + " TEXT NOT NULL, "
			+ FB_COLUMN_PROF_PIC + " BLOB NOT NULL);";

	public AlmightySQLiteHelper(Context context) {
		
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		
		database.execSQL(FB_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(AlmightySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		
		db.execSQL("DROP TABLE IF EXISTS " + FB_TABLE_NAME);
		
		onCreate(db);
	}
}
