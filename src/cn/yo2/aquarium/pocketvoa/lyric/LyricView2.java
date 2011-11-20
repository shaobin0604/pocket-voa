package cn.yo2.aquarium.pocketvoa.lyric;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;
import cn.yo2.aquarium.logutils.MyLog;
import cn.yo2.aquarium.pocketvoa.R;

public class LyricView2 extends FrameLayout {
	private static final String DEFAULT_ERROR_MESSAGE = "Cannot load lyric.";
	private static final int DEFAULT_FOCUSED_LINE_COLOR = Color.WHITE;
	private static final int DEFAULT_OTHER_LINE_COLOR = Color.GRAY;
	private static final float DEFAULT_TEXT_SIZE = 100;
	private static final int WHAT_LYRIC_LOAD_OK = 1;
	
	private int mFocusLineColor;
	private int mOtherLineColor;
	private float mTextSize;
	
	private Scroller mScroller;
	
	private TextPannel mTextPannel;
	private TextPaint mTextPaintDefault;
	private TextPaint mTextPaintHighlight;
	
	private int mCurrentLineIndex;
	private int mLineHeight;
	private int mPanelHeight;
	
	
	// The Lyric object
	private Lyric mLyric;	
	
	private List<LineBreak> mLineBreaks;
	
	private String mErrorMessage;
	private boolean mLyricLoaded;
	
	private boolean mOnLayoutCalled;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_LYRIC_LOAD_OK:
				requestLayout();
				break;

