package com.example.matrixplay;

import java.util.ArrayList;
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
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

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
	public static final int ZERO = 0;
	public static final int MERGED = 1;
	public static final int BLOCKED = 2;
	
	public int num = 0;
	public RectF rt = new RectF();
	public Position pos = new Position();
	
	public int Add(IntRectF other)
	{		
		if (other.num == num)
		{
			if (num == 0)
				return ZERO;
				
			num += other.num;
			other.num = 0;
			return MERGED;
		}
		else if (num == 0 || other.num == 0)
		{			
			num += other.num;
			other.num = 0;
			return ZERO;
		}
		else
			return BLOCKED;
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
		if (m_candidate.length <= 0)
			return -1;
		
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
	
	private final int m_nLines = 5;
	private final int m_nColumns = 5;
	private final float m_fDefaultTextSize = 400.0f / m_nColumns;
	private final float m_fDescent = 15.0f;
	private SparseArray<Float> m_textSizeMap = new SparseArray<Float>();
	private IntRectF[][] m_elementRects;
	private RandomGenerator m_rg;
	private Paint m_painter;
	private Paint m_textPainter;
	private boolean m_bInitElement = false;
	
	static int makeMark(int num, int i)
	{
		if (num != 0)
			return (1 << i);
		else
			return 0;
	}
	
	public MatrixView(Context context, AttributeSet attribs) {
		super(context, attribs);
		m_painter = new Paint();
		m_textPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_textPainter.setTextSize(m_fDefaultTextSize);
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
	
	private ArrayList<Integer> collectNotZero(IntRectF[] ira)
	{
		ArrayList<Integer> c = new ArrayList<Integer>();
		for (int i = 0; i < ira.length; ++i)
		{
			if (ira[i].num != 0)
				c.add(ira[i].num);
		}
		return c;
	}
	
	private ArrayList<Integer> collectNotZero(int iColumn)
	{
		if (iColumn < 0 || iColumn >= m_nColumns)
			return null;

		ArrayList<Integer> c = new ArrayList<Integer>();
		for (int i = 0; i < m_elementRects.length; ++i)
		{
			if (m_elementRects[i][iColumn].num != 0)
				c.add(m_elementRects[i][iColumn].num);
		}
		return c;
	}
	
	private boolean canMoveFromRightToleft(IntRectF[] ira)
	{
		boolean bFirstCount = false;
		boolean bZeroCount = false;
		for (int i = 0; i < ira.length; --i)
		{
			if (!bFirstCount)
			{
				if (ira[i].num == 0)
				{
					bZeroCount = true;
				}

				bFirstCount = true;
				continue;
			}
			
			if (!bZeroCount)
			{
				if (ira[i].num == 0)
					bZeroCount = true;
			}
			else if (ira[i].num != 0)
				return true;				
		}
		
		return false;
	}
	
	private boolean canMoveFromLeftToRight(IntRectF[] ira)
	{
		boolean bFirstCount = false;
		boolean bZeroCount = false;
		for (int i = ira.length - 1; i >= 0; --i)
		{
			if (!bFirstCount)
			{
				if (ira[i].num == 0)
				{
					bZeroCount = true;
				}

				bFirstCount = true;
				continue;
			}
			
			if (!bZeroCount)
			{
				if (ira[i].num == 0)
					bZeroCount = true;
			}
			else if (ira[i].num != 0)
				return true;				
		}
		
		return false;
	}
	
	private boolean canMoveFromUpToDown(int iColumn)
	{
		if (iColumn >= m_elementRects.length || iColumn < 0)
			return false;
		
		boolean bFirstCount = false;
		boolean bZeroCount = false;
		for (int i = 0; i < m_elementRects.length; ++i)
		{
			if (!bFirstCount)
			{
				if (m_elementRects[i][iColumn].num == 0)
				{
					bZeroCount = true;
				}

				bFirstCount = true;
				continue;
			}
			
			if (!bZeroCount)
			{
				if (m_elementRects[i][iColumn].num == 0)
					bZeroCount = true;
			}
			else if (m_elementRects[i][iColumn].num != 0)
				return true;				
		}
		return false;
	}
	
	private boolean canMoveFromDownToUp(int iColumn)
	{
		if (iColumn >= m_elementRects.length || iColumn < 0)
			return false;
		
		boolean bFirstCount = false;
		boolean bZeroCount = false;
		for (int i = m_elementRects.length - 1; i >= 0; --i)
		{
			if (!bFirstCount)
			{
				if (m_elementRects[i][iColumn].num == 0)
				{
					bZeroCount = true;
				}

				bFirstCount = true;
				continue;
			}
			
			if (!bZeroCount)
			{
				if (m_elementRects[i][iColumn].num == 0)
					bZeroCount = true;
			}
			else if (m_elementRects[i][iColumn].num != 0)
				return true;				
		}
		
		return false;
	}
	
	private boolean moveLeft(IntRectF[] ira)
	{
	//	if (!canMoveFromRightToleft(ira))
	//		return false;
		
		ArrayList<Integer> c = collectNotZero(ira);
		
		if (c.size() == 0)
			return false;
		
		int i = 0;
		for (; i < c.size(); ++i)
		{
			ira[i].num = c.get(i);
		}
		
		for (; i < ira.length; ++i)
		{
			ira[i].num = 0;
		}
		
		return true;
	}
	
	private boolean moveRight(IntRectF[] ira)
	{
	//	if (!canMoveFromLeftToRight(ira))
	//		return false;
		
		ArrayList<Integer> c = collectNotZero(ira);
		
		if (c.size() == 0)
			return false;
		
		int n = ira.length - c.size();
		for (int i = 0; i < n; ++i)
		{
			ira[i].num = 0;
		}
		
		for (int i = n, j = 0; i < ira.length; ++i, ++j)
		{
			ira[i].num = c.get(j);
		}
		
		return true;
	}
	
	private boolean moveDown(int iColumn)
	{				
		if (iColumn >= m_nColumns || iColumn < 0)
			return false;
		
	//	if (!canMoveFromUpToDown(iColumn))
	//		return false;
		
		ArrayList<Integer> c = collectNotZero(iColumn);
		
		if (c.size() == 0)
			return false;
		
		int n = m_nColumns - c.size();
		
		for (int i = 0; i < n; ++i)
		{
			m_elementRects[i][iColumn].num = 0;
		}
		
		for (int i = n, j = 0; i < m_elementRects.length; ++i, ++j)
		{
			m_elementRects[i][iColumn].num = c.get(j);
		}
		
		return true;
	}
	
	private boolean moveUp(int iColumn)
	{	
		if (iColumn >= m_nColumns || iColumn < 0)
			return false;
		
	//	if (!canMoveFromDownToUp(iColumn))
	//		return false;
		
		ArrayList<Integer> c = collectNotZero(iColumn);
		
		if (c.size() == 0)
			return false;
				
		int i = 0;
		for (; i < c.size(); ++i)
		{
			m_elementRects[i][iColumn].num = c.get(i);
		}
		
		for (; i < m_elementRects.length; ++i)
		{
			m_elementRects[i][iColumn].num = 0;
		}
		
		return true;
	}
	
	private void slideLeft()
	{
		boolean bRefresh = false;
		int beforeMerge = 0;
		int afterMerge  = 0;
		
		for (IntRectF[] ira : m_elementRects)
		{
			beforeMerge |= makeMark(ira[0].num, 0);
			for (int i = ira.length - 1; i > 0; --i)
			{
				beforeMerge |= makeMark(ira[i].num, i);
				ira[i - 1].Add(ira[i]);
				afterMerge |= makeMark(ira[i].num, i);				
			}
			afterMerge |= makeMark(ira[0].num, 0);
			
			if (beforeMerge != afterMerge)
			{
				moveLeft(ira);
				bRefresh = true;
			}
			
			beforeMerge = afterMerge = 0;
		}
		
		if (bRefresh)
		{
			genNumber();
			invalidate();
		}
	}
	
	private void slideRight()
	{
		boolean bRefresh = false;
		int beforeMerge = 0;
		int afterMerge  = 0;
		
		for (IntRectF[] ira : m_elementRects)
		{
			beforeMerge |= makeMark(ira[ira.length - 1].num, ira.length - 1);
			for (int i = 0; i < ira.length - 1 ; ++i)
			{
				beforeMerge |= makeMark(ira[i].num, i);
				ira[i + 1].Add(ira[i]);
				afterMerge |= makeMark(ira[i].num, i);
			}
			afterMerge |= makeMark(ira[ira.length - 1].num, ira.length - 1);
			
			if (beforeMerge != afterMerge)
			{
				moveRight(ira);
				bRefresh = true;
			}
			
			beforeMerge = afterMerge = 0;
		}	
		
		if (bRefresh)
		{
			genNumber();
			invalidate();
		}
	}
	
	private void slideUp()
	{
		boolean bRefresh = false;
		int beforeMerge = 0;
		int afterMerge  = 0;
		
		for (int j = 0; j < m_nColumns; ++j)
		{	
			beforeMerge |= makeMark(m_elementRects[0][j].num, 0);
			for (int i = m_elementRects.length - 1; i > 0; --i)
			{
				beforeMerge |= makeMark(m_elementRects[i][j].num, i);	
				m_elementRects[i - 1][j].Add(m_elementRects[i][j]);
				afterMerge |= makeMark(m_elementRects[i][j].num, i);	
			}
			afterMerge |= makeMark(m_elementRects[0][j].num, 0);			
			
			if (beforeMerge != afterMerge)
			{
				moveUp(j);
				bRefresh = true;
			}

			beforeMerge = afterMerge = 0;
		}

		if (bRefresh)
		{
			genNumber();
			invalidate();
		}
	}
	
	private void slideDown()
	{
		boolean bRefresh = false;
		int beforeMerge = 0;
		int afterMerge  = 0;
		
		for (int j = 0; j < m_nColumns; ++j)
		{	
			beforeMerge |= makeMark(m_elementRects[m_elementRects.length - 1][j].num, m_elementRects.length - 1);
			for (int i = 0; i < m_elementRects.length - 1; ++i)
			{
				beforeMerge |= makeMark(m_elementRects[i][j].num, i);
				m_elementRects[i + 1][j].Add(m_elementRects[i][j]);
				afterMerge |= makeMark(m_elementRects[i][j].num, i);

			}
			afterMerge |= makeMark(m_elementRects[m_elementRects.length - 1][j].num, m_elementRects.length - 1);

			if (beforeMerge != afterMerge)
			{
				moveDown(j);
				bRefresh = true;
			}

			beforeMerge = afterMerge = 0;
		}

		if (bRefresh)
		{
			genNumber();
			invalidate();
		}
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
		final RectF compExp = m_elementRects[0][0].rt;
		float fTextSize = m_fDefaultTextSize;
		
		for (IntRectF[] ira : m_elementRects)
		{
			for (IntRectF ir : ira)
			{
				if (ir.num == 0)
					continue;
				
				String strNum = String.valueOf(ir.num);					
				Float f = m_textSizeMap.get(strNum.length());

				if (f == null)
				{
					m_textPainter.getTextBounds(strNum, 0, strNum.length(), rfBound);
					RectF rfBoundF = new RectF();	
					rfBoundF.set(
							Math.abs(rfBound.left), 
							Math.abs(rfBound.top), 
							Math.abs(rfBound.right), 
							Math.abs(rfBound.bottom));	
					
					while (!compExp.contains(rfBoundF) && Float.compare(fTextSize, 0.0f) > 0)
					{
						fTextSize -= m_fDescent;
						m_textPainter.setTextSize(fTextSize);
						m_textPainter.getTextBounds(strNum, 0, strNum.length(), rfBound);
						rfBoundF.set(
								Math.abs(rfBound.left), 
								Math.abs(rfBound.top), 
								Math.abs(rfBound.right), 
								Math.abs(rfBound.bottom));
					}

					m_textPainter.setTextSize(fTextSize);
					m_textSizeMap.put(strNum.length(), fTextSize);
					fTextSize = m_fDefaultTextSize;
				}
				else
				{
					m_textPainter.setTextSize(f);
					m_textPainter.getTextBounds(strNum, 0, strNum.length(), rfBound);
				}
				
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
