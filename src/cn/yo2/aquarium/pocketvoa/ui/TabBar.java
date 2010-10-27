package cn.yo2.aquarium.pocketvoa.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.yo2.aquarium.pocketvoa.R;


/**
 * TabBar view hierarchy
 * 
 * +---------------------------------------------------------------+
 * + LinearLayout +                                                +
 * +              + LinearLayout(ImageView, TextView)              +
 * +              + LinearLayout(ImageView, TextView)              +
 * +              + ...                                            +
 * +---------------------------------------------------------------+
 * + LinearLayout                                                  +
 * +---------------------------------------------------------------+
 * 
 * 
 * @author shaobin
 *
 */
public class TabBar extends LinearLayout implements OnClickListener {
	private static final int TAB_SPACING = 0;
	private static final int TAB_HORIZONTAL_PADDING = 0;
	private static final int TAB_BAR_RES = 0;
	private static final int TAB_BUTTON_RES = R.drawable.tab_indicator;
	private static final int TEXT_SIZE = 14;
	private static final int TEXT_COLOR = Color.WHITE;
	private static final int TAB_SEPERATOR_RES = R.color.tab_bar_seperator;
	private static final int TAB_SEPERATOR_HEIGHT = 0;
	private static final int TAB_HEIGHT = 0;
	private static final int TAB_WIDTH = LinearLayout.LayoutParams.FILL_PARENT;

	
	private LinearLayout mTabHolder;
	private View mTabSeperator;
	
	
	private List<Tab> mTabList = new ArrayList<Tab>();
	private int mCurrentTabIndex = -1;
	
	private Context mContext;
	
	private int mTabWidth = TAB_WIDTH;
	private int mTabHeight = TAB_HEIGHT;
	private int mTabSpacing = TAB_SPACING;
	private int mTabHorizontalPadding = TAB_HORIZONTAL_PADDING;
	private int mTabSeperatorHeight = TAB_SEPERATOR_HEIGHT;
	
	private int mTabButtonDrawable = TAB_BUTTON_RES;
	private int mTabBarDrawable = TAB_BAR_RES;
	private int mTabSeperatorDrawable = TAB_SEPERATOR_RES;
	
	private int mTextColor = TEXT_COLOR;
	private int mTextSize = TEXT_SIZE;
	
	private boolean mPacked;
	
	private OnTabChangeListener mOnTabChangeListener;
	

	public TabBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		setupLayoutParams(context, attrs);
	}

	private void setupLayoutParams(Context context, AttributeSet attrs) {
		TypedArray params = context.obtainStyledAttributes(attrs,
				R.styleable.TabBar);
		
		mTextColor = params.getColor(R.styleable.TabBar_tabTextColor, TEXT_COLOR);
		mTextSize = (int) params.getDimension(R.styleable.TabBar_tabTextSize, TEXT_SIZE);
		mTabWidth = (int) params.getDimension(R.styleable.TabBar_tabWidth, TAB_WIDTH);
		mTabHeight = (int) params.getDimension(R.styleable.TabBar_tabHeight, TAB_HEIGHT);
		mTabSpacing = (int) params.getDimension(R.styleable.TabBar_tabSpacing, TAB_SPACING);
		mTabButtonDrawable = params.getResourceId(R.styleable.TabBar_tabButtonDrawable, TAB_BUTTON_RES);
		mTabBarDrawable = params.getResourceId(R.styleable.TabBar_tabBarDrawable, TAB_BAR_RES);
		mTabSeperatorDrawable = params.getResourceId(R.styleable.TabBar_tabSeperatorDrawable, TAB_SEPERATOR_RES);
		mTabSeperatorHeight = (int) params.getDimension(R.styleable.TabBar_tabSeperatorHeight, TAB_SEPERATOR_HEIGHT);
		mTabHorizontalPadding = (int) params.getDimension(R.styleable.TabBar_tabHorizontalPadding, TAB_HORIZONTAL_PADDING);
		
		
		params.recycle();
	}

	public TabBar(Context context) {
		super(context);
		mContext = context;
	}
	

	public void addTab(TabSpec tabSpec) {
		if (mPacked)
			throw new IllegalStateException();
		
		Tab holder = new Tab(mContext, tabSpec);
		
		mTabList.add(holder);
	}
	
	private static class Tab extends LinearLayout {
		private ImageView image;
		private TextView text;

		public Tab(Context context, TabSpec spec) {
			super(context);
			
			image = new ImageView(context);
			text = new TextView(context);
			text.setText(spec.text);
			
			setGravity(Gravity.CENTER);
			
			addView(image, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			addView(text, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			
			if (spec.icon != null) {
				image.setVisibility(View.VISIBLE);
				image.setImageDrawable(spec.icon);
			} else {
				image.setVisibility(View.GONE);
			}
		}
		
		public void setTextSize(float size) {
			text.setTextSize(size);
		}
		
		public void setTextColor(int color) {
			text.setTextColor(color);
		}
		
	}
	
	public void setOnTabChangeListener(OnTabChangeListener l) {
		if (!mPacked)
			throw new IllegalStateException();
		
		mOnTabChangeListener = l;
	}
	
	public int getCurrentTab() {
		if (!mPacked)
			throw new IllegalStateException();
		
		return mCurrentTabIndex;
	}
	
	public void setCurrentTab(int index) {
		selectTab(mTabList.get(index));
	}

	public void onClick(View v) {
		selectTab(v);
	}

	private void selectTab(View v) {
		if (!mPacked)
			throw new IllegalStateException();
		
		final int size = mTabList.size();
		
		for (int i = 0; i < size; i++) {
			View btn = mTabList.get(i);
			if (v == btn) {
				btn.setSelected(true);

				mCurrentTabIndex = i;
				
				if (mOnTabChangeListener != null)
					mOnTabChangeListener.onTabChanged(i);
			} else {
				btn.setSelected(false);
			}
		}
	}
	
	public void pack() {
		if (mPacked)
			throw new IllegalStateException();
		
		setOrientation(LinearLayout.VERTICAL);
		
		mTabHolder = new LinearLayout(mContext);
		mTabHolder.setPadding(mTabHorizontalPadding, 0, mTabHorizontalPadding, 0);
		addView(mTabHolder, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		mTabSeperator = new View(mContext);
		mTabSeperator.setBackgroundResource(mTabSeperatorDrawable);
		addView(mTabSeperator, LinearLayout.LayoutParams.FILL_PARENT, 2);
		
		
		final int size = mTabList.size();
		for (int i = 0; i < size; i++) {
			Tab tab = mTabList.get(i);
			tab.setTextSize(mTextSize);
			tab.setTextColor(mTextColor);
			tab.setBackgroundResource(mTabButtonDrawable);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mTabWidth, mTabHeight, 1.0F);
			lp.setMargins(mTabSpacing, 0, mTabSpacing, 0);
			mTabHolder.addView(tab, lp);
			tab.setOnClickListener(this);
		}
		
		mPacked = true;
	}
	
	public static class TabSpec {
		private final Drawable icon;
		private final CharSequence text;
		
		public TabSpec(CharSequence text, Drawable icon) {
			this.text = text;
			this.icon = icon;
		}
	}

	public static interface OnTabChangeListener {
		public void onTabChanged(int index);
	}
}
