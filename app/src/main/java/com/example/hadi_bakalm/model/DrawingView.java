package com.example.hadi_bakalm.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    // SCROLL modu eklendi
    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL }

    private static class DrawPath {
        Path path;
        Paint paint;

        DrawPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    private List<DrawPath> paths = new ArrayList<>();
    private Path currentPath;
    private Paint currentPaint;

    private int currentColor = 0xFF09090B;
    private ToolMode currentTool = ToolMode.PEN;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initNewStroke();
    }

    private void initNewStroke() {
        currentPath = new Path();
        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);

        applyToolSettings(currentPaint);
    }

    private void applyToolSettings(Paint paint) {
        if (currentTool == ToolMode.ERASER) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setStrokeWidth(36f);
        } else if (currentTool == ToolMode.HIGHLIGHTER) {
            paint.setXfermode(null);
            paint.setColor(0x40EAB308);
            paint.setStrokeWidth(24f);
        } else { // PEN
            paint.setXfermode(null);
            paint.setColor(currentColor);
            paint.setStrokeWidth(6f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (DrawPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }

        if (currentPath != null && currentPaint != null && currentTool != ToolMode.SCROLL) {
            canvas.drawPath(currentPath, currentPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Eğer kullanıcı SCROLL (Gezinme) modundaysa, dokunmayı çizim tuvali ele geçirmez;
        // RecyclerView'ın dikeyde rahatça kaymasına izin verir.
        if (currentTool == ToolMode.SCROLL) {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        }

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                initNewStroke();
                currentPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                currentPath.lineTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                paths.add(new DrawPath(currentPath, currentPaint));
                currentPath = new Path();
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void setToolMode(ToolMode mode) {
        this.currentTool = mode;
    }

    public void setColor(int newColor) {
        this.currentColor = newColor;
        if (currentTool == ToolMode.PEN) {
            applyToolSettings(currentPaint);
        }
    }

    public void clearCanvas() {
        paths.clear();
        if (currentPath != null) {
            currentPath.reset();
        }
        invalidate();
    }
}