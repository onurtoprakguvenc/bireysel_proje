package com.example.hadi_bakalm.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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

    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL, SELECT, RECTANGLE, SQUARE, CIRCLE, LINE, TEXT }

    public interface OnDrawingChangeListener {
        void onDrawingChanged(String jsonContent);
    }

    private OnDrawingChangeListener onDrawingChangeListener;
    public void setOnDrawingChangeListener(OnDrawingChangeListener listener) {
        this.onDrawingChangeListener = listener;
    }

    // =========================================================================
    // 1. SAHNE NESNELERİ (SCENE GRAPH ITEMS)
    // =========================================================================

    public static class Point {
        public float x, y;
        public Point(float x, float y) { this.x = x; this.y = y; }
    }

    public static class StrokeItem {
        public Path path;
        public Paint paint;
        public List<Point> points;
        public int color;
        public float strokeWidth;
        public boolean isEraser;

        public StrokeItem(Path path, Paint paint, List<Point> points, int color, float strokeWidth, boolean isEraser) {
            this.path = path;
            this.paint = paint;
            this.points = points;
            this.color = color;
            this.strokeWidth = strokeWidth;
            this.isEraser = isEraser;
        }
    }

    // Bağımsız ve Seçilebilir Şekil Modeli
    public static class ShapeItem {
        public ToolMode shapeType;
        public float startX, startY, endX, endY;
        public int color;
        public float strokeWidth;

        public ShapeItem(ToolMode shapeType, float startX, float startY, float endX, float endY, int color, float strokeWidth) {
            this.shapeType = shapeType;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }

        public RectF getBounds() {
            float minX = Math.min(startX, endX);
            float maxX = Math.max(startX, endX);
            float minY = Math.min(startY, endY);
            float maxY = Math.max(startY, endY);
            float pad = Math.max(strokeWidth / 2f, 15f);
            return new RectF(minX - pad, minY - pad, maxX + pad, maxY + pad);
        }

        public void offset(float dx, float dy) {
            startX += dx;
            startY += dy;
            endX += dx;
            endY += dy;
        }
    }

    public static class ImageItem {
        public float x, y, width, height;
        public Bitmap bitmap;
        public String uriStr;

        public ImageItem(float x, float y, float width, float height, Bitmap bitmap, String uriStr) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.bitmap = bitmap;
            this.uriStr = uriStr;
        }

        public RectF getBounds() {
            return new RectF(x, y, x + width, y + height);
        }

        public RectF getResizeHandle() {
            return new RectF(x + width - 40f, y + height - 40f, x + width + 20f, y + height + 20f);
        }
    }

    public static class TextItem {
        public float x, y, textSize;
        public String text;
        public int color;

        public TextItem(float x, float y, String text, int color, float textSize) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
            this.textSize = textSize;
        }
    }

    public static class TableCell {
        public int row, col;
        public String text;
        public TableCell(int row, int col, String text) {
            this.row = row;
            this.col = col;
            this.text = text;
        }
    }

    public static class TableItem {
        public float startX, startY;
        public float cellWidth = 160f, cellHeight = 90f;
        public int rows, cols;
        public List<TableCell> cells = new ArrayList<>();

        public TableItem(float startX, float startY, int rows, int cols) {
            this.startX = startX;
            this.startY = startY;
            this.rows = rows;
            this.cols = cols;
        }
    }

    public static class TableCellClickResult {
        public TableItem table;
        public int row, col;
        public TableCellClickResult(TableItem table, int row, int col) {
            this.table = table;
            this.row = row;
            this.col = col;
        }
    }

    // =========================================================================
    // 2. TUVAL VERİ HAVUZU (CANVAS REPOSITORY)
    // =========================================================================

    private final List<StrokeItem> strokes = new ArrayList<>();
    private final List<StrokeItem> undoneStrokes = new ArrayList<>();
    private final List<ShapeItem> shapes = new ArrayList<>();
    private final List<ShapeItem> undoneShapes = new ArrayList<>();
    private final List<ImageItem> images = new ArrayList<>();
    private final List<TextItem> texts = new ArrayList<>();
    private final List<TableItem> tables = new ArrayList<>();

    private Object selectedItem = null; // ShapeItem, ImageItem veya TextItem
    private final RectF menuDeleteBounds = new RectF();
    private final RectF menuSizeUpBounds = new RectF();
    private final RectF menuSizeDownBounds = new RectF();

    private int currentColor = 0xFF09090B;
    private float currentStrokeWidth = 8f;
    private ToolMode currentToolMode = ToolMode.PEN;

    private float scaleFactor = 1.0f;
    private float offsetY = 0f;
    private float lastTouchY;
    private ScaleGestureDetector scaleGestureDetector;

    private Paint textPaint;
    private Paint freeTextPaint;
    private Paint tablePaint;
    private Paint selectionBoxPaint;
    private Paint handlePaint;
    private Paint menuBgPaint;
    private Paint menuTextPaint;
    private Paint eraserCursorPaint;
    private Paint shapeRenderPaint;

    // =========================================================================
    // 3. BAŞLANGIÇ YAPILANDIRMASI
    // =========================================================================

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCanvas();
    }

    private void initCanvas() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF0F172A);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        freeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        freeTextPaint.setTextAlign(Paint.Align.LEFT);

        tablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tablePaint.setStyle(Paint.Style.STROKE);
        tablePaint.setColor(0xFF334155);
        tablePaint.setStrokeWidth(4f);

        shapeRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shapeRenderPaint.setStyle(Paint.Style.STROKE);
        shapeRenderPaint.setStrokeJoin(Paint.Join.ROUND);
        shapeRenderPaint.setStrokeCap(Paint.Cap.ROUND);

        selectionBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectionBoxPaint.setStyle(Paint.Style.STROKE);
        selectionBoxPaint.setColor(0xFF0284C7);
        selectionBoxPaint.setStrokeWidth(3f);
        selectionBoxPaint.setPathEffect(new DashPathEffect(new float[]{12, 8}, 0));

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(0xFF0284C7);

        menuBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        menuBgPaint.setColor(0xFF1E293B);
        menuBgPaint.setStyle(Paint.Style.FILL);
        menuBgPaint.setShadowLayer(6f, 0, 3f, 0x44000000);

        menuTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        menuTextPaint.setColor(Color.WHITE);
        menuTextPaint.setTextSize(26f);
        menuTextPaint.setTextAlign(Paint.Align.CENTER);

        eraserCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eraserCursorPaint.setStyle(Paint.Style.STROKE);
        eraserCursorPaint.setColor(0x8894A3B8);
        eraserCursorPaint.setStrokeWidth(3f);

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

    // =========================================================================
    // 4. RENDER DÖNGÜSÜ (ON DRAW)
    // =========================================================================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(0, offsetY);

        // 1. Serbest Çizimler
        for (StrokeItem stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

        // 2. Sabit Vektör Şekilleri
        renderShapes(canvas);

        // 3. Anlık Çizilen Çizgi / Şekil Önizlemesi
        if (activePath != null && activePaint != null && currentToolMode != ToolMode.SCROLL && currentToolMode != ToolMode.SELECT && currentToolMode != ToolMode.TEXT) {
            canvas.drawPath(activePath, activePaint);
        }

        // 4. Tablolar
        renderTables(canvas);

        // 5. Görseller
        renderImages(canvas);

        // 6. Serbest Metinler
        renderTexts(canvas);

        // 7. Seçim Çerçevesi ve Yüzen Hızlı Menü (En Üst Katman)
        if (currentToolMode == ToolMode.SELECT && selectedItem != null) {
            renderSelectionAndFloatingMenu(canvas);
        }

        // 8. Silgi İz Göstergesi
        if (currentToolMode == ToolMode.ERASER && isErasing && eraserX >= 0 && eraserY >= 0) {
            float radius = (currentStrokeWidth * 3f) / 2f;
            canvas.drawCircle(eraserX, eraserY, radius, eraserCursorPaint);
        }

        canvas.restore();
    }

    private void renderShapes(Canvas canvas) {
        for (ShapeItem s : shapes) {
            shapeRenderPaint.setColor(s.color);
            shapeRenderPaint.setStrokeWidth(s.strokeWidth);

            if (s.shapeType == ToolMode.RECTANGLE) {
                canvas.drawRect(Math.min(s.startX, s.endX), Math.min(s.startY, s.endY),
                        Math.max(s.startX, s.endX), Math.max(s.startY, s.endY), shapeRenderPaint);
            } else if (s.shapeType == ToolMode.SQUARE) {
                float dx = s.endX - s.startX;
                float dy = s.endY - s.startY;
                float side = Math.max(Math.abs(dx), Math.abs(dy));
                float left = (dx < 0) ? s.startX - side : s.startX;
                float top = (dy < 0) ? s.startY - side : s.startY;
                canvas.drawRect(left, top, left + side, top + side, shapeRenderPaint);
            } else if (s.shapeType == ToolMode.CIRCLE) {
                canvas.drawOval(new RectF(Math.min(s.startX, s.endX), Math.min(s.startY, s.endY),
                        Math.max(s.startX, s.endX), Math.max(s.startY, s.endY)), shapeRenderPaint);
            } else if (s.shapeType == ToolMode.LINE) {
                canvas.drawLine(s.startX, s.startY, s.endX, s.endY, shapeRenderPaint);
            }
        }
    }

    private void renderTables(Canvas canvas) {
        for (TableItem table : tables) {
            float totalW = table.cols * table.cellWidth;
            float totalH = table.rows * table.cellHeight;

            for (int i = 0; i <= table.rows; i++) {
                float y = table.startY + (i * table.cellHeight);
                canvas.drawLine(table.startX, y, table.startX + totalW, y, tablePaint);
            }
            for (int j = 0; j <= table.cols; j++) {
                float x = table.startX + (j * table.cellWidth);
                canvas.drawLine(x, table.startY, x, table.startY + totalH, tablePaint);
            }

            for (TableCell cell : table.cells) {
                if (cell.text != null && !cell.text.isEmpty()) {
                    float cx = table.startX + (cell.col * table.cellWidth) + (table.cellWidth / 2f);
                    float cy = table.startY + (cell.row * table.cellHeight) + (table.cellHeight / 2f) + 10f;
                    canvas.drawText(cell.text, cx, cy, textPaint);
                }
            }
        }
    }

    private void renderImages(Canvas canvas) {
        for (ImageItem img : images) {
            if (img.bitmap != null && !img.bitmap.isRecycled()) {
                canvas.drawBitmap(img.bitmap, null, img.getBounds(), null);
            }
        }
    }

    private void renderTexts(Canvas canvas) {
        for (TextItem t : texts) {
            if (t.text != null && !t.text.isEmpty()) {
                freeTextPaint.setColor(t.color);
                freeTextPaint.setTextSize(t.textSize > 0 ? t.textSize : 36f);
                canvas.drawText(t.text, t.x, t.y, freeTextPaint);
            }
        }
    }

    private void renderSelectionAndFloatingMenu(Canvas canvas) {
        RectF bounds = new RectF();
        boolean isText = (selectedItem instanceof TextItem);

        if (selectedItem instanceof ImageItem) {
            bounds = ((ImageItem) selectedItem).getBounds();
            canvas.drawCircle(bounds.right, bounds.bottom, 20f, handlePaint);
        } else if (selectedItem instanceof TextItem) {
            TextItem t = (TextItem) selectedItem;
            float textW = freeTextPaint.measureText(t.text);
            float textH = t.textSize > 0 ? t.textSize : 36f;
            bounds.set(t.x - 8f, t.y - textH - 8f, t.x + textW + 8f, t.y + 12f);
        } else if (selectedItem instanceof ShapeItem) {
            bounds = ((ShapeItem) selectedItem).getBounds();
        }

        canvas.drawRect(bounds, selectionBoxPaint);

        float menuW = isText ? 240f : 120f;
        float menuH = 64f;
        float menuX = bounds.left + (bounds.width() - menuW) / 2f;
        float menuY = bounds.top - menuH - 20f;

        if (menuY < -offsetY + 10f) {
            menuY = bounds.bottom + 20f;
        }

        RectF menuRect = new RectF(menuX, menuY, menuX + menuW, menuY + menuH);
        canvas.drawRoundRect(menuRect, 32f, 32f, menuBgPaint);

        if (isText) {
            menuSizeDownBounds.set(menuX, menuY, menuX + 80f, menuY + menuH);
            canvas.drawText("A-", menuX + 40f, menuY + 40f, menuTextPaint);

            menuSizeUpBounds.set(menuX + 80f, menuY, menuX + 160f, menuY + menuH);
            canvas.drawText("A+", menuX + 120f, menuY + 40f, menuTextPaint);

            menuDeleteBounds.set(menuX + 160f, menuY, menuX + 240f, menuY + menuH);
            canvas.drawText("Sil", menuX + 200f, menuY + 40f, menuTextPaint);
        } else {
            menuSizeDownBounds.setEmpty();
            menuSizeUpBounds.setEmpty();
            menuDeleteBounds.set(menuX, menuY, menuX + menuW, menuY + menuH);
            canvas.drawText("Sil", menuX + (menuW / 2f), menuY + 40f, menuTextPaint);
        }
    }

    // =========================================================================
    // 5. DETERMINISTIK DOKUNMA YÖNETİMİ
    // =========================================================================

    private Path activePath;
    private Paint activePaint;
    private List<Point> activePoints;
    private float touchStartX, touchStartY;
    private float lastMoveX, lastMoveY;
    private boolean isErasing = false;
    private float eraserX = -1f, eraserY = -1f;

    private boolean isDraggingObject = false;
    private boolean isResizingImage = false;
    private float dragOffsetX, dragOffsetY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        if (event.getPointerCount() > 1 || currentToolMode == ToolMode.SCROLL) {
            handleScrollTouch(event);
            return true;
        }

        if (currentToolMode == ToolMode.TEXT) {
            return false;
        }

        float touchX = event.getX() / scaleFactor;
        float touchY = (event.getY() / scaleFactor) - offsetY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = touchX;
                touchStartY = touchY;
                lastMoveX = touchX;
                lastMoveY = touchY;

                if (currentToolMode == ToolMode.SELECT) {
                    handleSelectDown(touchX, touchY);
                    return true;
                }

                selectedItem = null;
                startStroke(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                lastMoveX = touchX;
                lastMoveY = touchY;

                if (currentToolMode == ToolMode.SELECT) {
                    handleSelectMove(touchX, touchY);
                    return true;
                }

                continueStroke(touchX, touchY);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (currentToolMode == ToolMode.SELECT) {
                    handleSelectUp();
                    return true;
                }

                finishStroke();
                break;
        }

        invalidate();
        return true;
    }

    private void handleSelectDown(float x, float y) {
        // 1. Menü Tıklamaları
        if (selectedItem != null) {
            if (menuDeleteBounds.contains(x, y)) {
                if (selectedItem instanceof ImageItem) images.remove((ImageItem) selectedItem);
                else if (selectedItem instanceof TextItem) texts.remove((TextItem) selectedItem);
                else if (selectedItem instanceof ShapeItem) shapes.remove((ShapeItem) selectedItem);
                selectedItem = null;
                notifyChange();
                invalidate();
                return;
            }

            if (selectedItem instanceof TextItem) {
                TextItem t = (TextItem) selectedItem;
                if (menuSizeUpBounds.contains(x, y)) {
                    t.textSize += 6f;
                    notifyChange();
                    invalidate();
                    return;
                } else if (menuSizeDownBounds.contains(x, y)) {
                    t.textSize = Math.max(16f, t.textSize - 6f);
                    notifyChange();
                    invalidate();
                    return;
                }
            }
        }

        // 2. Görsel Boyutlandırma Tutamacı
        if (selectedItem instanceof ImageItem) {
            ImageItem img = (ImageItem) selectedItem;
            if (img.getResizeHandle().contains(x, y)) {
                isResizingImage = true;
                return;
            }
        }

        // 3. Şekil Dokunma Kontrolü
        for (int i = shapes.size() - 1; i >= 0; i--) {
            ShapeItem s = shapes.get(i);
            if (s.getBounds().contains(x, y)) {
                selectedItem = s;
                isDraggingObject = true;
                dragOffsetX = x;
                dragOffsetY = y;
                invalidate();
                return;
            }
        }

        // 4. Görsel Dokunma Kontrolü
        for (int i = images.size() - 1; i >= 0; i--) {
            ImageItem img = images.get(i);
            if (img.getBounds().contains(x, y)) {
                selectedItem = img;
                isDraggingObject = true;
                dragOffsetX = x - img.x;
                dragOffsetY = y - img.y;
                invalidate();
                return;
            }
        }

        // 5. Metin Dokunma Kontrolü
        for (int i = texts.size() - 1; i >= 0; i--) {
            TextItem t = texts.get(i);
            float w = freeTextPaint.measureText(t.text);
            float h = t.textSize > 0 ? t.textSize : 36f;
            if (x >= t.x - 10f && x <= t.x + w + 10f && y >= t.y - h - 10f && y <= t.y + 10f) {
                selectedItem = t;
                isDraggingObject = true;
                dragOffsetX = x - t.x;
                dragOffsetY = y - t.y;
                invalidate();
                return;
            }
        }

        selectedItem = null;
        invalidate();
    }

    private void handleSelectMove(float x, float y) {
        if (isResizingImage && selectedItem instanceof ImageItem) {
            ImageItem img = (ImageItem) selectedItem;
            float newW = Math.max(80f, x - img.x);
            float ratio = (float) img.bitmap.getHeight() / (float) img.bitmap.getWidth();
            img.width = newW;
            img.height = newW * ratio;
            invalidate();
        } else if (isDraggingObject && selectedItem != null) {
            if (selectedItem instanceof ShapeItem) {
                ShapeItem s = (ShapeItem) selectedItem;
                float dx = x - dragOffsetX;
                float dy = y - dragOffsetY;
                s.offset(dx, dy);
                dragOffsetX = x;
                dragOffsetY = y;
            } else if (selectedItem instanceof ImageItem) {
                ImageItem img = (ImageItem) selectedItem;
                img.x = x - dragOffsetX;
                img.y = y - dragOffsetY;
            } else if (selectedItem instanceof TextItem) {
                TextItem t = (TextItem) selectedItem;
                t.x = x - dragOffsetX;
                t.y = y - dragOffsetY;
            }
            invalidate();
        }
    }

    private void handleSelectUp() {
        if (isDraggingObject || isResizingImage) {
            isDraggingObject = false;
            isResizingImage = false;
            notifyChange();
        }
    }

    private void handleScrollTouch(MotionEvent event) {
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
                    if (offsetY > 0) offsetY = 0;
                    lastTouchY = newY;
                    invalidate();
                }
                break;
        }
    }

    private void startStroke(float x, float y) {
        activePath = new Path();
        activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activePaint.setStyle(Paint.Style.STROKE);
        activePaint.setStrokeJoin(Paint.Join.ROUND);
        activePaint.setStrokeCap(Paint.Cap.ROUND);
        activePoints = new ArrayList<>();

        if (currentToolMode == ToolMode.ERASER) {
            isErasing = true;
            eraserX = x;
            eraserY = y;
            activePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            activePaint.setColor(0x00000000);
            activePaint.setStrokeWidth(currentStrokeWidth * 3f);
        } else if (currentToolMode == ToolMode.HIGHLIGHTER) {
            int alphaColor = (currentColor & 0x00FFFFFF) | 0x66000000;
            if ((currentColor & 0x00FFFFFF) == 0x09090B || (currentColor & 0x00FFFFFF) == 0x000000) {
                alphaColor = 0x66EAB308;
            }
            activePaint.setColor(alphaColor);
            activePaint.setStrokeWidth(currentStrokeWidth * 3f);
        } else {
            activePaint.setColor(currentColor);
            activePaint.setStrokeWidth(currentStrokeWidth);
        }

        activePath.moveTo(x, y);
        activePoints.add(new Point(x, y));
    }

    private void continueStroke(float x, float y) {
        if (activePath == null) return;

        if (currentToolMode == ToolMode.ERASER) {
            eraserX = x;
            eraserY = y;
            activePath.lineTo(x, y);
            activePoints.add(new Point(x, y));
        } else if (currentToolMode == ToolMode.RECTANGLE) {
            activePath.reset();
            activePath.addRect(Math.min(touchStartX, x), Math.min(touchStartY, y), Math.max(touchStartX, x), Math.max(touchStartY, y), Path.Direction.CW);
        } else if (currentToolMode == ToolMode.SQUARE) {
            activePath.reset();
            float dx = x - touchStartX;
            float dy = y - touchStartY;
            float side = Math.max(Math.abs(dx), Math.abs(dy));
            float left = (dx < 0) ? touchStartX - side : touchStartX;
            float top = (dy < 0) ? touchStartY - side : touchStartY;
            activePath.addRect(left, top, left + side, top + side, Path.Direction.CW);
        } else if (currentToolMode == ToolMode.CIRCLE) {
            activePath.reset();
            activePath.addOval(Math.min(touchStartX, x), Math.min(touchStartY, y), Math.max(touchStartX, x), Math.max(touchStartY, y), Path.Direction.CW);
        } else if (currentToolMode == ToolMode.LINE) {
            activePath.reset();
            activePath.moveTo(touchStartX, touchStartY);
            activePath.lineTo(x, y);
        } else {
            activePath.lineTo(x, y);
            activePoints.add(new Point(x, y));
        }
    }

    private void finishStroke() {
        if (currentToolMode == ToolMode.RECTANGLE || currentToolMode == ToolMode.SQUARE ||
                currentToolMode == ToolMode.CIRCLE || currentToolMode == ToolMode.LINE) {
            shapes.add(new ShapeItem(currentToolMode, touchStartX, touchStartY, lastMoveX, lastMoveY, currentColor, currentStrokeWidth));
            undoneShapes.clear();
            activePath = null;
            activePaint = null;
            activePoints = null;
            notifyChange();
            return;
        }

        if (activePath == null) return;

        if (currentToolMode == ToolMode.ERASER) {
            isErasing = false;
            eraserX = -1f;
            eraserY = -1f;
        }

        undoneStrokes.clear();
        boolean isEraser = (currentToolMode == ToolMode.ERASER);
        strokes.add(new StrokeItem(activePath, new Paint(activePaint), activePoints, activePaint.getColor(), activePaint.getStrokeWidth(), isEraser));

        activePath = null;
        activePaint = null;
        activePoints = null;

        notifyChange();
    }

    private void notifyChange() {
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    // =========================================================================
    // 6. KAMUYA AÇIK YÖNETİM METOTLARI (PUBLIC API)
    // =========================================================================

    public void setToolMode(ToolMode mode) {
        this.currentToolMode = mode;
        this.selectedItem = null;
        this.activePath = null;
        invalidate();
    }

    public void setColor(int color) {
        this.currentColor = color;
    }

    public void setStrokeWidth(float width) {
        this.currentStrokeWidth = width;
    }

    public void undo() {
        if (!shapes.isEmpty()) {
            undoneShapes.add(shapes.remove(shapes.size() - 1));
            notifyChange();
            invalidate();
            return;
        }
        if (!strokes.isEmpty()) {
            undoneStrokes.add(strokes.remove(strokes.size() - 1));
            notifyChange();
            invalidate();
        }
    }

    public void redo() {
        if (!undoneShapes.isEmpty()) {
            shapes.add(undoneShapes.remove(undoneShapes.size() - 1));
            notifyChange();
            invalidate();
            return;
        }
        if (!undoneStrokes.isEmpty()) {
            strokes.add(undoneStrokes.remove(undoneStrokes.size() - 1));
            notifyChange();
            invalidate();
        }
    }

    public void clearCanvas() {
        strokes.clear();
        undoneStrokes.clear();
        shapes.clear();
        undoneShapes.clear();
        images.clear();
        texts.clear();
        tables.clear();
        selectedItem = null;
        activePath = null;
        offsetY = 0f;
        scaleFactor = 1.0f;
        notifyChange();
        invalidate();
    }

    public void addImageToCanvas(Bitmap bitmap, String uriStr) {
        if (bitmap == null) return;
        float startX = 80f;
        float startY = -offsetY + 150f;
        float targetWidth = 400f;
        float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        ImageItem item = new ImageItem(startX, startY, targetWidth, targetWidth * ratio, bitmap, uriStr);
        images.add(item);
        selectedItem = item;
        currentToolMode = ToolMode.SELECT;
        notifyChange();
        invalidate();
    }

    public void addTextToCanvas(float x, float y, String text, int color) {
        if (text == null || text.trim().isEmpty()) return;
        TextItem t = new TextItem(x, y, text, color, 36f);
        texts.add(t);
        selectedItem = t;
        notifyChange();
        invalidate();
    }

    public void updateTextObject(TextItem item, String newText) {
        if (item == null) return;
        item.text = newText;
        notifyChange();
        invalidate();
    }

    public void removeTextObject(TextItem item) {
        if (item == null) return;
        texts.remove(item);
        if (selectedItem == item) selectedItem = null;
        notifyChange();
        invalidate();
    }

    public TextItem checkTextClick(float touchX, float touchY) {
        for (int i = texts.size() - 1; i >= 0; i--) {
            TextItem t = texts.get(i);
            float w = freeTextPaint.measureText(t.text);
            float h = t.textSize > 0 ? t.textSize : 36f;
            if (touchX >= t.x - 10f && touchX <= t.x + w + 10f && touchY >= t.y - h - 10f && touchY <= t.y + 10f) {
                return t;
            }
        }
        return null;
    }

    public void addTableToCanvas(int rows, int cols) {
        float startX = 80f;
        float startY = -offsetY + 150f;
        TableItem table = new TableItem(startX, startY, rows, cols);
        tables.add(table);
        notifyChange();
        invalidate();
    }

    public TableCellClickResult checkTableCellClick(float touchX, float touchY) {
        for (TableItem table : tables) {
            float totalW = table.cols * table.cellWidth;
            float totalH = table.rows * table.cellHeight;
            if (touchX >= table.startX && touchX <= table.startX + totalW &&
                    touchY >= table.startY && touchY <= table.startY + totalH) {
                int col = (int) ((touchX - table.startX) / table.cellWidth);
                int row = (int) ((touchY - table.startY) / table.cellHeight);
                return new TableCellClickResult(table, row, col);
            }
        }
        return null;
    }

    public void updateTableCellText(TableItem table, int row, int col, String newText) {
        for (TableCell cell : table.cells) {
            if (cell.row == row && cell.col == col) {
                cell.text = newText;
                notifyChange();
                invalidate();
                return;
            }
        }
        table.cells.add(new TableCell(row, col, newText));
        notifyChange();
        invalidate();
    }

    public float getScaleFactor() { return scaleFactor; }
    public float getOffsetY() { return offsetY; }

    // =========================================================================
    // 7. JSON SERİLEŞTİRME (IMPORT / EXPORT)
    // =========================================================================

    public String getDrawingJson() {
        try {
            JSONObject mainObj = new JSONObject();

            JSONArray pathsArray = new JSONArray();
            for (StrokeItem stroke : strokes) {
                JSONObject pathObj = new JSONObject();
                pathObj.put("color", stroke.color);
                pathObj.put("strokeWidth", stroke.strokeWidth);
                pathObj.put("isEraser", stroke.isEraser);

                JSONArray pointsArray = new JSONArray();
                for (Point p : stroke.points) {
                    JSONObject pointObj = new JSONObject();
                    pointObj.put("x", p.x);
                    pointObj.put("y", p.y);
                    pointsArray.put(pointObj);
                }
                pathObj.put("points", pointsArray);
                pathsArray.put(pathObj);
            }
            mainObj.put("paths", pathsArray);

            JSONArray shapesArray = new JSONArray();
            for (ShapeItem s : shapes) {
                JSONObject shapeObj = new JSONObject();
                shapeObj.put("type", s.shapeType.name());
                shapeObj.put("startX", s.startX);
                shapeObj.put("startY", s.startY);
                shapeObj.put("endX", s.endX);
                shapeObj.put("endY", s.endY);
                shapeObj.put("color", s.color);
                shapeObj.put("strokeWidth", s.strokeWidth);
                shapesArray.put(shapeObj);
            }
            mainObj.put("shapes", shapesArray);

            JSONArray tablesArray = new JSONArray();
            for (TableItem table : tables) {
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

            JSONArray imagesArray = new JSONArray();
            for (ImageItem img : images) {
                JSONObject imgObj = new JSONObject();
                imgObj.put("x", img.x);
                imgObj.put("y", img.y);
                imgObj.put("width", img.width);
                imgObj.put("height", img.height);
                imgObj.put("uri", img.uriStr);
                imagesArray.put(imgObj);
            }
            mainObj.put("images", imagesArray);

            JSONArray textsArray = new JSONArray();
            for (TextItem t : texts) {
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
            strokes.clear();
            shapes.clear();
            tables.clear();
            images.clear();
            texts.clear();

            if (!jsonStr.startsWith("{")) {
                parseStrokes(new JSONArray(jsonStr));
                return;
            }

            JSONObject mainObj = new JSONObject(jsonStr);
            if (mainObj.has("paths")) parseStrokes(mainObj.getJSONArray("paths"));

            if (mainObj.has("shapes")) {
                JSONArray shapesArray = mainObj.getJSONArray("shapes");
                for (int i = 0; i < shapesArray.length(); i++) {
                    JSONObject sObj = shapesArray.getJSONObject(i);
                    ToolMode type = ToolMode.valueOf(sObj.getString("type"));
                    float sx = (float) sObj.getDouble("startX");
                    float sy = (float) sObj.getDouble("startY");
                    float ex = (float) sObj.getDouble("endX");
                    float ey = (float) sObj.getDouble("endY");
                    int color = sObj.getInt("color");
                    float width = (float) sObj.getDouble("strokeWidth");
                    shapes.add(new ShapeItem(type, sx, sy, ex, ey, color, width));
                }
            }

            if (mainObj.has("tables")) {
                JSONArray tablesArray = mainObj.getJSONArray("tables");
                for (int i = 0; i < tablesArray.length(); i++) {
                    JSONObject obj = tablesArray.getJSONObject(i);
                    TableItem table = new TableItem((float) obj.getDouble("startX"), (float) obj.getDouble("startY"), obj.getInt("rows"), obj.getInt("cols"));
                    if (obj.has("cells")) {
                        JSONArray cellsArray = obj.getJSONArray("cells");
                        for (int j = 0; j < cellsArray.length(); j++) {
                            JSONObject c = cellsArray.getJSONObject(j);
                            table.cells.add(new TableCell(c.getInt("row"), c.getInt("col"), c.getString("text")));
                        }
                    }
                    tables.add(table);
                }
            }

            if (mainObj.has("images")) {
                JSONArray imagesArray = mainObj.getJSONArray("images");
                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject obj = imagesArray.getJSONObject(i);
                    float x = (float) obj.getDouble("x");
                    float y = (float) obj.getDouble("y");
                    float w = (float) obj.getDouble("width");
                    float h = (float) obj.getDouble("height");
                    String uriStr = obj.getString("uri");
                    try {
                        Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), android.net.Uri.parse(uriStr));
                        images.add(new ImageItem(x, y, w, h, bitmap, uriStr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (mainObj.has("texts")) {
                JSONArray textsArray = mainObj.getJSONArray("texts");
                for (int i = 0; i < textsArray.length(); i++) {
                    JSONObject obj = textsArray.getJSONObject(i);
                    texts.add(new TextItem((float) obj.getDouble("x"), (float) obj.getDouble("y"), obj.getString("text"), obj.getInt("color"), (float) obj.getDouble("textSize")));
                }
            }

            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseStrokes(JSONArray pathsArray) throws Exception {
        for (int i = 0; i < pathsArray.length(); i++) {
            JSONObject obj = pathsArray.getJSONObject(i);
            int color = obj.getInt("color");
            float width = (float) obj.getDouble("strokeWidth");
            boolean isEraser = obj.optBoolean("isEraser", false);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(width);

            if (isEraser) {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                paint.setColor(0x00000000);
            } else {
                paint.setXfermode(null);
                paint.setColor(color);
            }

            JSONArray pointsArray = obj.getJSONArray("points");
            Path path = new Path();
            List<Point> points = new ArrayList<>();

            for (int j = 0; j < pointsArray.length(); j++) {
                JSONObject p = pointsArray.getJSONObject(j);
                float px = (float) p.getDouble("x");
                float py = (float) p.getDouble("y");
                points.add(new Point(px, py));
                if (j == 0) path.moveTo(px, py);
                else path.lineTo(px, py);
            }
            strokes.add(new StrokeItem(path, paint, points, color, width, isEraser));
        }
    }
}