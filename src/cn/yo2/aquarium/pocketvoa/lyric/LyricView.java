package cn.yo2.aquarium.pocketvoa.lyric;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import cn.yo2.aquarium.pocketvoa.R;

public class LyricView extends View {
	private static final String TAG = LyricView.class.getSimpleName();

	private static final String DEFAULT_ERROR_MESSAGE = "Cannot load lyric.";
	private static final int DEFAULT_FOCUSED_LINE_COLOR = Color.WHITE;
	private static final int DEFAULT_OTHER_LINE_COLOR = Color.GRAY;
	private static final float DEFAULT_TEXT_SIZE = 100;

	private final static long DISAPPEAR_TIME = 1000L;// 歌词从显示完到消失的时间

	private int mFocusLineColor;
	private int mOtherLineColor;

	private TextPaint mTextPaint;
	private int mLineHeight;

	// Current playing Sentence line number
	private int mCurrentLineIndex;
	// Current playing line vertical offset from half of view height
	private int mCurrentVerticalOffset;
	// Current playing Sentence
	private Sentence mCurrentSentence;

	// The Lyric object
	private Lyric mLyric;

	private List<Float> mLineLeftX;

	// View width
	private int mWidth;
	// View height
	private int mHeight;

	// Half of view height
	private int mHalfHeight;

	private int mOtherLineCount;

	private String mErrorMessage;

