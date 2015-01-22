package com.example.matrixplay;

import java.util.HashSet;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

class Position
{
	public int line;
	public int column;
	
	public Position()
	{
		line = column = 0;
	}
	
	public Position(int l, int c)
	{
		setPosition(l, c);
	}
	
	public void setPosition(int l, int c)
	{
		line = l;
		column = c;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Position)
		{
			Position other = (Position)obj;
			return line == other.line && column == other.column;
		}
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return line * 10 + column;
	}
}

class IntRectF
{
	public int num = 0;
	public RectF rt = new RectF();
	public Position pos = new Position();
	
	public boolean Add(IntRectF other)
	{
		if (other.num == num || num == 0)
		{
			num += other.num;
			other.num = 0;
			return true;
		}
		return false;
	}
}

final class RandomGenerator 
{
	private final Random m_rand = new Random(System.currentTimeMillis());
	private final int[] m_candidate = new int[]{0, 0, 2, 2, 2, 2, 4, 4, 8};
	private IntRectF[][] m_elementRects;
	private HashSet<Position> m_positions = new HashSet<Position>();
	
	public RandomGenerator(final IntRectF[][] rects)
	{
		m_elementRects = rects;
	}
	
	public int NextNumber()
	{
		int idx = m_rand.nextInt(m_candidate.length);
		return m_candidate[idx];
	}
	
	public Position NextPosition()
	{
		for (IntRectF[] ira : m_elementRects)
		{
			for (IntRectF ir : ira)
			{
				if (ir.num == 0)
					m_positions.add(ir.pos);
				else
					m_positions.remove(ir.pos);
			}
		}
		int n = m_positions.size();
		int rand = m_rand.nextInt(n);
		int i = 0;
		
		if (m_positions.size() == 0)
			return null;
		
		for (Position pos : m_positions)
		{
			if (i++ < rand)
				continue;
			else
				return pos;
		}
		
		return null;
	}
}

public class MatrixView extends TextView 
{
	private static final String TAG = "MatrixView";
	
	private final int m_nLines = 4;
	private final int m_nColumns = 4;
	private IntRectF[][] m_elementRects;
	private RandomGenerator m_rg;
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
		handleOnTouch();		
	}	
	
	private void genNumber()
	{
		int num = m_rg.NextNumber();
		if (num == 0)
			return;
		
		Position pos = m_rg.NextPosition();
		if (pos == null)
			return;
		
		if (pos.column >= m_nColumns || pos.line >= m_nLines)
		{
			Log.e(TAG, "Pos out of range!!");
			return;
		}
		
		m_elementRects[pos.line][pos.column].num = num;
	}
	
	private void slideLeft()
	{
		boolean bNew = true;
		for (IntRectF[] ira : m_elementRects)
		{
			for (int i = ira.length - 1; i > 0 ; --i)
			{
				bNew = ira[i - 1].Add(ira[i]);
			}
		}
		
		genNumber();
		invalidate();
	}
	
	private void slideRight()
	{
		for (IntRectF[] ira : m_elementRects)
		{
			for (int i = 0; i < ira.length - 1 ; ++i)
			{
				ira[i + 1].Add(ira[i]);
			}
		}	
		
		genNumber();
		invalidate();
	}
	
	private void slideUp()
	{
		for (int j = 0; j < m_nColumns; ++j)
		{		
			for (int i = m_elementRects.length - 1; i > 0; --i)
			{
				m_elementRects[i - 1][j].Add(m_elementRects[i][j]);
			}
		}

		genNumber();
		invalidate();
	}
	
	private void slideDown()
	{
		for (int j = 0; j < m_nColumns; ++j)
		{		
			for (int i = 0; i < m_elementRects.length - 1; ++i)
			{
				m_elementRects[i + 1][j].Add(m_elementRects[i][j]);
			}
		}

		genNumber();
		invalidate();
	}
	
	private void handleOnTouch()
	{
		this.setOnTouchListener(new OnTouchListener()
		{
			private float m_x = 0.0f;
			private float m_y = 0.0f;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				switch(event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
					m_x = event.getX();
					m_y = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
				{
					float disX = m_x - event.getX();
					float disY = m_y - event.getY();
					
					if (Math.abs(disX) > Math.abs(disY))
					{
						int ret = Float.compare(disX, 0.0f);
						if (ret > 0)
						{
							// move left
							Log.d(TAG, "Move Left");
							slideLeft();
						}
						else if (ret < 0)
						{
							// move right
							Log.d(TAG, "Move Right");
							slideRight();
						}
					}
					else
					{
						int ret = Float.compare(disY, 0.0f);
						if (ret > 0)
						{
							// move up
							Log.d(TAG, "Move Up");
							slideUp();
						}
						else if (ret < 0)
						{
							// move down
							Log.d(TAG, "Move Down");
							slideDown();
						}						
					}
					
				}
					break;
				}
				return true;
			}			
		});
	}
	
	private void initElementRects(int nLine, int nColumn, int width, int height)
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
				rt[j].pos.setPosition(i, j);
				//test
				//rt[j].num = i * j;
				
				xPos += columnBlank;
			}
			yPos += lineBlank;
			xPos = 0.0f;
		}
		
		m_rg = new RandomGenerator(m_elementRects);
		
		for (int i = 0; i < m_nLines; ++i)
			genNumber();
	}
	
	private void drawNumber(Canvas canvas)
	{
		Rect rfBound = new Rect();
		for (IntRectF[] ira : m_elementRects)
		{
			for (IntRectF ir : ira)
			{
				if (ir.num == 0)
					continue;
				
				String strNum = String.valueOf(ir.num);
				m_textPainter.getTextBounds(strNum, 0, strNum.length(), rfBound);
				canvas.drawText(
						strNum, 
						ir.rt.centerX(), 
						ir.rt.top + (rfBound.height() + ir.rt.height()) / 2, 
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
			initElementRects(m_nLines, m_nColumns, round, round);
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
