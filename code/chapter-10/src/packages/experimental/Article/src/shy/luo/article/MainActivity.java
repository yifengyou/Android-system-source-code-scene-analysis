package shy.luo.article;

import shy.luo.providers.articles.Articles;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
	private final static String LOG_TAG = "shy.luo.article.MainActivity";
	
	private final static int ADD_ARTICAL_ACTIVITY = 1;
	private final static int EDIT_ARTICAL_ACTIVITY = 2;
	
	private ArticlesAdapter aa = null;
 	private ArticleAdapter adapter = null;
	private ArticleObserver observer = null;
	
	private ListView articleList = null;
	private Button addButton = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		 
		aa = new ArticlesAdapter(this);
		
		articleList = (ListView)findViewById(R.id.listview_article);
		adapter = new ArticleAdapter(this);
		articleList.setAdapter(adapter);
		articleList.setOnItemClickListener(this);

		observer = new ArticleObserver(new Handler());
		getContentResolver().registerContentObserver(Articles.CONTENT_URI, true, observer);
		
		addButton = (Button)findViewById(R.id.button_add);
		addButton.setOnClickListener(this);

	    	Log.i(LOG_TAG, "MainActivity Created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		getContentResolver().unregisterContentObserver(observer);
	} 
	
	@Override
	public void onClick(View v) {
		if(v.equals(addButton)) {
			Uri uri = Articles.CONTENT_URI;
			int count = uri.getPathSegments().size();
			Log.i(LOG_TAG, "count: " + count);
			for(int i = 0; i < count; ++i) {
				String segment = uri.getPathSegments().get(i);
				Log.i(LOG_TAG, "segment " + i + ": " + segment);	
			}
			
			Intent intent = new Intent(this, ArticleActivity.class);
	    		startActivityForResult(intent, ADD_ARTICAL_ACTIVITY);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		Intent intent = new Intent(this, ArticleActivity.class);
		
		Article article = aa.getArticleByPos(pos);
		intent.putExtra(Articles.ID, article.getId());
		intent.putExtra(Articles.TITLE, article.getTitle());
		intent.putExtra(Articles.ABSTRACT, article.getAbstract());
		intent.putExtra(Articles.URL, article.getUrl());
		
    		startActivityForResult(intent, EDIT_ARTICAL_ACTIVITY);
	}
	
	
	@Override
	public void onActivityResult(int requestCode,int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    
		switch(requestCode) {
		case ADD_ARTICAL_ACTIVITY: {
			if(resultCode == Activity.RESULT_OK) {
				String title = data.getStringExtra(Articles.TITLE);
				String abs = data.getStringExtra(Articles.ABSTRACT);
				String url = data.getStringExtra(Articles.URL);
				
				Article article = new Article(-1, title, abs, url);
				aa.insertArticle(article);
				
				//adapter.notifyDataSetChanged();
			}
			
			break;
		}
		case EDIT_ARTICAL_ACTIVITY: {
			if(resultCode == Activity.RESULT_OK) {
				int action = data.getIntExtra(ArticleActivity.EDIT_ARTICLE_ACTION, -1);
				if(action == ArticleActivity.MODIFY_ARTICLE) {
					int id = data.getIntExtra(Articles.ID, -1);
					String title = data.getStringExtra(Articles.TITLE);
					String abs = data.getStringExtra(Articles.ABSTRACT);
					String url = data.getStringExtra(Articles.URL);
					
					Article article = new Article(id, title, abs, url);
					aa.updateArticle(article);
				} else if(action == ArticleActivity.DELETE_ARTICLE)	{
					int id = data.getIntExtra(Articles.ID, -1);
					
					aa.removeArticle(id);
				}
				
				//adapter.notifyDataSetChanged();
			}
			
			break;
		}
		}
	}

	private class ArticleObserver extends ContentObserver {
		public ArticleObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange (boolean selfChange) {
			Log.i("MainActivity.ArticleObserver", "MainActivity.ArticleObserver.onChange(" + selfChange  + ")");

			adapter.notifyDataSetChanged();
		}
	}
	     
	private class ArticleAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public ArticleAdapter(Context context){
			  inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return aa.getArticleCount();
		}
		
		@Override
		public Object getItem(int pos) {
			return aa.getArticleByPos(pos);
		}

		@Override
		public long getItemId(int pos) {
			return aa.getArticleByPos(pos).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
		
			Article article = (Article)getItem(position);
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item, null);
			}
			
			TextView titleView = (TextView)convertView.findViewById(R.id.textview_article_title);
			titleView.setText("Title: " + article.getTitle());
			
			TextView abstractView = (TextView)convertView.findViewById(R.id.textview_article_abstract);
			abstractView.setText("Abstract: " + article.getAbstract());
			
			TextView urlView = (TextView)convertView.findViewById(R.id.textview_article_url);
			urlView.setText("URL: " + article.getUrl());
			
			return convertView;
		}
	}
}