			default:
				break;
			}
			
		}
		
	};
	
	public boolean loadLyric(InputStream is, int width) {
		MyLog.d("in loadLyric");
		mLyricLoaded = mLyric.parseLyric(is);
		if (mLyricLoaded) {
			resetLyric();
			
			breakLyrics(width);
			
			mHandler.sendEmptyMessage(WHAT_LYRIC_LOAD_OK);
		}
		return mLyricLoaded;
	}
	
	private void breakLyrics(int width) {
		final int size = mLyric.getSize();
		
		MyLog.d("lyric size = " + size + ", screen width = " + width);
		
		mLineBreaks = new ArrayList<LineBreak>(size);
		mPanelHeight = 0;
		
		
		Sentence sentence;
		String text;
		LineBreak lineBreak;
		for (int i = 0; i < size; i++) {
			sentence = mLyric.getSentence(i);
			text = sentence.mContent;
			
			lineBreak = LineBreak.breakLine(text, mTextPaintDefault, width, mLineHeight);
			
			mLineBreaks.add(lineBreak);
			
			mPanelHeight += lineBreak.getHeight();
		}
	}
	
	public boolean isLyricLoaded() {
		return mLyricLoaded;
	}
	
	public void resetLyric() {
		mCurrentLineIndex = 0;
	}
	
	public void setLyricLoaded() {
		mLyricLoaded = true;
	}

	public void clearLyricLoaded() {
		mLyricLoaded = false;
	}
	
	public LyricView2(Context context) {
		super(context);
		
		init(context);
	}
	
	public LyricView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray params = context.obtainStyledAttributes(attrs,
				R.styleable.LyricView2);

		mErrorMessage = params
				.getString(R.styleable.LyricView2_errorMessage);

		if (mErrorMessage == null)
			mErrorMessage = DEFAULT_ERROR_MESSAGE;

		mFocusLineColor = params
				.getColor(
						R.styleable.LyricView2_focusLineColor,
						DEFAULT_FOCUSED_LINE_COLOR);

		mOtherLineColor = params
				.getColor(
						R.styleable.LyricView2_otherLineColor,
						DEFAULT_OTHER_LINE_COLOR);

		mTextSize = params.getDimension(
				R.styleable.LyricView2_textSize,
				DEFAULT_TEXT_SIZE);
		
		init(context);
	}

	private void init(Context context) {
		mLyric = new Lyric();
		
		mScroller = new Scroller(context);
		mTextPannel = new TextPannel(context);
		addView(mTextPannel, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		mTextPaintDefault = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		mTextPaintDefault.setColor(mOtherLineColor);
		mTextPaintDefault.setTextSize(mTextSize);
		mTextPaintDefault.setTextAlign(Align.CENTER);
		
		mTextPaintHighlight = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
		mTextPaintHighlight.setColor(mFocusLineColor);
		mTextPaintHighlight.setTextSize(mTextSize);
		mTextPaintHighlight.setTextAlign(Align.CENTER);
		
		
		FontMetricsInt fmi = mTextPaintDefault.getFontMetricsInt();
		mLineHeight = (fmi.bottom - fmi.top) * 3 / 2;
	}
	
	@Override
	public void computeScroll() {
//		MyLog.d("computeScroll");
		if (mScroller.computeScrollOffset()) {
			int currY = mScroller.getCurrY();
//			MyLog.d("currY = " + currY);
			
            scrollTo(0, currY); 
            postInvalidate(); 
        } 
	}
	
	public void update(long time) {
//		MyLog.d("mLyricLoaded = " + mLyricLoaded + ", time = " + time);
		if (mLyricLoaded) {
			int lineIndex = mLyric.getSentenceIndexInTimeFast(time);
			// int lineIndex = mLyric.getSentenceIndexInTime(time);
			if (lineIndex == -1) {
				mLyricLoaded = false;
			} else {
				int offsetY = calculateOffsetY(mCurrentLineIndex, lineIndex);

				if (offsetY != 0 && mOnLayoutCalled) {

					mCurrentLineIndex = lineIndex;

//					MyLog.d("offsetY = " + offsetY + ", start refresh");
					int scrollY = getScrollY();
					mScroller.startScroll(0, scrollY, 0, offsetY);

					invalidate();
				} else {
//					MyLog.d("no need to refresh");
				}
			}
		}
	}
	
	private int calculateOffsetY(int oldLineIndex, int newLineIndex) {
		int offsetY = 0;
		
		LineBreak lineBreak;
		if (newLineIndex > oldLineIndex) {
			for (int i = oldLineIndex; i < newLineIndex; i++) {
				lineBreak = mLineBreaks.get(i);
				offsetY += lineBreak.getHeight();
			}
		} else if (newLineIndex < oldLineIndex) {
			for (int i = newLineIndex; i < oldLineIndex; i++) {
				lineBreak = mLineBreaks.get(i);
				offsetY -= lineBreak.getHeight();
			}
		} 
		
		return offsetY;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		int scrollY = ((top - bottom) / 2 + mLineHeight);
		
		MyLog.d("changed = " + changed + ", scrollY = " + scrollY);
		
		if (!mOnLayoutCalled) {
			scrollTo(0, scrollY);
			mOnLayoutCalled = true;
		}
	}
	

	private class TextPannel extends View {

		public TextPannel(Context context) {
			super(context);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			MyLog.d("onDraw");
			
			final int offsetX = getWidth() / 2;
			
			if (mLyricLoaded) {
				MyLog.d("lyric loaded");
				LineBreak lineBreak;
				final int size = mLineBreaks.size();
				for (int i = 0, offsetY = 0; i < size; i++) {
					lineBreak = mLineBreaks.get(i);
					
					if (i == mCurrentLineIndex) {
						// draw focus line
						
						final int lineCount = lineBreak.getLineCount();
						for (int j = 0; j < lineCount; j++) {
							String text = lineBreak.getLine(j);
							offsetY += mLineHeight;
							canvas.drawText(text, offsetX, offsetY, mTextPaintHighlight);
						}
					} else {
						// draw the other line
						
						final int lineCount = lineBreak.getLineCount();
						for (int j = 0; j < lineCount; j++) {
							String text = lineBreak.getLine(j);
							offsetY += mLineHeight;
							canvas.drawText(text, offsetX, offsetY, mTextPaintDefault);
						}
					}
				}
			} else {
				MyLog.d("lyric not load");
				drawErrorMessage(canvas);
			}
		}
		
		private void drawErrorMessage(Canvas canvas) {
			canvas.drawText(mErrorMessage, getWidth()/2, mLineHeight, mTextPaintHighlight);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
			int measuredHeight = mLyricLoaded ? mPanelHeight : getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

			MyLog.d("mLyricLoaded = " + mLyricLoaded + ", measureWidth = " + measuredWidth + ", measuredHeight = " + measuredHeight);
			
			setMeasuredDimension(measuredWidth, measuredHeight);
		}
	}
	
	private static class LineBreak {
		private List<String> mLines;
		private int mHeight;
		
		private void addLine(String text) {
			mLines.add(text);
		}
		
		private LineBreak() {
			mLines = new ArrayList<String>();
		}
		
		public String getLine(int index) {
			return mLines.get(index);
		}
		
		public int getLineCount() {
			return mLines.size();
		}
		
		public int getHeight() {
			return mHeight;
		}
		
		public void setHeight(int height) {
			mHeight = height; 
		}
		
		public static LineBreak breakLine(String text, Paint paint, float width, int height) {
			MyLog.d("width = " + width + ", height = " + height + ", text = " + text);
			
			LineBreak lineBreak = new LineBreak();
			
			int maxlen = text.length();
			int totallen = 0;
			int measured = 0;
			
			while (totallen < maxlen) {
				measured = paint.breakText(text, totallen, maxlen, true, width, null);
				
				lineBreak.addLine(text.substring(totallen, totallen + measured));
				
				totallen += measured;
			}
			
			lineBreak.setHeight(height * lineBreak.getLineCount());
			
			return lineBreak;
		}
	}
}

