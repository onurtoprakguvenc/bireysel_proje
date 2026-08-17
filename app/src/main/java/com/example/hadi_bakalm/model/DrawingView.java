package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class DrawingView extends View {

    private static final String TAG = "DrawingView";

    public enum ToolMode { PEN, HIGHLIGHTER, ERASER, SCROLL, SELECT, LASSO, RECTANGLE, SQUARE, CIRCLE, LINE, TEXT }

    public interface OnDrawingChangeListener {
        void onDrawingChanged(String jsonContent);
    }

    private OnDrawingChangeListener onDrawingChangeListener;
    private TextItem editingTextItem = null;
    private TableCellClickResult editingTableCell = null;

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

        public void offset(float dx, float dy) {
            path.offset(dx, dy);
            for (Point p : points) {
                p.x += dx;
                p.y += dy;
            }
        }
    }

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

        public RectF getExactGeometry() {
            if (shapeType == ToolMode.SQUARE) {
                float dx = endX - startX;
                float dy = endY - startY;
                float side = Math.max(Math.abs(dx), Math.abs(dy));
                float left = (dx < 0) ? startX - side : startX;
                float top = (dy < 0) ? startY - side : startY;
                return new RectF(left, top, left + side, top + side);
            } else {
                float left = Math.min(startX, endX);
                float top = Math.min(startY, endY);
                float right = Math.max(startX, endX);
                float bottom = Math.max(startY, endY);
                return new RectF(left, top, right, bottom);
            }
        }

        public RectF getBounds() {
            RectF geo = getExactGeometry();
            float pad = Math.max(strokeWidth / 2f, 15f);
            return new RectF(geo.left - pad, geo.top - pad, geo.right + pad, geo.bottom + pad);
        }

        public RectF getResizeHandle() {
            RectF geo = getExactGeometry();
            return new RectF(geo.right - 25f, geo.bottom - 25f, geo.right + 25f, geo.bottom + 25f);
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

        public void offset(float dx, float dy) {
            x += dx;
            y += dy;
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

        public RectF getBounds(Paint paint) {
            if (text == null || text.isEmpty()) {
                return new RectF(x, y, x, y);
            }
            float prevSize = paint.getTextSize();
            paint.setTextSize(textSize > 0 ? textSize : 36f);

            float textW = paint.measureText(text);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float pad = 12f;

            float left = x - pad;
            float top = y + fm.ascent - pad;
            float right = x + textW + pad;
            float bottom = y + fm.descent + pad;

            paint.setTextSize(prevSize);
            return new RectF(left, top, right, bottom);
        }

        public void offset(float dx, float dy) {
            x += dx;
            y += dy;
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
        public float defaultCellWidth = 160f;
        public float cellHeight = 90f;
        public int rows, cols;
        public List<TableCell> cells = new ArrayList<>();

        public TableItem(float startX, float startY, int rows, int cols) {
            this.startX = startX;
            this.startY = startY;
            this.rows = rows;
            this.cols = cols;
        }

        public float[] getColumnWidths(Paint textPaint) {
            float[] colWidths = new float[cols];
            for (int c = 0; c < cols; c++) {
                float maxW = defaultCellWidth;
                for (TableCell cell : cells) {
                    if (cell.col == c && cell.text != null && !cell.text.isEmpty()) {
                        float textW = textPaint.measureText(cell.text) + 32f;
                        if (textW > maxW) {
                            maxW = textW;
                        }
                    }
                }
                colWidths[c] = maxW;
            }
            return colWidths;
        }

        public float getTotalWidth(Paint textPaint) {
            float[] colWidths = getColumnWidths(textPaint);
            float total = 0f;
            for (float w : colWidths) total += w;
            return total;
        }

        public RectF getBounds(Paint textPaint) {
            return new RectF(startX, startY, startX + getTotalWidth(textPaint), startY + (rows * cellHeight));
        }

        public void offset(float dx, float dy) {
            startX += dx;
            startY += dy;
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
    // 2. TUVAL VERİ HAVUZU
    // =========================================================================

    private final List<StrokeItem> strokes = new ArrayList<>();
    private final List<StrokeItem> undoneStrokes = new ArrayList<>();
    private final List<ShapeItem> shapes = new ArrayList<>();
    private final List<ShapeItem> undoneShapes = new ArrayList<>();
    private final List<ImageItem> images = new ArrayList<>();
    private final List<TextItem> texts = new ArrayList<>();
    private final List<TableItem> tables = new ArrayList<>();

    private Object selectedItem = null;
    private final RectF menuDeleteBounds = new RectF();
    private final RectF menuSizeUpBounds = new RectF();
    private final RectF menuSizeDownBounds = new RectF();

    private final List<Object> selectedGroup = new ArrayList<>();
    private final RectF groupBounds = new RectF();
    private Path lassoPath = null;
    private boolean isDraggingGroup = false;
    private float groupDragStartX, groupDragStartY;

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
    private Paint lassoPaint;

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

        lassoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lassoPaint.setStyle(Paint.Style.STROKE);
        lassoPaint.setColor(0xFF0284C7);
        lassoPaint.setStrokeWidth(3f);
        lassoPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
                invalidate();
                return true;
            }
        });
    }

    // =========================================================================
    // 4. RENDER DÖNGÜSÜ
    // =========================================================================

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(0, offsetY);

        for (StrokeItem stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

        renderShapes(canvas);

        if (activePath != null && activePaint != null && currentToolMode != ToolMode.SCROLL &&
                currentToolMode != ToolMode.SELECT && currentToolMode != ToolMode.LASSO && currentToolMode != ToolMode.TEXT) {
            canvas.drawPath(activePath, activePaint);
        }

        renderTables(canvas);
        renderImages(canvas);
        renderTexts(canvas);

        if (lassoPath != null) {
            canvas.drawPath(lassoPath, lassoPaint);
        }

        if (!selectedGroup.isEmpty()) {
            renderGroupSelectionAndMenu(canvas);
        }

        if (currentToolMode == ToolMode.SELECT && selectedItem != null && selectedGroup.isEmpty()) {
            renderSelectionAndFloatingMenu(canvas);
        }

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
            RectF geo = s.getExactGeometry();

            if (s.shapeType == ToolMode.RECTANGLE || s.shapeType == ToolMode.SQUARE) {
                canvas.drawRect(geo, shapeRenderPaint);
            } else if (s.shapeType == ToolMode.CIRCLE) {
                canvas.drawOval(geo, shapeRenderPaint);
            } else if (s.shapeType == ToolMode.LINE) {
                canvas.drawLine(s.startX, s.startY, s.endX, s.endY, shapeRenderPaint);
            }
        }
    }

    private void renderTables(Canvas canvas) {
        for (TableItem table : tables) {
            float[] colWidths = table.getColumnWidths(textPaint);
            float totalW = 0f;
            for (float w : colWidths) totalW += w;
            float totalH = table.rows * table.cellHeight;

            for (int i = 0; i <= table.rows; i++) {
                float y = table.startY + (i * table.cellHeight);
                canvas.drawLine(table.startX, y, table.startX + totalW, y, tablePaint);
            }

            float currentX = table.startX;
            canvas.drawLine(currentX, table.startY, currentX, table.startY + totalH, tablePaint);
            for (int j = 0; j < table.cols; j++) {
                currentX += colWidths[j];
                canvas.drawLine(currentX, table.startY, currentX, table.startY + totalH, tablePaint);
            }

            for (TableCell cell : table.cells) {
                // Eğer bu hücre şu an EditText ile düzenleniyorsa tuvalde çizme:
                if (editingTableCell != null && editingTableCell.table == table &&
                        editingTableCell.row == cell.row && editingTableCell.col == cell.col) {
                    continue;
                }

                if (cell.text != null && !cell.text.isEmpty()) {
                    float cellStartX = table.startX;
                    for (int c = 0; c < cell.col; c++) {
                        cellStartX += colWidths[c];
                    }
                    float cellW = colWidths[cell.col];
                    float cellY = table.startY + (cell.row * table.cellHeight);

                    canvas.save();
                    canvas.clipRect(cellStartX + 4f, cellY + 4f, cellStartX + cellW - 4f, cellY + table.cellHeight - 4f);

                    float cx = cellStartX + (cellW / 2f);
                    float cy = cellY + (table.cellHeight / 2f) + 10f;
                    canvas.drawText(cell.text, cx, cy, textPaint);

                    canvas.restore();
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
            if (t == editingTextItem) continue;
            if (t.text != null && !t.text.isEmpty()) {
                freeTextPaint.setColor(t.color);
                freeTextPaint.setTextSize(t.textSize > 0 ? t.textSize : 36f);
                canvas.drawText(t.text, t.x, t.y, freeTextPaint);
            }
        }
    }

    private void renderGroupSelectionAndMenu(Canvas canvas) {
        canvas.drawRect(groupBounds, selectionBoxPaint);

        float menuW = 120f;
        float menuH = 64f;
        float menuX = groupBounds.left + (groupBounds.width() - menuW) / 2f;
        float menuY = groupBounds.top - menuH - 20f;

        if (menuY < -offsetY + 10f) {
            menuY = groupBounds.bottom + 20f;
        }

        menuSizeDownBounds.setEmpty();
        menuSizeUpBounds.setEmpty();
        menuDeleteBounds.set(menuX, menuY, menuX + menuW, menuY + menuH);
        canvas.drawRoundRect(menuDeleteBounds, 32f, 32f, menuBgPaint);
        canvas.drawText("Sil", menuX + (menuW / 2f), menuY + 40f, menuTextPaint);
    }

    private void renderSelectionAndFloatingMenu(Canvas canvas) {
        RectF bounds = new RectF();
        boolean isText = (selectedItem instanceof TextItem);

        if (selectedItem instanceof ImageItem) {
            bounds = ((ImageItem) selectedItem).getBounds();
            canvas.drawCircle(bounds.right, bounds.bottom, 20f, handlePaint);
        } else if (selectedItem instanceof TextItem) {
            TextItem t = (TextItem) selectedItem;
            bounds = t.getBounds(freeTextPaint);
        } else if (selectedItem instanceof ShapeItem) {
            bounds = ((ShapeItem) selectedItem).getBounds();
            canvas.drawCircle(bounds.right, bounds.bottom, 20f, handlePaint);
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
    // 5. DOKUNMA VE ETKİLEŞİM YÖNETİMİ
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
    public boolean performClick() {
        return super.performClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
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

                if (currentToolMode == ToolMode.LASSO) {
                    handleLassoDown(touchX, touchY);
                    return true;
                }

                selectedItem = null;
                selectedGroup.clear();
                groupBounds.setEmpty();
                startStroke(touchX, touchY);
                break;

            case MotionEvent.ACTION_MOVE:
                lastMoveX = touchX;
                lastMoveY = touchY;

                if (currentToolMode == ToolMode.SELECT) {
                    handleSelectMove(touchX, touchY);
                    return true;
                }

                if (currentToolMode == ToolMode.LASSO) {
                    handleLassoMove(touchX, touchY);
                    return true;
                }

                continueStroke(touchX, touchY);
                break;

            case MotionEvent.ACTION_UP:
                performClick();
                if (currentToolMode == ToolMode.SELECT) {
                    handleSelectUp();
                    return true;
                }

                if (currentToolMode == ToolMode.LASSO) {
                    handleLassoUp();
                    return true;
                }

                finishStroke();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (currentToolMode == ToolMode.SELECT) {
                    handleSelectUp();
                    return true;
                }

                if (currentToolMode == ToolMode.LASSO) {
                    handleLassoUp();
                    return true;
                }

                finishStroke();
                break;
        }

        invalidate();
        return true;
    }

    private void handleSelectDown(float x, float y) {
        selectedGroup.clear();
        groupBounds.setEmpty();

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

        if (selectedItem instanceof ImageItem) {
            ImageItem img = (ImageItem) selectedItem;
            if (img.getResizeHandle().contains(x, y)) {
                isResizingImage = true;
                return;
            }
        }

        if (selectedItem instanceof ShapeItem) {
            ShapeItem s = (ShapeItem) selectedItem;
            if (s.getResizeHandle().contains(x, y)) {
                isResizingImage = true;
                return;
            }
        }

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

        for (int i = texts.size() - 1; i >= 0; i--) {
            TextItem t = texts.get(i);
            RectF tBounds = t.getBounds(freeTextPaint);
            if (tBounds.contains(x, y)) {
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
        if (isResizingImage && selectedItem != null) {
            if (selectedItem instanceof ImageItem) {
                ImageItem img = (ImageItem) selectedItem;
                float newW = Math.max(80f, x - img.x);
                float ratio = (float) img.bitmap.getHeight() / (float) img.bitmap.getWidth();
                img.width = newW;
                img.height = newW * ratio;
                invalidate();
            } else if (selectedItem instanceof ShapeItem) {
                ShapeItem s = (ShapeItem) selectedItem;
                s.endX = x;
                s.endY = y;
                invalidate();
            }
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

    // --- KEMENT (LASSO) METOTLARI (TABLO ENTEGRELİ) ---
    private void handleLassoDown(float x, float y) {
        selectedItem = null;

        if (!selectedGroup.isEmpty()) {
            if (menuDeleteBounds.contains(x, y)) {
                for (Object obj : selectedGroup) {
                    if (obj instanceof StrokeItem) strokes.remove((StrokeItem) obj);
                    else if (obj instanceof ShapeItem) shapes.remove((ShapeItem) obj);
                    else if (obj instanceof ImageItem) images.remove((ImageItem) obj);
                    else if (obj instanceof TextItem) texts.remove((TextItem) obj);
                    else if (obj instanceof TableItem) tables.remove((TableItem) obj);
                }
                selectedGroup.clear();
                groupBounds.setEmpty();
                notifyChange();
                invalidate();
                return;
            }

            if (groupBounds.contains(x, y)) {
                isDraggingGroup = true;
                groupDragStartX = x;
                groupDragStartY = y;
                return;
            }

            selectedGroup.clear();
            groupBounds.setEmpty();
        }

        lassoPath = new Path();
        lassoPath.moveTo(x, y);
        invalidate();
    }

    private void handleLassoMove(float x, float y) {
        if (isDraggingGroup) {
            float dx = x - groupDragStartX;
            float dy = y - groupDragStartY;

            for (Object obj : selectedGroup) {
                if (obj instanceof StrokeItem) ((StrokeItem) obj).offset(dx, dy);
                else if (obj instanceof ShapeItem) ((ShapeItem) obj).offset(dx, dy);
                else if (obj instanceof ImageItem) ((ImageItem) obj).offset(dx, dy);
                else if (obj instanceof TextItem) ((TextItem) obj).offset(dx, dy);
                else if (obj instanceof TableItem) ((TableItem) obj).offset(dx, dy);
            }

            groupBounds.offset(dx, dy);
            groupDragStartX = x;
            groupDragStartY = y;
            invalidate();
            return;
        }

        if (lassoPath != null) {
            lassoPath.lineTo(x, y);
            invalidate();
        }
    }

    private void handleLassoUp() {
        if (isDraggingGroup) {
            isDraggingGroup = false;
            notifyChange();
            return;
        }

        if (lassoPath != null) {
            calculateLassoSelection();
        }
    }

    private void calculateLassoSelection() {
        if (lassoPath == null) return;

        selectedGroup.clear();
        lassoPath.close();

        RectF lassoRect = new RectF();
        lassoPath.computeBounds(lassoRect, true);

        Region clipRegion = new Region((int) lassoRect.left, (int) lassoRect.top, (int) lassoRect.right, (int) lassoRect.bottom);
        Region lassoRegion = new Region();
        lassoRegion.setPath(lassoPath, clipRegion);

        for (StrokeItem stroke : strokes) {
            for (Point p : stroke.points) {
                if (lassoRegion.contains((int) p.x, (int) p.y)) {
                    selectedGroup.add(stroke);
                    break;
                }
            }
        }

        for (ShapeItem shape : shapes) {
            RectF b = shape.getBounds();
            if (lassoRegion.contains((int) b.centerX(), (int) b.centerY())) {
                selectedGroup.add(shape);
            }
        }

        for (ImageItem img : images) {
            RectF b = img.getBounds();
            if (lassoRegion.contains((int) b.centerX(), (int) b.centerY())) {
                selectedGroup.add(img);
            }
        }

        for (TextItem txt : texts) {
            RectF b = txt.getBounds(freeTextPaint);
            if (lassoRegion.contains((int) b.centerX(), (int) b.centerY())) {
                selectedGroup.add(txt);
            }
        }

        for (TableItem table : tables) {
            RectF b = table.getBounds(textPaint);
            if (lassoRegion.contains((int) b.centerX(), (int) b.centerY())) {
                selectedGroup.add(table);
            }
        }

        calculateGroupBounds();
        lassoPath = null;
        invalidate();
    }

    private void calculateGroupBounds() {
        if (selectedGroup.isEmpty()) {
            groupBounds.setEmpty();
            return;
        }

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

        for (Object obj : selectedGroup) {
            if (obj instanceof StrokeItem) {
                for (Point p : ((StrokeItem) obj).points) {
                    minX = Math.min(minX, p.x);
                    minY = Math.min(minY, p.y);
                    maxX = Math.max(maxX, p.x);
                    maxY = Math.max(maxY, p.y);
                }
            } else if (obj instanceof ShapeItem) {
                RectF b = ((ShapeItem) obj).getBounds();
                minX = Math.min(minX, b.left);
                minY = Math.min(minY, b.top);
                maxX = Math.max(maxX, b.right);
                maxY = Math.max(maxY, b.bottom);
            } else if (obj instanceof ImageItem) {
                RectF b = ((ImageItem) obj).getBounds();
                minX = Math.min(minX, b.left);
                minY = Math.min(minY, b.top);
                maxX = Math.max(maxX, b.right);
                maxY = Math.max(maxY, b.bottom);
            } else if (obj instanceof TextItem) {
                RectF b = ((TextItem) obj).getBounds(freeTextPaint);
                minX = Math.min(minX, b.left);
                minY = Math.min(minY, b.top);
                maxX = Math.max(maxX, b.right);
                maxY = Math.max(maxY, b.bottom);
            } else if (obj instanceof TableItem) {
                RectF b = ((TableItem) obj).getBounds(textPaint);
                minX = Math.min(minX, b.left);
                minY = Math.min(minY, b.top);
                maxX = Math.max(maxX, b.right);
                maxY = Math.max(maxY, b.bottom);
            }
        }

        groupBounds.set(minX - 16f, minY - 16f, maxX + 16f, maxY + 16f);
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
            // DrawingView.java -> continueStroke(float x, float y) içinde:

        } else if (currentToolMode == ToolMode.RECTANGLE) {
            activePath.reset();
            float left = Math.min(touchStartX, x);
            float top = Math.min(touchStartY, y);
            float right = Math.max(touchStartX, x);
            float bottom = Math.max(touchStartY, y);
            activePath.addRect(left, top, right, bottom, Path.Direction.CW);
        } else if (currentToolMode == ToolMode.SQUARE) {
            activePath.reset();
            float dx = x - touchStartX;
            float dy = y - touchStartY;
            float side = Math.max(Math.abs(dx), Math.abs(dy));
            float left = (dx < 0) ? touchStartX - side : touchStartX;
            float top = (dy < 0) ? touchStartY - side : touchStartY;
            float right = left + side;
            float bottom = top + side;
            activePath.addRect(left, top, right, bottom, Path.Direction.CW);
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
    // 6. KAMUYA AÇIK API
    // =========================================================================

    public void setToolMode(ToolMode mode) {
        this.currentToolMode = mode;
        this.selectedItem = null;
        this.selectedGroup.clear();
        this.groupBounds.setEmpty();
        this.lassoPath = null;
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
        selectedGroup.clear();
        groupBounds.setEmpty();
        lassoPath = null;
        activePath = null;
        offsetY = 0f;
        scaleFactor = 1.0f;
        editingTextItem = null;
        editingTableCell = null;
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
        if (editingTextItem == item) editingTextItem = null;
        notifyChange();
        invalidate();
    }

    public void setEditingTextItem(TextItem item) {
        this.editingTextItem = item;
        invalidate();
    }

    public void setEditingTableCell(TableCellClickResult cellResult) {
        this.editingTableCell = cellResult;
        invalidate();
    }

    public TableCellClickResult getEditingTableCell() {
        return this.editingTableCell;
    }

    public TextItem checkTextClick(float touchX, float touchY) {
        for (int i = texts.size() - 1; i >= 0; i--) {
            TextItem t = texts.get(i);
            RectF bounds = t.getBounds(freeTextPaint);
            if (bounds.contains(touchX, touchY)) {
                return t;
            }
        }
        return null;
    }

    public void addTableToCanvas(int rows, int cols) {
        float startX = 60f;
        float startY = -offsetY + 140f;
        addTableToCanvas(startX, startY, rows, cols);
    }

    public void addTableToCanvas(float x, float y, int rows, int cols) {
        TableItem table = new TableItem(x, y, rows, cols);
        tables.add(table);
        notifyChange();
        invalidate();
    }

    public TableCellClickResult checkTableCellClick(float touchX, float touchY) {
        for (TableItem table : tables) {
            float[] colWidths = table.getColumnWidths(textPaint);
            float totalW = 0f;
            for (float w : colWidths) totalW += w;
            float totalH = table.rows * table.cellHeight;

            if (touchX >= table.startX && touchX <= table.startX + totalW &&
                    touchY >= table.startY && touchY <= table.startY + totalH) {

                int row = (int) ((touchY - table.startY) / table.cellHeight);
                float currentX = table.startX;
                int clickedCol = -1;
                for (int c = 0; c < table.cols; c++) {
                    if (touchX >= currentX && touchX <= currentX + colWidths[c]) {
                        clickedCol = c;
                        break;
                    }
                    currentX += colWidths[c];
                }

                if (clickedCol != -1 && row < table.rows) {
                    return new TableCellClickResult(table, row, clickedCol);
                }
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
    // 7. JSON SERİLEŞTİRME
    // =========================================================================

    public String getDrawingJson() {
        try {
            JSONObject mainObj = new JSONObject();
            mainObj.put("paths", serializePaths());
            mainObj.put("shapes", serializeShapes());
            mainObj.put("tables", serializeTables());
            mainObj.put("images", serializeImages());
            mainObj.put("texts", serializeTexts());
            return mainObj.toString();
        } catch (Exception e) {
            Log.e(TAG, "getDrawingJson hatası", e);
            return "";
        }
    }

    private JSONArray serializePaths() throws Exception {
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
        return pathsArray;
    }

    private JSONArray serializeShapes() throws Exception {
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
        return shapesArray;
    }

    private JSONArray serializeTables() throws Exception {
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
        return tablesArray;
    }

    private JSONArray serializeImages() throws Exception {
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
        return imagesArray;
    }

    private JSONArray serializeTexts() throws Exception {
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
        return textsArray;
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
                        Uri uri = Uri.parse(uriStr);
                        Bitmap bitmap;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContext().getContentResolver(), uri));
                        } else {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                        }
                        images.add(new ImageItem(x, y, w, h, bitmap, uriStr));
                    } catch (Exception e) {
                        Log.e(TAG, "Görsel yüklenemedi: " + uriStr, e);
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
            Log.e(TAG, "loadDrawingFromJson hatası", e);
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