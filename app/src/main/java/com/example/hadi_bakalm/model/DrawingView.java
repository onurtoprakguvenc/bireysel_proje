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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL, RECTANGLE, CIRCLE, LINE }

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
            paint.setColor(0x40EAB308);
            paint.setStrokeWidth(currentStrokeWidth * 2.5f);
        } else {
            paint.setXfermode(null);
            paint.setColor(currentColor);
            paint.setStrokeWidth(currentStrokeWidth);
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
                currentPoints.add(new Point(touchX, touchY));
                break;

            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
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
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                undonePaths.clear();
                paths.add(new DrawPath(currentPath, new Paint(currentPaint), currentPoints));
                currentPath = new Path();
                break;

            default:
                return false;
        }
        invalidate();
        return true;
    }

    // VERİTABANINA KAYIT İÇİN JSON ÇIKTILAR
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

    // VERİTABANINDAN OKUNAN JSON'I EKRANA ÇİZER
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
        if (currentPaint != null) applyToolSettings(currentPaint);
    }

    public void setColor(int newColor) {
        this.currentColor = newColor;
        if (currentPaint != null) applyToolSettings(currentPaint);
    }

    public void setStrokeWidth(float width) {
        this.currentStrokeWidth = width;
        if (currentPaint != null) applyToolSettings(currentPaint);
    }

    public void undo() {
        if (!paths.isEmpty()) {
            DrawPath lastPath = paths.remove(paths.size() - 1);
            undonePaths.add(lastPath);
            invalidate();
        }
    }

    public void redo() {
        if (!undonePaths.isEmpty()) {
            DrawPath pathToRestore = undonePaths.remove(undonePaths.size() - 1);
            paths.add(pathToRestore);
            invalidate();
        }
    }

    public void clearCanvas() {
        paths.clear();
        undonePaths.clear();
        if (currentPath != null) currentPath.reset();
        invalidate();
    }
}