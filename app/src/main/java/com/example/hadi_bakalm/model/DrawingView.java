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

    // RECTANGLE modu eklendi
    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL, RECTANGLE }

    private static class DrawPath {
        Path path;
        Paint paint;

        DrawPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    // Ana Çizim Listesi
    private final List<DrawPath> paths = new ArrayList<>();
    // Geri Alınan Çizimleri Saklayan İleri Al (Redo) Listesi
    private final List<DrawPath> undonePaths = new ArrayList<>();

    private Path currentPath;
    private Paint currentPaint;

    private int currentColor = 0xFF09090B;
    private float currentStrokeWidth = 8f; // Dinamik fırça kalınlığı
    private ToolMode currentTool = ToolMode.PEN;

    // Şekil çizimi için ilk dokunma noktaları
    private float startX, startY;

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
            paint.setStrokeWidth(currentStrokeWidth * 3); // Silgi fırçadan daha geniş olur
        } else if (currentTool == ToolMode.HIGHLIGHTER) {
            paint.setXfermode(null);
            paint.setColor(0x40EAB308); // Yarı saydam sarı
            paint.setStrokeWidth(currentStrokeWidth * 2.5f);
        } else { // PEN veya RECTANGLE
            paint.setXfermode(null);
            paint.setColor(currentColor);
            paint.setStrokeWidth(currentStrokeWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Kaydedilmiş tüm yolları çiz
        for (DrawPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }

        // Şu an çizilmekte olan yolu çiz
        if (currentPath != null && currentPaint != null && currentTool != ToolMode.SCROLL) {
            canvas.drawPath(currentPath, currentPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                startX = touchX;
                startY = touchY;

                initNewStroke();
                currentPath.moveTo(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (currentTool == ToolMode.RECTANGLE) {
                    // Dikdörtgen çizilirken yolu anlık olarak güncelle
                    currentPath.reset();
                    float left = Math.min(startX, touchX);
                    float top = Math.min(startY, touchY);
                    float right = Math.max(startX, touchX);
                    float bottom = Math.max(startY, touchY);
                    currentPath.addRect(left, top, right, bottom, Path.Direction.CW);
                } else {
                    currentPath.lineTo(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                // Yeni bir hamle yapıldığında Redo (İleri Al) geçmişi temizlenir
                undonePaths.clear();
                paths.add(new DrawPath(currentPath, currentPaint));
                currentPath = new Path();
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }

    // MOD SEÇİMİ
    public void setToolMode(ToolMode mode) {
        this.currentTool = mode;
    }

    // RENK DEĞİŞTİRME
    public void setColor(int newColor) {
        this.currentColor = newColor;
        if (currentTool == ToolMode.PEN || currentTool == ToolMode.RECTANGLE) {
            applyToolSettings(currentPaint);
        }
    }

    // FIRÇA KALINLIĞI DEĞİŞTİRME
    public void setStrokeWidth(float width) {
        this.currentStrokeWidth = width;
        if (currentPaint != null) {
            applyToolSettings(currentPaint);
        }
    }

    // GERİ AL (UNDO)
    public void undo() {
        if (!paths.isEmpty()) {
            DrawPath lastPath = paths.remove(paths.size() - 1);
            undonePaths.add(lastPath);
            invalidate();
        }
    }

    // İLERİ AL (REDO)
    public void redo() {
        if (!undonePaths.isEmpty()) {
            DrawPath pathToRestore = undonePaths.remove(undonePaths.size() - 1);
            paths.add(pathToRestore);
            invalidate();
        }
    }

    // TUVAN TEMİZLE
    public void clearCanvas() {
        paths.clear();
        undonePaths.clear();
        if (currentPath != null) {
            currentPath.reset();
        }
        invalidate();
    }
}