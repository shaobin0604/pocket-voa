package cn.yo2.aquarium.pocketvoa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper {
	private static final String CLASSTAG = DatabaseHelper.class.getSimpleName();

	private static final String EXEC_SQL_PREFIX = "SQL EXEC -- ";

	private static final String DB_NAME = "pocketvoa.db";

	private static final int DB_VER = 3;

	static final String T_ARTICLES = "articles";

	static final String C_ID = Article.K_ID;
	static final String C_TYPE = Article.K_TYPE;
	static final String C_SUBTYPE = Article.K_SUBTYPE;
	static final String C_TITLE = Article.K_TITLE;
	static final String C_DATE = Article.K_DATE;
	static final String C_URL = Article.K_URL;
	static final String C_MP3 = Article.K_MP3;

	static final String[] COLS = { C_ID, C_TYPE, C_SUBTYPE, C_TITLE, C_DATE,
			C_URL, C_MP3, };

	private static final String CREATE_TABLE_SQL = "CREATE TABLE " + T_ARTICLES
			+ "(" + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + C_TYPE
			+ " TEXT," + C_SUBTYPE + " TEXT," + C_TITLE + " TEXT," + C_DATE
			+ " INTEGER," + C_URL + " TEXT," + C_MP3 + " TEXT" + ");";

	private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS "
			+ T_ARTICLES + ";";

	private DatabaseOpenHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;

	public DatabaseHelper(Context context) {
		mCtx = context;
		mDbHelper = new DatabaseOpenHelper(mCtx);
	}

	public void open() {
		if (mDb == null)
			mDb = mDbHelper.getWritableDatabase();
	}

	public void close() {
		if (mDb != null) {
			mDb.close();
			mDb = null;
		}
	}

	/**
	 * Test if the article exist in database
	 * 
	 * @param article
	 * @return true if exist
	 */
	public boolean isArticleExist(Article article) {
		Cursor cursor = mDb.query(T_ARTICLES, new String[] { C_ID }, C_URL
				+ "=?", new String[] { article.url }, null, null, null);
		return (cursor != null && cursor.moveToFirst());
	}

	/**
	 * Query articles with the type and subtype, if the argument is null, do not
	 * filter on the column
	 * 
	 * @param type
	 * @param subtype
	 * @return SQLite Cursor on the result set
	 */
	public Cursor queryArticles(String type, String subtype) {
		String sel = null;
		String[] selArgs = null;
		if (type != null && subtype != null) {
			sel = C_TYPE + "=? and " + C_SUBTYPE + "=?";
			selArgs = new String[] { type, subtype };
		} else if (type != null) {
			sel = C_TYPE + "=?";
			selArgs = new String[] { type };
		} else if (subtype != null) {
			sel = C_SUBTYPE + "=?";
			selArgs = new String[] { subtype };
		}

		return mDb.query(T_ARTICLES, COLS, sel, selArgs, null, null, C_DATE
				+ " desc");
	}

	/**
	 * 
	 * @param id
	 * @return the Article with the id or null if cannot be found
	 */
	public Article queryArticle(long id) {
		Article article = null;
		Cursor cursor = mDb.query(T_ARTICLES, COLS, C_ID + "=" + id, null,
				null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				article = new Article();
				article.id = id;
				article.type = cursor.getString(cursor.getColumnIndex(C_TYPE));
				article.subtype = cursor.getString(cursor
						.getColumnIndex(C_SUBTYPE));
				article.title = cursor
						.getString(cursor.getColumnIndex(C_TITLE));
				article.date = cursor.getString(cursor.getColumnIndex(C_DATE));
				article.url = cursor.getString(cursor.getColumnIndex(C_URL));
				article.mp3 = cursor.getString(cursor.getColumnIndex(C_MP3));
			}
			cursor.close();
		}

		return article;
	}

	public long createArticle(String title, String date, String type,
			String subtype, String url, String mp3) {
		ContentValues values = new ContentValues();

		values.put(C_TITLE, title);
		values.put(C_DATE, date);
		values.put(C_TYPE, type);
		values.put(C_SUBTYPE, subtype);
		values.put(C_URL, url);
		values.put(C_MP3, mp3);

		return mDb.insert(T_ARTICLES, null, values);
	}

	public long createArticle(Article article) {
		ContentValues values = new ContentValues();

		values.put(C_TITLE, article.title);
		values.put(C_DATE, article.date);
		values.put(C_TYPE, article.type);
		values.put(C_SUBTYPE, article.subtype);
		values.put(C_URL, article.url);
		values.put(C_MP3, article.mp3);

		return mDb.insert(T_ARTICLES, null, values);
	}

	public int deleteArticle(long id) {
		return mDb.delete(T_ARTICLES, C_ID + "=?", new String[] { String
				.valueOf(id) });
	}

	private static class DatabaseOpenHelper extends SQLiteOpenHelper {

		DatabaseOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VER);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				 Log.d(CLASSTAG, EXEC_SQL_PREFIX + CREATE_TABLE_SQL);
				db.execSQL(CREATE_TABLE_SQL);
			} catch (SQLException e) {
				 Log.e(CLASSTAG, "Error when create tables", e);
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String msg = "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data";
			Log.w(CLASSTAG, msg);
			try {
				Log.d(CLASSTAG, EXEC_SQL_PREFIX + DROP_TABLE_SQL);
				db.execSQL(DROP_TABLE_SQL);
			} catch (SQLException e) {
				Log.e(CLASSTAG, "Error when drop tables", e);
			}

			onCreate(db);
		}
	}
}
