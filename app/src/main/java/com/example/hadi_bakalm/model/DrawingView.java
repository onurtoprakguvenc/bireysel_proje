package com.example.hadi_bakalm.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    public enum ToolMode { PEN, HIGHLIGHTER, ERASER }

    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int currentColor = 0xFF09090B;
    private ToolMode currentTool = ToolMode.PEN;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(currentColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(6f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setToolMode(ToolMode mode) {
        this.currentTool = mode;
        if (mode == ToolMode.ERASER) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.setStrokeWidth(30f);
        } else if (mode == ToolMode.HIGHLIGHTER) {
            drawPaint.setColor(0x40EAB308); // Yarı saydam sarı
            drawPaint.setStrokeWidth(24f);
        } else {
            drawPaint.setColor(currentColor);
            drawPaint.setStrokeWidth(6f);
        }
    }

    public void setColor(int newColor) {
        this.currentColor = newColor;
        if (currentTool == ToolMode.PEN) {
            drawPaint.setColor(newColor);
        }
    }

    public void clearCanvas() {
        drawPath.reset();
        invalidate();
    }
}
