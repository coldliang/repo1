package com.cqupt.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.cqupt.R;


public class RoundProgressBar extends View {
	RectF oval;
	/** 
     * ���ʶ�������� 
     */  
    private Paint paint;  

      
    /** 
     * Բ������ɫ 
     */  
    private int roundColor;  
      
    /** 
     * Բ�����ȵ���ɫ 
     */  
    private int roundProgressColor;  
      
    /** 
     * �м���Ȱٷֱȵ��ַ�������ɫ 
     */  
    private int textColor;  
      
    /** 
     * �м���Ȱٷֱȵ��ַ��������� 
     */  
    private float textSize;  
      
    /** 
     * Բ���Ŀ�� 
     */  
    private float roundWidth;  
      
    
      
    /** 
     * ��ǰ���� 
     */  
    private int progress;  
    /** 
     * ������ 
     */  
    private int max; 
      
    public static final int STROKE = 0;  
    public static final int FILL = 1;  

	public RoundProgressBar(Context context) {
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint();
		oval = new RectF();
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,  
                R.styleable.RoundProgressBar);
		roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);  
	    roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);  
	    textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.GREEN);  
	    textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);  
	    roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);  
	    max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
	    mTypedArray.recycle();  
	}

	@Override
	protected void onDraw(Canvas canvas) {

		super.onDraw(canvas);
		/** 
         * �������Ĵ�Բ�� 
         */  
        int centre = getWidth()/2; //��ȡԲ�ĵ�x����  
        int radius = (int) (centre - roundWidth/2); //Բ���İ뾶  
        paint.setColor(roundColor); //����Բ������ɫ  
        paint.setStyle(Paint.Style.STROKE); //���ÿ���  
        paint.setStrokeWidth(roundWidth); //����Բ���Ŀ��  
        paint.setAntiAlias(true);  //�������   
        canvas.drawCircle(centre, centre, radius, paint); //����Բ��  
        /** 
         * �����Ȱٷֱ� 
         */  
        paint.setStrokeWidth(0);   
        paint.setColor(textColor);  
        paint.setTextSize(textSize);  
        paint.setTypeface(Typeface.DEFAULT_BOLD); //��������
        if(max != 0){//��ĸ����Ϊ��
        	int percent = (int)(((float)progress / (float)max) * 100);  //�м�Ľ��Ȱٷֱȣ���ת����float�ڽ��г������㣬��Ȼ��Ϊ0
        	float textWidth = paint.measureText(percent + "%");   //���������ȣ�������Ҫ��������Ŀ��������Բ���м�  
            canvas.drawText(percent+"%", centre - textWidth / 2, centre + textSize/2, paint); //�������Ȱٷֱ�
        }
        
        /** 
         * ��Բ�� ����Բ���Ľ��� 
         */   
        paint.setStrokeWidth(roundWidth); //����Բ���Ŀ��  
        paint.setColor(roundProgressColor);  //���ý��ȵ���ɫ  
        oval.set(centre - radius, centre - radius, centre  
                + radius, centre + radius);  //���ڶ����Բ������״�ʹ�С�Ľ���  
        paint.setStyle(Paint.Style.STROKE);
        if(max != 0){
        	canvas.drawArc(oval, 270, 360 * progress / max, false, paint);  //���ݽ��Ȼ�Բ�� 
        }
        }
	/** 
     * ���ý��ȵ����ֵ 
     * @param max 
     */  
    public synchronized void setMax(int max) {  
        if(max < 0){  
            throw new IllegalArgumentException("max not less than 0");  
        }  
        this.max = max;  
    }  
  
    /** 
     * ��ȡ����.��Ҫͬ�� 
     * @return 
     */  
    public synchronized int getProgress() {  
        return progress;  
    }  
  
    /** 
     * ���ý��ȣ���Ϊ�̰߳�ȫ�ؼ������ڿ��Ƕ��ߵ����⣬��Ҫͬ�� 
     * ˢ�½������postInvalidate()���ڷ�UI�߳�ˢ�� 
     * @param progress 
     */  
    public synchronized void setProgress(int progress) {  
        if(progress < 0){  
            throw new IllegalArgumentException("progress not less than 0");  
        }  
        if(progress > max){  
            progress = max;  
        }  
        if(progress <= max){  
            this.progress = progress;  
            postInvalidate();  
        }  
          
    }

	public int getRoundColor() {
		return roundColor;
	}

	public void setRoundColor(int roundColor) {
		this.roundColor = roundColor;
	}

	public int getRoundProgressColor() {
		return roundProgressColor;
	}

	public void setRoundProgressColor(int roundProgressColor) {
		this.roundProgressColor = roundProgressColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

	
    
	}
	


