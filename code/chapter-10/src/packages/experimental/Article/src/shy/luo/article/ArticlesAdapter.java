package shy.luo.article;

import java.util.LinkedList;

import shy.luo.providers.articles.Articles;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;


public class ArticlesAdapter {
	private static final String LOG_TAG = "shy.luo.article.ArticlesAdapter";

	private ContentResolver resolver = null;
	
	public ArticlesAdapter(Context context) {
		resolver = context.getContentResolver();
	}
	
	public long insertArticle(Article article) {	
		ContentValues values = new ContentValues();
		values.put(Articles.TITLE, article.getTitle());
		values.put(Articles.ABSTRACT, article.getAbstract());
		values.put(Articles.URL, article.getUrl());
		
		Uri uri = resolver.insert(Articles.CONTENT_URI, values);
		String itemId = uri.getPathSegments().get(1);
		
		return Integer.valueOf(itemId).longValue();
	}
	
	public boolean updateArticle(Article article) {
		Uri uri = ContentUris.withAppendedId(Articles.CONTENT_URI, article.getId());
		
		ContentValues values = new ContentValues();
		values.put(Articles.TITLE, article.getTitle());
		values.put(Articles.ABSTRACT, article.getAbstract());
		values.put(Articles.URL, article.getUrl());
		
		int count = resolver.update(uri, values, null, null);
		
		return count > 0;
	}
	
	public boolean removeArticle(int id) {
        	Uri uri = ContentUris.withAppendedId(Articles.CONTENT_URI, id);
        
       	 	int count = resolver.delete(uri, null, null);
        
        	return count > 0;
	}
	
	public LinkedList<Article> getAllArticles() {
		LinkedList<Article> articles = new LinkedList<Article>();
		
        	String[] projection = new String[] { 
        		Articles.ID,       
                	Articles.TITLE,
                	Articles.ABSTRACT,
                	Articles.URL
        	};
    
        	Cursor cursor = resolver.query(Articles.CONTENT_URI, projection, null, null, Articles.DEFAULT_SORT_ORDER);
		if (cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String abs = cursor.getString(2);
				String url = cursor.getString(3);
				
				Article article = new Article(id, title, abs, url);
				articles.add(article);
			} while(cursor.moveToNext());
		}
		
		return articles;
	}
	
	public int getArticleCount() {
                Log.i(LOG_TAG, "ArticlesAdapter.getArticleCount");

		/*
        	Cursor cursor = resolver.query(Articles.CONTENT_COUNT_URI, null, null, null, Articles.DEFAULT_SORT_ORDER);
        
		Log.i(LOG_TAG, "cursor.moveToFirst");

        	if (!cursor.moveToFirst()) {
			return 0;
		}
		
		return cursor.getInt(0);
		*/
		
		int count = 0;

		try {
			IContentProvider provider = resolver.acquireProvider(Articles.CONTENT_URI);
			Bundle bundle = provider.call(Articles.METHOD_GET_ITEM_COUNT, null, null);
			count = bundle.getInt(Articles.KEY_ITEM_COUNT, 0);
		} catch(RemoteException e) {
			e.printStackTrace();		
		}

		return count;
	}
	
	public Article getArticleById(int id) {
		Uri uri = ContentUris.withAppendedId(Articles.CONTENT_URI, id);
		
		String[] projection = new String[] { 
				Articles.ID,       
	            Articles.TITLE,
	            Articles.ABSTRACT,
	            Articles.URL
	    	};
	    
		Cursor cursor = resolver.query(uri, projection, null, null, Articles.DEFAULT_SORT_ORDER);
		
		Log.i(LOG_TAG, "cursor.moveToFirst");

		if (!cursor.moveToFirst()) {
			return null;
		}
		
		String title = cursor.getString(1);
		String abs = cursor.getString(2);
		String url = cursor.getString(3);

		return new Article(id, title, abs, url);
	}
	
	public Article getArticleByPos(int pos) {
		Uri uri = ContentUris.withAppendedId(Articles.CONTENT_POS_URI, pos);

		String[] projection = new String[] { 
				Articles.ID,       
	            Articles.TITLE,
	            Articles.ABSTRACT,
	            Articles.URL
	    	};
	    
		Cursor cursor = resolver.query(uri, projection, null, null, Articles.DEFAULT_SORT_ORDER);
		if (!cursor.moveToFirst()) {
			return null;
		}
		
		int id = cursor.getInt(0);
		String title = cursor.getString(1);
		String abs = cursor.getString(2);
		String url = cursor.getString(3);

		return new Article(id, title, abs, url);
	}
}
