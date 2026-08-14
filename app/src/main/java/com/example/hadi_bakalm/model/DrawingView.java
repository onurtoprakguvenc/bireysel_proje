package com.example.hadi_bakalm.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL, RECTANGLE, SQUARE, CIRCLE, LINE, TEXT }

    public interface OnDrawingChangeListener {
        void onDrawingChanged(String jsonContent);
    }

    private OnDrawingChangeListener onDrawingChangeListener;

    public void setOnDrawingChangeListener(OnDrawingChangeListener listener) {
        this.onDrawingChangeListener = listener;
    }

    // --- TABLO VERİ MODELLERİ ---
    public static class TableCell {
        public int row, col;
        public String text;
        public TableCell(int row, int col, String text) {
            this.row = row;
            this.col = col;
            this.text = text;
        }
    }

    public static class TableObject {
        public float startX, startY;
        public float cellWidth = 160f;
        public float cellHeight = 90f;
        public int rows, cols;
        public List<TableCell> cells = new ArrayList<>();

        public TableObject(float startX, float startY, int rows, int cols) {
            this.startX = startX;
            this.startY = startY;
            this.rows = rows;
            this.cols = cols;
        }
    }

    public static class TableCellClickResult {
        public TableObject table;
        public int row, col;
        public TableCellClickResult(TableObject table, int row, int col) {
            this.table = table;
            this.row = row;
            this.col = col;
        }
    }

    // --- GÖRSEL NESNESİ MODELİ ---
    public static class ImageObject {
        public float x, y;
        public float width, height;
        public Bitmap bitmap;
        public String imageUriStr;

        public ImageObject(float x, float y, float width, float height, Bitmap bitmap, String imageUriStr) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.bitmap = bitmap;
            this.imageUriStr = imageUriStr;
        }
    }

    // --- SERBEST METİN NESNESİ MODELİ ---
    public static class TextObject {
        public float x, y;
        public String text;
        public int color;
        public float textSize;

        public TextObject(float x, float y, String text, int color, float textSize) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.textSize = textSize;
        }
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
    private final List<TableObject> tables = new ArrayList<>();
    private final List<ImageObject> images = new ArrayList<>();
    private final List<TextObject> textObjects = new ArrayList<>();

    private Path currentPath;
    private Paint currentPaint;
    private Paint textPaint;
    private Paint freeTextPaint;
    private List<Point> currentPoints;

    private int currentColor = 0xFF09090B;
    private float currentStrokeWidth = 8f;
    private ToolMode currentTool = ToolMode.PEN;

    private float startX, startY;
    private float offsetY = 0f;
    private float lastTouchY;
    private float scaleFactor = 1.0f;
    private ScaleGestureDetector scaleGestureDetector;

    // --- SİLGİ İMLECİ VE PİKSEL KAZIMA DEĞİŞKENLERİ ---
    private float eraserTouchX = -1f;
    private float eraserTouchY = -1f;
    private boolean isErasing = false;
    private Paint eraserCirclePaint;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initNewStroke();
        initTextPaint();
        initEraserPaint();

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
                invalidate();
                return true;
            }
        });
    }

    private void initTextPaint() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(0xFF0F172A);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        freeTextPaint = new Paint();
        freeTextPaint.setAntiAlias(true);
        freeTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void initEraserPaint() {
        eraserCirclePaint = new Paint();
        eraserCirclePaint.setAntiAlias(true);
        eraserCirclePaint.setStyle(Paint.Style.STROKE);
        eraserCirclePaint.setColor(0x8894A3B8);
        eraserCirclePaint.setStrokeWidth(3f);
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
            paint.setColor(0x00000000);
            paint.setStrokeWidth(currentStrokeWidth * 3f);
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
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(0, offsetY);

        // 1. Görselleri Çiz
        drawImages(canvas);

        // 2. Serbest Metinleri Çiz
        drawFreeTexts(canvas);

        // 3. Geçmiş Çizimleri ve Silgi Kazımalarını Çiz
        for (DrawPath dp : paths) {
            canvas.drawPath(dp.path, dp.paint);
        }

        // Anlık çizilen çizgi veya silgi izi
        if (currentPath != null && currentPaint != null && currentTool != ToolMode.SCROLL && currentTool != ToolMode.TEXT) {
            canvas.drawPath(currentPath, currentPaint);
        }

        // 4. Vektörel Tabloları Çiz
        drawTables(canvas);

        // 5. Silgi Görsel Gösterge Halkasını Çiz
        if (currentTool == ToolMode.ERASER && isErasing && eraserTouchX >= 0 && eraserTouchY >= 0) {
            if (eraserCirclePaint == null) initEraserPaint();
            float eraserRadius = (currentStrokeWidth * 3f) / 2f;
            canvas.drawCircle(eraserTouchX, eraserTouchY, eraserRadius, eraserCirclePaint);
        }

        canvas.restore();
    }

    private void drawImages(Canvas canvas) {
        for (ImageObject img : images) {
            if (img.bitmap != null && !img.bitmap.isRecycled()) {
                RectF dst = new RectF(img.x, img.y, img.x + img.width, img.y + img.height);
                canvas.drawBitmap(img.bitmap, null, dst, null);
            }
        }
    }

    private void drawFreeTexts(Canvas canvas) {
        if (freeTextPaint == null) initTextPaint();
        for (TextObject t : textObjects) {
            if (t.text != null && !t.text.isEmpty()) {
                freeTextPaint.setColor(t.color);
                freeTextPaint.setTextSize(t.textSize > 0 ? t.textSize : 36f);
                canvas.drawText(t.text, t.x, t.y, freeTextPaint);
            }
        }
    }

    private void drawTables(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(0xFF334155);
        gridPaint.setStrokeWidth(4f);

        for (TableObject table : tables) {
            float totalWidth = table.cols * table.cellWidth;
            float totalHeight = table.rows * table.cellHeight;

            for (int i = 0; i <= table.rows; i++) {
                float y = table.startY + (i * table.cellHeight);
                canvas.drawLine(table.startX, y, table.startX + totalWidth, y, gridPaint);
            }
            for (int j = 0; j <= table.cols; j++) {
                float x = table.startX + (j * table.cellWidth);
                canvas.drawLine(x, table.startY, x, table.startY + totalHeight, gridPaint);
            }

            if (textPaint == null) initTextPaint();
            for (TableCell cell : table.cells) {
                if (cell.text != null && !cell.text.isEmpty()) {
                    float cellCenterX = table.startX + (cell.col * table.cellWidth) + (table.cellWidth / 2f);
                    float cellCenterY = table.startY + (cell.row * table.cellHeight) + (table.cellHeight / 2f) + 10f;
                    canvas.drawText(cell.text, cellCenterX, cellCenterY, textPaint);
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        int pointerCount = event.getPointerCount();

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

        // TEXT modunda dokunma çizim üretmesin
        if (currentTool == ToolMode.TEXT) {
            return false;
        }

        float rawX = event.getX();
        float rawY = event.getY();

        float touchX = rawX / scaleFactor;
        float touchY = (rawY / scaleFactor) - offsetY;

        // Silgi konumunu güncelle
        if (currentTool == ToolMode.ERASER) {
            eraserTouchX = touchX;
            eraserTouchY = touchY;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = touchX;
                startY = touchY;

                if (currentTool == ToolMode.ERASER) {
                    isErasing = true;
                }

                initNewStroke();
                currentPath.moveTo(touchX, touchY);
                currentPoints.add(new Point(touchX, touchY));
                break;

            case MotionEvent.ACTION_MOVE:
                float maxCanvasWidth = getWidth() / scaleFactor;
                float clampedTouchX = Math.max(0f, Math.min(touchX, maxCanvasWidth));
                float clampedTouchY = touchY;

                if (currentTool == ToolMode.ERASER) {
                    // Silgi sadece değdiği yeri kazır, tüm şekli silmez
                    currentPath.lineTo(touchX, touchY);
                    currentPoints.add(new Point(touchX, touchY));

                } else if (currentTool == ToolMode.RECTANGLE) {
                    currentPath.reset();
                    float left = Math.min(startX, clampedTouchX);
                    float top = Math.min(startY, clampedTouchY);
                    float right = Math.max(startX, clampedTouchX);
                    float bottom = Math.max(startY, clampedTouchY);
                    currentPath.addRect(left, top, right, bottom, Path.Direction.CW);

                } else if (currentTool == ToolMode.SQUARE) {
                    currentPath.reset();
                    float dx = clampedTouchX - startX;
                    float dy = clampedTouchY - startY;
                    float side = Math.max(Math.abs(dx), Math.abs(dy));

                    if (dx >= 0 && (startX + side) > maxCanvasWidth) {
                        side = maxCanvasWidth - startX;
                    } else if (dx < 0 && (startX - side) < 0) {
                        side = startX;
                    }

                    float left = (dx < 0) ? startX - side : startX;
                    float top = (dy < 0) ? startY - side : startY;
                    float right = left + side;
                    float bottom = top + side;

                    currentPath.addRect(left, top, right, bottom, Path.Direction.CW);

                } else if (currentTool == ToolMode.CIRCLE) {
                    currentPath.reset();
                    float left = Math.min(startX, clampedTouchX);
                    float top = Math.min(startY, clampedTouchY);
                    float right = Math.max(startX, clampedTouchX);
                    float bottom = Math.max(startY, clampedTouchY);
                    currentPath.addOval(left, top, right, bottom, Path.Direction.CW);

                } else if (currentTool == ToolMode.LINE) {
                    currentPath.reset();
                    currentPath.moveTo(startX, startY);
                    currentPath.lineTo(clampedTouchX, clampedTouchY);

                } else {
                    currentPath.lineTo(touchX, touchY);
                    currentPoints.add(new Point(touchX, touchY));
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (currentTool == ToolMode.ERASER) {
                    isErasing = false;
                    eraserTouchX = -1f;
                    eraserTouchY = -1f;
                }

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

    public void addTextToCanvas(float x, float y, String text, int color) {
        if (text == null || text.trim().isEmpty()) return;
        textObjects.add(new TextObject(x, y, text, color, 36f));
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public void addImageToCanvas(Bitmap bitmap, String uriStr) {
        if (bitmap == null) return;

        float startX = 80f;
        float startY = -offsetY + 150f;

        float targetWidth = 400f;
        float aspectRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        float targetHeight = targetWidth * aspectRatio;

        ImageObject img = new ImageObject(startX, startY, targetWidth, targetHeight, bitmap, uriStr);
        images.add(img);
        invalidate();

        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public TableCellClickResult checkTableCellClick(float touchX, float touchY) {
        for (TableObject table : tables) {
            float totalWidth = table.cols * table.cellWidth;
            float totalHeight = table.rows * table.cellHeight;

            if (touchX >= table.startX && touchX <= table.startX + totalWidth &&
                    touchY >= table.startY && touchY <= table.startY + totalHeight) {

                int col = (int) ((touchX - table.startX) / table.cellWidth);
                int row = (int) ((touchY - table.startY) / table.cellHeight);

                return new TableCellClickResult(table, row, col);
            }
        }
        return null;
    }

    public void updateTableCellText(TableObject table, int row, int col, String newText) {
        for (TableCell cell : table.cells) {
            if (cell.row == row && cell.col == col) {
                cell.text = newText;
                invalidate();
                if (onDrawingChangeListener != null) {
                    onDrawingChangeListener.onDrawingChanged(getDrawingJson());
                }
                return;
            }
        }
        table.cells.add(new TableCell(row, col, newText));
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public void addTableToCanvas(int rows, int cols) {
        float startX = 80f;
        float startY = -offsetY + 150f;
        TableObject newTable = new TableObject(startX, startY, rows, cols);
        tables.add(newTable);
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public String getDrawingJson() {
        try {
            JSONObject mainObj = new JSONObject();

            // Çizimler ve Silgi Kazımaları
            JSONArray pathsArray = new JSONArray();
            for (DrawPath dp : paths) {
                JSONObject pathObj = new JSONObject();
                pathObj.put("color", dp.paint.getColor());
                pathObj.put("strokeWidth", dp.paint.getStrokeWidth());
                pathObj.put("isEraser", dp.paint.getXfermode() != null);

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
            mainObj.put("paths", pathsArray);

            // Tablolar
            JSONArray tablesArray = new JSONArray();
            for (TableObject table : tables) {
                JSONObject tableObj = new JSONObject();
                tableObj.put("startX", table.startX);
                tableObj.put("startY", table.startY);
                tableObj.put("rows", table.rows);
                tableObj.put("cols", table.cols);

                JSONArray cellsArray = new JSONArray();
                for (TableCell cell : table.cells) {
                    JSONObject cellObj = new JSONObject();
                    cellObj.put("row", cell.row);
                    cellObj.put("col", cell.col);
                    cellObj.put("text", cell.text);
                    cellsArray.put(cellObj);
                }
                tableObj.put("cells", cellsArray);
                tablesArray.put(tableObj);
            }
            mainObj.put("tables", tablesArray);

            // Görseller
            JSONArray imagesArray = new JSONArray();
            for (ImageObject img : images) {
                JSONObject imgObj = new JSONObject();
                imgObj.put("x", img.x);
                imgObj.put("y", img.y);
                imgObj.put("width", img.width);
                imgObj.put("height", img.height);
                imgObj.put("uri", img.imageUriStr);
                imagesArray.put(imgObj);
            }
            mainObj.put("images", imagesArray);

            // Serbest Metinler
            JSONArray textsArray = new JSONArray();
            for (TextObject t : textObjects) {
                JSONObject tObj = new JSONObject();
                tObj.put("x", t.x);
                tObj.put("y", t.y);
                tObj.put("text", t.text);
                tObj.put("color", t.color);
                tObj.put("textSize", t.textSize);
                textsArray.put(tObj);
            }
            mainObj.put("texts", textsArray);

            return mainObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void loadDrawingFromJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) return;
        try {
            paths.clear();
            tables.clear();
            images.clear();
            textObjects.clear();

            if (!jsonStr.startsWith("{")) {
                JSONArray pathsArray = new JSONArray(jsonStr);
                parsePathsJson(pathsArray);
                return;
            }

            JSONObject mainObj = new JSONObject(jsonStr);
            if (mainObj.has("paths")) {
                parsePathsJson(mainObj.getJSONArray("paths"));
            }

            if (mainObj.has("tables")) {
                JSONArray tablesArray = mainObj.getJSONArray("tables");
                for (int i = 0; i < tablesArray.length(); i++) {
                    JSONObject tableObj = tablesArray.getJSONObject(i);
                    float startX = (float) tableObj.getDouble("startX");
                    float startY = (float) tableObj.getDouble("startY");
                    int rows = tableObj.getInt("rows");
                    int cols = tableObj.getInt("cols");

                    TableObject table = new TableObject(startX, startY, rows, cols);

                    if (tableObj.has("cells")) {
                        JSONArray cellsArray = tableObj.getJSONArray("cells");
                        for (int j = 0; j < cellsArray.length(); j++) {
                            JSONObject cellObj = cellsArray.getJSONObject(j);
                            int row = cellObj.getInt("row");
                            int col = cellObj.getInt("col");
                            String text = cellObj.getString("text");
                            table.cells.add(new TableCell(row, col, text));
                        }
                    }
                    tables.add(table);
                }
            }

            if (mainObj.has("images")) {
                JSONArray imagesArray = mainObj.getJSONArray("images");
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject imgObj = imagesArray.getJSONObject(i);
                    float x = (float) imgObj.getDouble("x");
                    float y = (float) imgObj.getDouble("y");
                    float w = (float) imgObj.getDouble("width");
                    float h = (float) imgObj.getDouble("height");
                    String uriStr = imgObj.getString("uri");

                    try {
                        android.net.Uri uri = android.net.Uri.parse(uriStr);
                        Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                        images.add(new ImageObject(x, y, w, h, bitmap, uriStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (mainObj.has("texts")) {
                JSONArray textsArray = mainObj.getJSONArray("texts");
                for (int i = 0; i < textsArray.length(); i++) {
                    JSONObject tObj = textsArray.getJSONObject(i);
                    float x = (float) tObj.getDouble("x");
                    float y = (float) tObj.getDouble("y");
                    String text = tObj.getString("text");
                    int color = tObj.getInt("color");
                    float textSize = (float) tObj.getDouble("textSize");
                    textObjects.add(new TextObject(x, y, text, color, textSize));
                }
            }

            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsePathsJson(JSONArray pathsArray) throws Exception {
        for (int i = 0; i < pathsArray.length(); i++) {
            JSONObject pathObj = pathsArray.getJSONObject(i);
            int color = pathObj.getInt("color");
            float strokeWidth = (float) pathObj.getDouble("strokeWidth");
            boolean isEraser = pathObj.optBoolean("isEraser", false);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(strokeWidth);

            if (isEraser) {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                paint.setColor(0x00000000);
            } else {
                paint.setXfermode(null);
                paint.setColor(color);
            }

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
    }

    public float getScaleFactor() { return scaleFactor; }
    public float getOffsetY() { return offsetY; }

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
        tables.clear();
        images.clear();
        textObjects.clear();
        if (currentPath != null) currentPath.reset();
        offsetY = 0f;
        scaleFactor = 1.0f;
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public TextObject checkTextClick(float touchX, float touchY) {
        if (freeTextPaint == null) initTextPaint();

        for (int i = textObjects.size() - 1; i >= 0; i--) {
            TextObject t = textObjects.get(i);
            float textWidth = freeTextPaint.measureText(t.text);
            float textHeight = t.textSize > 0 ? t.textSize : 36f;

            if (touchX >= t.x - 20f && touchX <= t.x + textWidth + 20f &&
                    touchY >= t.y - textHeight - 20f && touchY <= t.y + 20f) {
                return t;
            }
        }
        return null;
    }

    public void updateTextObject(TextObject textObj, String newText) {
        if (textObj == null) return;
        textObj.text = newText;
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    public void removeTextObject(TextObject textObj) {
        if (textObj == null) return;
        textObjects.remove(textObj);
        invalidate();
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }
}