	private boolean mLyricLoaded;

	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);

		Log.d(TAG, "in LyricView Constructor");

		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

		TypedArray params = context.obtainStyledAttributes(attrs,
				R.styleable.cn_yo2_aquarium_pocketvoa_lyric_LyricView);

		mErrorMessage = params
				.getString(R.styleable.cn_yo2_aquarium_pocketvoa_lyric_LyricView_errorMessage);

		if (mErrorMessage == null)
			mErrorMessage = DEFAULT_ERROR_MESSAGE;

		mFocusLineColor = params
				.getColor(
						R.styleable.cn_yo2_aquarium_pocketvoa_lyric_LyricView_focusLineColor,
						DEFAULT_FOCUSED_LINE_COLOR);

		mOtherLineColor = params
				.getColor(
						R.styleable.cn_yo2_aquarium_pocketvoa_lyric_LyricView_otherLineColor,
						DEFAULT_OTHER_LINE_COLOR);

		float textSize = params.getDimension(
				R.styleable.cn_yo2_aquarium_pocketvoa_lyric_LyricView_textSize,
				DEFAULT_TEXT_SIZE);
		mTextPaint.setTextSize(textSize);

		FontMetricsInt fmi = mTextPaint.getFontMetricsInt();
		mLineHeight = fmi.bottom - fmi.top;

		mLyric = new Lyric();

		mLineLeftX = new ArrayList<Float>();
	}

	public boolean loadLyric(InputStream is) {
		Log.d(TAG, "in loadLyric");
		mLyricLoaded = mLyric.parseLyric(is);
		if (mLyricLoaded)
			resetLyric();
		return mLyricLoaded;
	}
	
	public void resetLyric() {
		mCurrentLineIndex = 0;
		mCurrentSentence = mLyric.getSentence(mCurrentLineIndex);
	}

	private void calculateLineLeftX() {
		mLineLeftX.clear();
		for (int i = 0; i < mLyric.getSize(); i++) {
			mLineLeftX.add((mWidth - mTextPaint.measureText(mLyric
					.getSentence(i).mContent)) / 2);
		}
	}

	public boolean update(long time) {
		Log.d(TAG, "in update time -- " + time);
		if (mCurrentLineIndex == mLyric.getSize() - 1)
			return false;

		// if (mCurrentSentence == null) {
		// mCurrentIndex = 0;
		// } else if (time > mCurrentSentence.mToTime) {
		// mCurrentIndex++;
		// }
		
		mCurrentLineIndex = mLyric.getSentenceIndexInTimeFast(time);

//		mCurrentLineIndex = mLyric.getSentenceIndexInTime(time);
		mCurrentSentence = mLyric.getSentence(mCurrentLineIndex);
		mCurrentVerticalOffset = getVerticalOffset(mCurrentSentence.mFromTime,
				mCurrentSentence.mToTime, time);

		invalidate();
		return true;
	}

	private static int getGradientColor(int c1, int c2, float f) {
		int deltaR = Color.red(c2) - Color.red(c1);
		int deltaG = Color.green(c2) - Color.green(c1);
		int deltaB = Color.blue(c2) - Color.blue(c1);

		int r1 = (int) (Color.red(c1) + deltaR * f);
		int g1 = (int) (Color.green(c1) + deltaG * f);
		int b1 = (int) (Color.blue(c1) + deltaB * f);

		return Color.rgb(r1, g1, b1);
	}

	/**
	 * 根据当前指定的时候,得到这个时候应该 取渐变色的哪个阶段了,目前的算法是从 快到结束的五分之一处开始渐变,这样平缓一些
	 * 
	 * @param c1
	 *            高亮色
	 * @param c2
	 *            普通色
	 * @param fromtime
	 *            歌词开始时间
	 * @param totime
	 *            歌词结束时间
	 * @param time
	 *            时间
	 * 
	 * @return 新的渐入颜色
	 */
	private static int getBestInColor(int c1, int c2, long fromtime,
			long totime, long time) {
		long during = totime - fromtime;
		float f = (time - fromtime) * 1.0f / during;
		if (f > 0.1f) {// 如果已经过了十分之一的地方,就直接返高亮色
			return c1;
		} else {
			f = (time - fromtime) * 1.0f / (during * 0.1f);
			if (f > 1 || f < 0) {
				return c1;
			}
			return getGradientColor(c2, c1, f);
		}
	}

	/**
	 * 得到最佳的渐出颜色
	 * 
	 * @param c1
	 *            高亮色
	 * @param c2
	 *            普通色
	 * @param time
	 * @return
	 */
	public static int getBestOutColor(int c1, int c2, long fromtime,
			long totime, long time) {
		if (time >= fromtime && time <= totime) {
			return c1;
		}
		float f = (time - totime) * 1.0f / DISAPPEAR_TIME;
		if (f > 1f || f <= 0) {// 如果时间已经超过了最大的时间了，则直接返回原来的颜色
			return c2;
		} else {
			return getGradientColor(c1, c2, f);
		}
	}

	/**
	 * 得到V方向的增量
	 * 
	 * @param time
	 *            时间
	 * @return 增量
	 */
	private int getVerticalOffset(long fromtime, long totime, long time) {
		return (int) (mLineHeight * ((time - fromtime) * 1.0 / (totime - fromtime)));
	}

	private void drawFocusedLine(Canvas canvas) {
		Log.d(TAG, "CurrentIndex -- " + mCurrentLineIndex);

		mTextPaint.setColor(mFocusLineColor);

		String content = mCurrentSentence.mContent;
		canvas.drawText(content, mLineLeftX.get(mCurrentLineIndex), mHalfHeight
				- mCurrentVerticalOffset, mTextPaint);
	}

	private void drawOtherLines(Canvas canvas) {
		mTextPaint.setColor(mOtherLineColor);
		// draw lines above focused line
		for (int index = mCurrentLineIndex - 1, y = mHalfHeight - mCurrentVerticalOffset
				- mLineHeight, count = 0; count < mOtherLineCount 
				&& index >= 0; index--, y -= mLineHeight, count++) {
			String content = mLyric.getSentence(index).mContent;
			// Log.d(TAG, "line i " + index + " has content -- " + content);
			canvas.drawText(content, mLineLeftX.get(index), y, mTextPaint);
		}

		// draw lines below focused line
		for (int index = mCurrentLineIndex + 1, y = mHalfHeight - mCurrentVerticalOffset
				+ mLineHeight, count = 0; count < mOtherLineCount
				&& index < mLyric.getSize(); index++, y += mLineHeight, count++) {
			String content = mLyric.getSentence(index).mContent;
			// Log.d(TAG, "line i " + index + " has content -- " + content);
			canvas.drawText(content, mLineLeftX.get(index), y, mTextPaint);
		}
	}

	private void drawErrorMessage(Canvas canvas) {
		mTextPaint.setColor(mFocusLineColor);

		canvas.drawText(mErrorMessage, (mWidth - mTextPaint
				.measureText(mErrorMessage)) / 2, mHeight / 2, mTextPaint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "in onDraw");
		if (mLyricLoaded) {
			drawFocusedLine(canvas);
			drawOtherLines(canvas);
		} else {
			drawErrorMessage(canvas);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mWidth = w - getPaddingLeft() - getPaddingRight();
		mHeight = h - getPaddingBottom() - getPaddingTop();

		mHalfHeight = mHeight / 2;
		mOtherLineCount = mHalfHeight / 2;

		if (mLyricLoaded)
			calculateLineLeftX();
	}

	public void setLyricLoaded() {
		mLyricLoaded = true;
	}

	public void clearLyricLoaded() {
		mLyricLoaded = false;
	}
}
