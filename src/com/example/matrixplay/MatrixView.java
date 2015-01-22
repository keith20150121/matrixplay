package com.example.matrixplay;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

class IntRectF
{
	public int num;
	public RectF rt = new RectF();
	
	public void Add(final IntRectF other)
	{
		if (other.num == num)
			num += other.num;
	}
}

class RandomGenerator
{
	private static final Random m_rand = new Random(System.currentTimeMillis());
	private static final int[] s_candidate = new int[]{2, 4, 8};
	
	public int Next()
	{
		return s_candidate[m_rand.nextInt(2)];
	}
}

public class MatrixView extends TextView 
{
	private final int m_nLines = 4;
	private final int m_nColumns = 4;
	private IntRectF[][] m_elementRects;
	private Paint m_painter;
	private Paint m_textPainter;
	private boolean m_bInitElement = false;
	
	public MatrixView(Context context, AttributeSet attribs) {
		super(context, attribs);
		m_painter = new Paint();
		m_textPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_textPainter.setTextSize(100.0f);
		m_textPainter.setTextAlign(Paint.Align.CENTER);
		m_textPainter.setColor(Color.RED);
		m_painter.setColor(Color.BLACK);
	}

	private void InitElementRects(int nLine, int nColumn, int width, int height)
	{
		if (m_elementRects != null)
			return;
		
		float lineBlank = (float)height / nLine;
		float columnBlank = (float)width / nColumn;
		
		float xPos = 0.0f;
		float yPos = 0.0f;
		
		m_elementRects = new IntRectF[nLine][];
		for (int i = 0; i < nLine; ++i)
		{
			m_elementRects[i] = new IntRectF[nColumn];
			IntRectF[] rt = m_elementRects[i];
			for (int j = 0; j < nColumn; ++j)
			{
				rt[j] = new IntRectF();
				rt[j].rt.left = xPos;
				rt[j].rt.right = xPos + columnBlank;
				rt[j].rt.top = yPos;
				rt[j].rt.bottom = yPos + lineBlank;
				
				//test
				rt[j].num = i * j;
				
				xPos += columnBlank;
			}
			yPos += lineBlank;
			xPos = 0.0f;
		}
	}
	
	private void drawNumber(Canvas canvas)
	{
		Rect rfBound = new Rect();
		for (IntRectF[] ir : m_elementRects)
		{
			for (IntRectF ira : ir)
			{
				String strNum = String.valueOf(ira.num);
				m_textPainter.getTextBounds(strNum, 0, strNum.length(), rfBound);
				canvas.drawText(
						strNum, 
						ira.rt.centerX(), 
						ira.rt.top + (rfBound.height() + ira.rt.height()) / 2, 
						m_textPainter);
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		// clip edges
		int width = getWidth() - 1;
		if (width <= 0)
			return;
		
		int height = getHeight() - 1;
		
		int round = width > height ? height : width;
		
		if (!m_bInitElement)
		{
			m_bInitElement = true;
			InitElementRects(m_nLines, m_nColumns, round, round);
		}
		
		drawMatrix(canvas, m_nLines, m_nColumns, round, round);
		drawNumber(canvas);
	}
		
	private void drawMatrix(Canvas canvas, int nLine, int nColumn, int width, int height)
	{
		float lineBlank = (float)height / nLine;
		float columnBlank = (float)width / nColumn;
		// lines
		for (int i = 0; i <= nLine; ++i)
		{
			float yPos = i * lineBlank;
			canvas.drawLine(0, yPos, width, yPos, m_painter);
		}
		// columns
		for (int i = 0; i <= nColumn; ++i)
		{
			float xPos = i * columnBlank;
			canvas.drawLine(xPos, 0, xPos, height, m_painter);
		}
	}
}
