package cn.yo2.aquarium.pocketvoa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	private static final String CLASSTAG = Main.class.getSimpleName();

	private static final String HOST = "http://www.51voa.com";
	private static final String LIST_URL = HOST + "/VOA_Standard_English/";

	private static final int PROGRESS_DIALOG = 1;

	protected static final int WHAT_SUCCESS = 0;

	protected static final int WHAT_FAIL_IO = 1;

	private ArrayList<Article> mList;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_SUCCESS:
				bindList();
				dismissDialog(PROGRESS_DIALOG);
				break;
			case WHAT_FAIL_IO:
				Toast.makeText(Main.this, "FAIL IO", Toast.LENGTH_LONG);
				break;
			default:
				break;
			}
		}

	};
	
	private Button btnRefreshStandard;
	private TextView tvStandard;
	private ListView lvStandard;
	
	private Button btnRefreshSpecial;
	private TextView tvSpecial;
	private ListView lvSpecial;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
		tabHost.setup();
		
		TabHost.TabSpec tabSpec = tabHost.newTabSpec("Standard");
		tabSpec.setContent(R.id.tab_standard);
		tabSpec.setIndicator("Standard");
		tabHost.addTab(tabSpec);
		
		tabSpec = tabHost.newTabSpec("Special");
		tabSpec.setContent(R.id.tab_special);
		tabSpec.setIndicator("Special");
		tabHost.addTab(tabSpec);
		
		tabHost.setCurrentTab(0);

		btnRefreshStandard = (Button)findViewById(R.id.btn_refresh_standard);
		tvStandard = (TextView)findViewById(R.id.empty_standard);
		lvStandard = (ListView)findViewById(R.id.list_standard);
		lvStandard.setEmptyView(tvStandard);
		
		lvStandard.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Article article = mList.get(position);
				Intent intent = new Intent(Main.this, Show.class);
				intent.putExtra(DatabaseHelper.C_TITLE, article.title);
				intent.putExtra(DatabaseHelper.C_URL, article.url);
				startActivity(intent);
			}
			
		});
		
		btnRefreshStandard.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				refreshList();
			}
		});
		
		
		btnRefreshSpecial = (Button)findViewById(R.id.btn_refresh_special);
		tvSpecial = (TextView)findViewById(R.id.empty_special);
		lvSpecial = (ListView)findViewById(R.id.list_special);
		lvSpecial.setEmptyView(tvSpecial);
		
		mList = (ArrayList<Article>)getLastNonConfigurationInstance();
		
		if (mList == null)
			refreshList();
		else 
			bindList();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mList;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Loading");
			progressDialog.setMessage("Loading");
			return progressDialog;
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	private void bindList() {
		RowAdapter adapter = new RowAdapter(this, mList);
		lvStandard.setAdapter(adapter);
	}

	private void refreshList() {
		showDialog(PROGRESS_DIALOG);
		new Thread() {

			@Override
			public void run() {
				try {
					downloadItems();
					mHandler.sendEmptyMessage(WHAT_SUCCESS);
				} catch (IOException e) {
					mHandler.sendEmptyMessage(WHAT_FAIL_IO);
				}
			}

		}.start();
	}

	private void downloadItems() throws IOException {
		int timeoutConnection = 3000;
		int timeoutSocket = 1000 * 300;

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
		HttpConnectionParams.setSoTimeout(params, timeoutSocket);

		DefaultHttpClient client = new DefaultHttpClient(params);
		// ///////////////////

		HttpGet get = new HttpGet(LIST_URL);
		ResponseHandler<String> handler = new BasicResponseHandler();

		try {
			mList = parse(client.execute(get, handler));
		} catch (ClientProtocolException e) {
			Log.e(CLASSTAG, "Error when execute http get.", e);
			throw e;
		} catch (IOException e) {
			Log.e(CLASSTAG, "Error when execute http get.", e);
			throw e;
		} finally {
			get.abort();
			client.getConnectionManager().shutdown();
		}
	}

	private ArrayList<Article> parse(String body) {
		ArrayList<Article> list = new ArrayList<Article>();
		// int ulStart = body.indexOf("id=\"list\"");
		Pattern pattern = Pattern
				.compile("<a href=\"(/VOA_Standard_English/VOA_Standard_English_\\d+.html)\" target=\"_blank\">([^<]+)</a>");
		Matcher matcher = pattern.matcher(body);
		while (matcher.find()) {
			String url = matcher.group(1);
			String title = matcher.group(2);
			Log.d(CLASSTAG, "url -- " + url + " title -- " + title);
			Article article = new Article();
			article.url = url;
			article.title = title;
			list.add(article);
		}
		return list;
	}
}

class RowAdapter extends BaseAdapter {

	private ArrayList<Article> mList;
	private Context mContext;
	private LayoutInflater mInflater;
	
	public RowAdapter(Context context, ArrayList<Article> list) {
		super();
		mContext = context;
		mList = list;
		mInflater = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewWraper wraper = null;
		if (row == null) {
			row = mInflater.inflate(R.layout.list_item, null);
			wraper = new ViewWraper(row);
			row.setTag(wraper);
		} else {
			wraper = (ViewWraper)row.getTag();
		}
		
		wraper.getTitle().setText(mList.get(position).title);
		
		return row;
	}

}

class ViewWraper {
	View root;
	TextView tvTitle;

	public ViewWraper(View root) {
		this.root = root;
	}

	TextView getTitle() {
		if (tvTitle == null) {
			tvTitle = (TextView) root.findViewById(R.id.tv_title);
		}
		return tvTitle;
	}
}



