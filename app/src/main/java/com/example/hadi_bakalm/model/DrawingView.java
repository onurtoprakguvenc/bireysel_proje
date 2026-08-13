package com.example.hadi_bakalm.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL, RECTANGLE, CIRCLE, LINE }

    public interface OnDrawingChangeListener {
        void onDrawingChanged(String jsonContent);
    }

    private OnDrawingChangeListener onDrawingChangeListener;

    public void setOnDrawingChangeListener(OnDrawingChangeListener listener) {
        this.onDrawingChangeListener = listener;
    }

    public static class Point {
        public float x, y;
        public Point(float x, float y) { this.x = x; this.y = y; }
    }

    private static class DrawPath {
        Path path;
        Paint paint;
        List<Point> points;

        DrawPath(Path path, Paint paint, List<Point> points) {
            this.path = path;
            this.paint = paint;
            this.points = points;
        }
    }

    private final List<DrawPath> paths = new ArrayList<>();
    private final List<DrawPath> undonePaths = new ArrayList<>();

    private Path currentPath;
    private Paint currentPaint;
    private List<Point> currentPoints;

    private int currentColor = 0xFF09090B;
    private float currentStrokeWidth = 8f;
    private ToolMode currentTool = ToolMode.PEN;

    private float startX, startY;

    // SADECE DİKEY KAYDIRMA VE ZOOM DEĞİŞKENLERİ
    private float offsetY = 0f; // Sağ-Sol (offsetX) tamamen kaldırıldı
    private float lastTouchY;
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initNewStroke();

        // Zoom (Yakınlaştırma) Dinleyicisi
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f)); // Min 0.5x, Max 3.0x zoom
                invalidate();
                return true;
            }
        });
    }

    private void initNewStroke() {
        currentPath = new Path();
        currentPaint = new Paint();
        currentPoints = new ArrayList<>();

        currentPaint.setAntiAlias(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);

        applyToolSettings(currentPaint);
    }

    private void applyToolSettings(Paint paint) {
        if (paint == null) return;

        if (currentTool == ToolMode.ERASER) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setStrokeWidth(currentStrokeWidth * 3);
        } else if (currentTool == ToolMode.HIGHLIGHTER) {
            paint.setXfermode(null);
            int alphaColor = (currentColor & 0x00FFFFFF) | 0x66000000;
            if ((currentColor & 0x00FFFFFF) == 0x09090B || (currentColor & 0x00FFFFFF) == 0x000000) {
                alphaColor = 0x66EAB308;
            }
            paint.setColor(alphaColor);
            paint.setStrokeWidth(currentStrokeWidth * 3f);
        } else {
            paint.setXfermode(null);
            paint.setColor(currentColor);
            paint.setStrokeWidth(currentStrokeWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        // ZOOM VE SADECE DİKEY (Y) DÖNÜŞÜMÜ
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(0, offsetY);

        for (DrawPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }

        if (currentPath != null && currentPaint != null && currentTool != ToolMode.SCROLL) {
            canvas.drawPath(currentPath, currentPaint);
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Zoom hareketini işle
        scaleGestureDetector.onTouchEvent(event);

        int pointerCount = event.getPointerCount();

        // 1. İKİ PARMAK İLE KAYDIRMA VEYA SCROLL MODU (Sadece Dikey)
        if (pointerCount > 1 || currentTool == ToolMode.SCROLL) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    lastTouchY = event.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!scaleGestureDetector.isInProgress()) {
                        float newY = event.getY();
                        float dy = (newY - lastTouchY) / scaleFactor;

                        offsetY += dy;

                        // Sayfanın üst sınırını koru (Sayfa aşağı kaçmasın)
                        if (offsetY > 0) {
                            offsetY = 0;
                        }

                        lastTouchY = newY;
                        invalidate();
                    }
                    break;
            }
            return true;
        }

        // 2. TEK PARMAK / S-PEN İLE DİKEY YÖNLÜ ÇİZİM
        float rawX = event.getX();
        float rawY = event.getY();

        // Dokunma noktasını Zoom ve Dikey Offset'e göre hesapla
        float touchX = rawX / scaleFactor;
        float touchY = (rawY / scaleFactor) - offsetY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = touchX;
                startY = touchY;

                initNewStroke();
                currentPath.moveTo(touchX, touchY);
                currentPoints.add(new Point(touchX, touchY));
                break;

            case MotionEvent.ACTION_MOVE:
                if (currentTool == ToolMode.RECTANGLE) {
                    currentPath.reset();
                    float left = Math.min(startX, touchX);
                    float top = Math.min(startY, touchY);
                    float right = Math.max(startX, touchX);
                    float bottom = Math.max(startY, touchY);
                    currentPath.addRect(left, top, right, bottom, Path.Direction.CW);
                } else if (currentTool == ToolMode.CIRCLE) {
                    currentPath.reset();
                    float left = Math.min(startX, touchX);
                    float top = Math.min(startY, touchY);
                    float right = Math.max(startX, touchX);
                    float bottom = Math.max(startY, touchY);
                    currentPath.addOval(left, top, right, bottom, Path.Direction.CW);
                } else if (currentTool == ToolMode.LINE) {
                    currentPath.reset();
                    currentPath.moveTo(startX, startY);
                    currentPath.lineTo(touchX, touchY);
                } else {
                    currentPath.lineTo(touchX, touchY);
                    currentPoints.add(new Point(touchX, touchY));
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                undonePaths.clear();
                paths.add(new DrawPath(currentPath, new Paint(currentPaint), currentPoints));
                currentPath = new Path();

                if (onDrawingChangeListener != null) {
                    onDrawingChangeListener.onDrawingChanged(getDrawingJson());
                }
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }

    public String getDrawingJson() {
        try {
            JSONArray pathsArray = new JSONArray();
            for (DrawPath dp : paths) {
                JSONObject pathObj = new JSONObject();
                pathObj.put("color", dp.paint.getColor());
                pathObj.put("strokeWidth", dp.paint.getStrokeWidth());

                JSONArray pointsArray = new JSONArray();
                for (Point p : dp.points) {
                    JSONObject pointObj = new JSONObject();
                    pointObj.put("x", p.x);
                    pointObj.put("y", p.y);
                    pointsArray.put(pointObj);
                }
                pathObj.put("points", pointsArray);
                pathsArray.put(pathObj);
            }
            return pathsArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void loadDrawingFromJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return;
        try {
            paths.clear();
            JSONArray pathsArray = new JSONArray(jsonStr);

            for (int i = 0; i < pathsArray.length(); i++) {
                JSONObject pathObj = pathsArray.getJSONObject(i);
                int color = pathObj.getInt("color");
                float strokeWidth = (float) pathObj.getDouble("strokeWidth");

                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setColor(color);
                paint.setStrokeWidth(strokeWidth);

                JSONArray pointsArray = pathObj.getJSONArray("points");
                Path path = new Path();
                List<Point> points = new ArrayList<>();

                for (int j = 0; j < pointsArray.length(); j++) {
                    JSONObject pointObj = pointsArray.getJSONObject(j);
                    float x = (float) pointObj.getDouble("x");
                    float y = (float) pointObj.getDouble("y");
                    points.add(new Point(x, y));

                    if (j == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                paths.add(new DrawPath(path, paint, points));
            }

            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setToolMode(ToolMode mode) {
        this.currentTool = mode;
        initNewStroke();
        invalidate();
    }

    public void setColor(int newColor) {
        this.currentColor = newColor;
        initNewStroke();
        invalidate();
    }

    public void setStrokeWidth(float width) {
        this.currentStrokeWidth = width;
        initNewStroke();
        invalidate();
    }

    public void undo() {
        if (!paths.isEmpty()) {
            DrawPath lastPath = paths.remove(paths.size() - 1);
            undonePaths.add(lastPath);
            invalidate();
            if (onDrawingChangeListener != null) {
                onDrawingChangeListener.onDrawingChanged(getDrawingJson());
            }
        }
    }

    public void redo() {
        if (!undonePaths.isEmpty()) {
            DrawPath pathToRestore = undonePaths.remove(undonePaths.size() - 1);
            paths.add(pathToRestore);
            invalidate();
            if (onDrawingChangeListener != null) {
                onDrawingChangeListener.onDrawingChanged(getDrawingJson());
            }
        }
    }

    public void clearCanvas() {
        paths.clear();
        undonePaths.clear();
        if (currentPath != null) currentPath.reset();
        offsetY = 0f;
        scaleFactor = 1.0f;
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public void addTableToCanvas(int rows, int cols) {
        float startX = 100f;
        float startY = -offsetY + 200f; // O an ekranda görünen yerin üst kısmına koyar
        float cellWidth = 150f;
        float cellHeight = 80f;

        float totalWidth = cols * cellWidth;
        float totalHeight = rows * cellHeight;

        Paint tablePaint = new Paint();
        tablePaint.setAntiAlias(true);
        tablePaint.setStyle(Paint.Style.STROKE);
        tablePaint.setColor(0xFF334155); // Koyu gri tablo çizgileri
        tablePaint.setStrokeWidth(4f);

        // Yatay Çizgiler
        for (int i = 0; i <= rows; i++) {
            Path yPath = new Path();
            float y = startY + (i * cellHeight);
            yPath.moveTo(startX, y);
            yPath.lineTo(startX + totalWidth, y);

            List<Point> points = new ArrayList<>();
            points.add(new Point(startX, y));
            points.add(new Point(startX + totalWidth, y));

            paths.add(new DrawPath(yPath, new Paint(tablePaint), points));
        }

        // Dikey Çizgiler
        for (int j = 0; j <= cols; j++) {
            Path xPath = new Path();
            float x = startX + (j * cellWidth);
            xPath.moveTo(x, startY);
            xPath.lineTo(x, startY + totalHeight);

            List<Point> points = new ArrayList<>();
            points.add(new Point(x, startY));
            points.add(new Point(x, startY + totalHeight));

            paths.add(new DrawPath(xPath, new Paint(tablePaint), points));
        }

        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }
}