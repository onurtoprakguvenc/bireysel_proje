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
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class DrawingView extends View {

    private static final String TAG = "DrawingView";

    public enum ToolMode {PEN, HIGHLIGHTER, ERASER, HAND, SELECT, LASSO, RECTANGLE, SQUARE, CIRCLE, LINE, TEXT}

    public enum PageGridMode {BLANK, GRID, HORIZONTAL_LINES, VERTICAL_LINES}

    public enum CanvasTheme {
        WHITE(0xFFFFFFFF, 0xFFE2E8F0),   // Beyaz kağıt - Açık gri çizgiler
        SEPIA(0xFFFDF6E2, 0xFFE4D5B7),   // Sarımsı kağıt - Sıcak sepya çizgiler
        DARK(0xFF1E293B, 0xFF334155);    // Koyu lacivert/siyah - Koyu gri çizgiler

        public final int bgColor;
        public final int gridLineColor;

        CanvasTheme(int bgColor, int gridLineColor) {
            this.bgColor = bgColor;
            this.gridLineColor = gridLineColor;
        }
    }

    private enum SnappedType {NONE, LINE, CIRCLE, RECTANGLE}

    public interface OnDrawingChangeListener {
        void onDrawingChanged(String jsonContent);
    }

    private OnDrawingChangeListener onDrawingChangeListener;
    private TextItem editingTextItem = null;
    private TableCellClickResult editingTableCell = null;

    private boolean isLocked = false;
    private boolean isMultiTouchGesturing = false;

    // Otomatik Şekil Düzeltme (Snap to Shape) Motoru
    private final Handler snapShapeHandler = new Handler(Looper.getMainLooper());
    private boolean isSnapShapeTriggered = false;
    private float dynamicCanvasHeight = 0f;
    private SnappedType currentSnappedType = SnappedType.NONE;
    private float snapAnchorX = 0f, snapAnchorY = 0f;
    private float snappedMinX, snappedMinY, snappedMaxX, snappedMaxY;
    private final Runnable snapShapeRunnable = this::attemptSnapToShape;

    public void setOnDrawingChangeListener(OnDrawingChangeListener listener) {
        this.onDrawingChangeListener = listener;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
        invalidate();
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    // =========================================================================
    // 1. SAHNE VE GEÇMİŞ İŞLEM NESNELERİ
    // =========================================================================

    public static class DeleteAction {
        public List<Object> deletedItems = new ArrayList<>();

        public DeleteAction(List<Object> items) {
            this.deletedItems.addAll(items);
        }

        public DeleteAction(Object item) {
            this.deletedItems.add(item);
        }
    }

    public static class Point {
        public float x, y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
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
            float left = Math.min(startX, endX);
            float top = Math.min(startY, endY);
            float right = Math.max(startX, endX);
            float bottom = Math.max(startY, endY);

            if (shapeType == ToolMode.SQUARE) {
                float side = Math.max(right - left, bottom - top);
                return new RectF(left, top, left + side, top + side);
            }
            return new RectF(left, top, right, bottom);
        }

        public RectF getBounds() {
            RectF geo = getExactGeometry();
            float pad = Math.max(strokeWidth, 24f);
            return new RectF(geo.left - pad, geo.top - pad, geo.right + pad, geo.bottom + pad);
        }

        public RectF getResizeHandle() {
            RectF geo = getExactGeometry();
            return new RectF(geo.right - 30f, geo.bottom - 30f, geo.right + 30f, geo.bottom + 30f);
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

        public RectF getResizeHandle(Paint textPaint) {
            RectF b = getBounds(textPaint);
            return new RectF(b.right - 30f, b.bottom - 30f, b.right + 30f, b.bottom + 30f);
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
    // 2. TUVAL VERİ HAVUZU & İŞLEM GEÇMİŞİ
    // =========================================================================

    private final List<StrokeItem> strokes = new ArrayList<>();
    private final List<ShapeItem> shapes = new ArrayList<>();
    private final List<ImageItem> images = new ArrayList<>();
    private final List<TextItem> texts = new ArrayList<>();
    private final List<TableItem> tables = new ArrayList<>();

    private final List<Object> historyStack = new ArrayList<>();
    private final List<Object> redoStack = new ArrayList<>();

    private PageGridMode currentPageGridMode = PageGridMode.BLANK;
    private CanvasTheme currentCanvasTheme = CanvasTheme.WHITE;

    private Object selectedItem = null;
    private final RectF menuDeleteBounds = new RectF();
    private final RectF menuCopyBounds = new RectF();
    private final RectF menuSizeUpBounds = new RectF();
    private final RectF menuSizeDownBounds = new RectF();

    private final List<Object> selectedGroup = new ArrayList<>();
    private final RectF groupBounds = new RectF();
    private Path lassoPath = null;
    private boolean isDraggingGroup = false;
    private boolean isResizingGroup = false;
    private float groupDragStartX, groupDragStartY;

    private int currentColor = 0xFF09090B;
    private float currentStrokeWidth = 8f;
    private ToolMode currentToolMode = ToolMode.PEN;

    private float scaleFactor = 1.0f;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 4.0f;

    private float offsetX = 0f;
    private float offsetY = 0f;
    private float lastTouchX;
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
    private Paint gridPaint;

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

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(0xFFE2E8F0);
        gridPaint.setStrokeWidth(1.5f);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                float prevScale = scaleFactor;
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();

                offsetX += (focusX / scaleFactor) - (focusX / prevScale);
                offsetY += (focusY / scaleFactor) - (focusY / prevScale);

                clampOffsets();
                invalidate();
                return true;
            }
        });
    }

    public void setCanvasTheme(CanvasTheme theme) {
        if (theme == null) return;
        this.currentCanvasTheme = theme;

        // Karanlık temaya geçildiğinde siyah kalem rengini otomatik beyaz yap
        if (theme == CanvasTheme.DARK && this.currentColor == 0xFF09090B) {
            this.currentColor = 0xFFFFFFFF;
        } else if (theme != CanvasTheme.DARK && this.currentColor == 0xFFFFFFFF) {
            this.currentColor = 0xFF09090B;
        }

        invalidate();
    }

    public CanvasTheme getCanvasTheme() {
        return currentCanvasTheme;
    }

    public void resetZoomAndPosition() {
        this.scaleFactor = 1.0f;
        this.offsetX = 0f;
        this.offsetY = 0f;
        invalidate();
    }

    public Bitmap exportSelectedArea(RectF selectionBounds) {
        if (selectionBounds == null || selectionBounds.width() <= 0 || selectionBounds.height() <= 0) {
            return null;
        }

        try {
            int width = (int) selectionBounds.width();
            int height = (int) selectionBounds.height();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            canvas.drawColor(currentCanvasTheme.bgColor);
            canvas.translate(-selectionBounds.left, -selectionBounds.top);

            renderPageBackgroundGuides(canvas);
            renderShapes(canvas);
            for (StrokeItem stroke : strokes) {
                canvas.drawPath(stroke.path, stroke.paint);
            }
            renderTables(canvas);
            renderImages(canvas);
            renderTexts(canvas);

            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "exportSelectedArea hatası", e);
            return null;
        }
    }

    private void clampOffsets() {
        float viewW = getWidth() > 0 ? getWidth() : 1080f;
        float viewH = getHeight() > 0 ? getHeight() : 1920f;

        float maxOffsetX = viewW * 0.25f;
        float minOffsetX = -(viewW * scaleFactor) * 0.5f;

        float contentBottom = calculateContentBottomY();
        float currentScrollDepth = (-offsetY + (viewH / scaleFactor));

        float maxReachedBottom = Math.max(contentBottom, currentScrollDepth);
        dynamicCanvasHeight = Math.max(viewH * 2.0f, maxReachedBottom + (viewH * 2.0f));

        float maxOffsetY = viewH * 0.15f;
        float minOffsetY = -dynamicCanvasHeight;

        offsetX = Math.max(minOffsetX, Math.min(maxOffsetX, offsetX));
        offsetY = Math.max(minOffsetY, Math.min(maxOffsetY, offsetY));
    }

    // =========================================================================
    // 4. RENDER DÖNGÜSÜ (ONDRAW)
    // =========================================================================

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // 1. Tuval arka planını seçili temaya göre boya
        canvas.drawColor(currentCanvasTheme.bgColor);

        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);
        canvas.translate(offsetX, offsetY);

        // 2. Kılavuz çizgilerini temaya uyarlanmış renkle çiz
        renderPageBackgroundGuides(canvas);

        int layerId = canvas.saveLayer(null, null);
        renderShapes(canvas);

        for (StrokeItem stroke : strokes) {
            canvas.drawPath(stroke.path, stroke.paint);
        }

        if (activePath != null && activePaint != null && currentToolMode != ToolMode.HAND &&
                currentToolMode != ToolMode.SELECT && currentToolMode != ToolMode.LASSO && currentToolMode != ToolMode.TEXT) {
            canvas.drawPath(activePath, activePaint);
        }

        canvas.restoreToCount(layerId);

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

    private void renderPageBackgroundGuides(Canvas canvas) {
        if (currentPageGridMode == PageGridMode.BLANK) return;

        // Çizgilerin rengini güncel temanın kılavuz rengine ayarla
        gridPaint.setColor(currentCanvasTheme.gridLineColor);

        float lineSpacing = 64f;
        float startX = -offsetX;
        float startY = -offsetY;

        if (currentPageGridMode == PageGridMode.GRID) {
            for (float x = startX - (startX % lineSpacing); x < startX + (getWidth() / scaleFactor); x += lineSpacing) {
                canvas.drawLine(x, startY, x, startY + (getHeight() / scaleFactor), gridPaint);
            }
            for (float y = startY - (startY % lineSpacing); y < startY + (getHeight() / scaleFactor); y += lineSpacing) {
                canvas.drawLine(startX, y, startX + (getWidth() / scaleFactor), y, gridPaint);
            }
        } else if (currentPageGridMode == PageGridMode.HORIZONTAL_LINES) {
            for (float y = startY - (startY % lineSpacing); y < startY + (getHeight() / scaleFactor); y += lineSpacing) {
                canvas.drawLine(startX, y, startX + (getWidth() / scaleFactor), y, gridPaint);
            }
        } else if (currentPageGridMode == PageGridMode.VERTICAL_LINES) {
            for (float x = startX - (startX % lineSpacing); x < startX + (getWidth() / scaleFactor); x += lineSpacing) {
                canvas.drawLine(x, startY, x, startY + (getHeight() / scaleFactor), gridPaint);
            }
        }
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

    private RectF getGroupResizeHandle() {
        return new RectF(groupBounds.right - 30f, groupBounds.bottom - 30f, groupBounds.right + 30f, groupBounds.bottom + 30f);
    }

    private void renderGroupSelectionAndMenu(Canvas canvas) {
        canvas.drawRect(groupBounds, selectionBoxPaint);
        canvas.drawCircle(groupBounds.right, groupBounds.bottom, 16f, handlePaint);

        float menuW = 220f;
        float menuH = 56f;
        float menuX = groupBounds.left + (groupBounds.width() - menuW) / 2f;
        float menuY = groupBounds.top - menuH - 16f;

        if (menuY < -offsetY + 10f) {
            menuY = groupBounds.bottom + 16f;
        }

        RectF totalMenuRect = new RectF(menuX, menuY, menuX + menuW, menuY + menuH);
        canvas.drawRoundRect(totalMenuRect, 28f, 28f, menuBgPaint);

        menuCopyBounds.set(menuX, menuY, menuX + 110f, menuY + menuH);
        canvas.drawText("Kopyala", menuX + 55f, menuY + 36f, menuTextPaint);

        canvas.drawLine(menuX + 110f, menuY + 12f, menuX + 110f, menuY + menuH - 12f, gridPaint);

        menuDeleteBounds.set(menuX + 110f, menuY, menuX + 220f, menuY + menuH);
        canvas.drawText("Sil", menuX + 165f, menuY + 36f, menuTextPaint);
    }

    private void renderSelectionAndFloatingMenu(Canvas canvas) {
        RectF bounds = new RectF();
        boolean isText = (selectedItem instanceof TextItem);

        if (selectedItem instanceof ShapeItem) {
            ShapeItem s = (ShapeItem) selectedItem;
            bounds = s.getExactGeometry();
            canvas.drawCircle(bounds.right, bounds.bottom, 16f, handlePaint);
        } else if (selectedItem instanceof ImageItem) {
            bounds = ((ImageItem) selectedItem).getBounds();
            canvas.drawCircle(bounds.right, bounds.bottom, 16f, handlePaint);
        } else if (selectedItem instanceof TableItem) {
            TableItem tbl = (TableItem) selectedItem;
            bounds = tbl.getBounds(textPaint);
            canvas.drawCircle(bounds.right, bounds.bottom, 16f, handlePaint);
        } else if (selectedItem instanceof TextItem) {
            TextItem t = (TextItem) selectedItem;
            bounds = t.getBounds(freeTextPaint);
        }

        canvas.drawRect(bounds, selectionBoxPaint);

        float menuW = isText ? 240f : 120f;
        float menuH = 56f;
        float menuX = bounds.left + (bounds.width() - menuW) / 2f;
        float menuY = bounds.top - menuH - 16f;

        if (menuY < -offsetY + 10f) {
            menuY = bounds.bottom + 16f;
        }

        RectF menuRect = new RectF(menuX, menuY, menuX + menuW, menuY + menuH);
        canvas.drawRoundRect(menuRect, 28f, 28f, menuBgPaint);

        if (isText) {
            menuSizeDownBounds.set(menuX, menuY, menuX + 80f, menuY + menuH);
            canvas.drawText("A-", menuX + 40f, menuY + 36f, menuTextPaint);

            menuSizeUpBounds.set(menuX + 80f, menuY, menuX + 160f, menuY + menuH);
            canvas.drawText("A+", menuX + 120f, menuY + 36f, menuTextPaint);

            menuDeleteBounds.set(menuX + 160f, menuY, menuX + 240f, menuY + menuH);
            canvas.drawText("Sil", menuX + 200f, menuY + 36f, menuTextPaint);
        } else {
            menuSizeDownBounds.setEmpty();
            menuSizeUpBounds.setEmpty();
            menuDeleteBounds.set(menuX, menuY, menuX + menuW, menuY + menuH);
            canvas.drawText("Sil", menuX + (menuW / 2f), menuY + 36f, menuTextPaint);
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
    private boolean isResizingObject = false;
    private float dragOffsetX, dragOffsetY;

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();

        if (event.getPointerCount() > 1 || action == MotionEvent.ACTION_POINTER_DOWN) {
            isMultiTouchGesturing = true;
            snapShapeHandler.removeCallbacks(snapShapeRunnable);

            activePath = null;
            activePaint = null;
            activePoints = null;
            isSnapShapeTriggered = false;
            currentSnappedType = SnappedType.NONE;

            handleScrollTouch(event);
            invalidate();
            return true;
        }

        if (isMultiTouchGesturing) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                isMultiTouchGesturing = false;
            }
            return true;
        }

        if (currentToolMode == ToolMode.HAND) {
            handleScrollTouch(event);
            return true;
        }

        if (isLocked || currentToolMode == ToolMode.TEXT) {
            snapShapeHandler.removeCallbacks(snapShapeRunnable);
            return false;
        }

        float touchX = (event.getX() / scaleFactor) - offsetX;
        float touchY = (event.getY() / scaleFactor) - offsetY;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isMultiTouchGesturing = false;
                touchStartX = touchX;
                touchStartY = touchY;
                lastMoveX = touchX;
                lastMoveY = touchY;
                snapAnchorX = touchX;
                snapAnchorY = touchY;
                isSnapShapeTriggered = false;
                currentSnappedType = SnappedType.NONE;

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

                if (currentToolMode == ToolMode.PEN || currentToolMode == ToolMode.HIGHLIGHTER) {
                    snapShapeHandler.postDelayed(snapShapeRunnable, 450);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float distFromAnchor = (float) Math.hypot(touchX - snapAnchorX, touchY - snapAnchorY);
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

                if (distFromAnchor > 35f && !isSnapShapeTriggered) {
                    snapAnchorX = touchX;
                    snapAnchorY = touchY;
                    snapShapeHandler.removeCallbacks(snapShapeRunnable);
                    if (currentToolMode == ToolMode.PEN || currentToolMode == ToolMode.HIGHLIGHTER) {
                        snapShapeHandler.postDelayed(snapShapeRunnable, 450);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                snapShapeHandler.removeCallbacks(snapShapeRunnable);
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
        }

        invalidate();
        return true;
    }

    private void attemptSnapToShape() {
        if (activePoints == null || activePoints.size() < 4 || activePath == null) return;
        if (currentToolMode != ToolMode.PEN && currentToolMode != ToolMode.HIGHLIGHTER) return;

        Point startP = activePoints.get(0);
        Point endP = activePoints.get(activePoints.size() - 1);

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float totalLength = 0f;
        for (int i = 0; i < activePoints.size(); i++) {
            Point p = activePoints.get(i);
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
            if (i > 0) {
                Point prev = activePoints.get(i - 1);
                totalLength += (float) Math.hypot(p.x - prev.x, p.y - prev.y);
            }
        }

        float directDist = (float) Math.hypot(endP.x - startP.x, endP.y - startP.y);
        float width = maxX - minX;
        float height = maxY - minY;
        float closeDistance = (float) Math.hypot(endP.x - startP.x, endP.y - startP.y);

        if (totalLength > 30f && (directDist / totalLength) > 0.76f) {
            activePath.reset();
            activePath.moveTo(startP.x, startP.y);
            activePath.lineTo(endP.x, endP.y);
            isSnapShapeTriggered = true;
            currentSnappedType = SnappedType.LINE;

            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            invalidate();
            return;
        }

        float maxDimension = Math.max(width, height);
        if ((closeDistance < (maxDimension * 0.45f) || closeDistance < 140f) && width > 25f && height > 25f) {
            snappedMinX = minX;
            snappedMinY = minY;
            snappedMaxX = maxX;
            snappedMaxY = maxY;

            float ratio = width / height;
            if (ratio >= 0.65f && ratio <= 1.55f) {
                activePath.reset();
                activePath.addOval(new RectF(minX, minY, maxX, maxY), Path.Direction.CW);
                currentSnappedType = SnappedType.CIRCLE;
            } else {
                activePath.reset();
                activePath.addRect(new RectF(minX, minY, maxX, maxY), Path.Direction.CW);
                currentSnappedType = SnappedType.RECTANGLE;
            }

            isSnapShapeTriggered = true;
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            invalidate();
        }
    }

    private void handleScrollTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int pIndex = event.getActionIndex();
                lastTouchX = event.getX(pIndex);
                lastTouchY = event.getY(pIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleGestureDetector.isInProgress()) {
                    float curX = event.getX();
                    float curY = event.getY();
                    float dx = (curX - lastTouchX) / scaleFactor;
                    float dy = (curY - lastTouchY) / scaleFactor;

                    offsetX += dx;
                    offsetY += dy;

                    clampOffsets();

                    lastTouchX = curX;
                    lastTouchY = curY;
                    invalidate();
                }
                break;
        }
    }

    private void handleSelectDown(float x, float y) {
        selectedGroup.clear();
        groupBounds.setEmpty();

        if (selectedItem != null) {
            if (menuDeleteBounds.contains(x, y)) {
                deleteSingleSelectedItem(selectedItem);
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

            if (selectedItem instanceof ShapeItem) {
                ShapeItem s = (ShapeItem) selectedItem;
                if (s.getResizeHandle().contains(x, y)) {
                    isResizingObject = true;
                    return;
                }
            } else if (selectedItem instanceof ImageItem) {
                ImageItem img = (ImageItem) selectedItem;
                if (img.getResizeHandle().contains(x, y)) {
                    isResizingObject = true;
                    return;
                }
            } else if (selectedItem instanceof TableItem) {
                TableItem tbl = (TableItem) selectedItem;
                if (tbl.getResizeHandle(textPaint).contains(x, y)) {
                    isResizingObject = true;
                    return;
                }
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

        for (int i = tables.size() - 1; i >= 0; i--) {
            TableItem tbl = tables.get(i);
            if (tbl.getBounds(textPaint).contains(x, y)) {
                selectedItem = tbl;
                isDraggingObject = true;
                dragOffsetX = x - tbl.startX;
                dragOffsetY = y - tbl.startY;
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

    private void deleteSingleSelectedItem(Object item) {
        if (item == null) return;
        if (item instanceof ImageItem) images.remove((ImageItem) item);
        else if (item instanceof TextItem) texts.remove((TextItem) item);
        else if (item instanceof ShapeItem) shapes.remove((ShapeItem) item);
        else if (item instanceof TableItem) tables.remove((TableItem) item);

        redoStack.clear();
        historyStack.add(new DeleteAction(item));
    }

    private void handleSelectMove(float x, float y) {
        if (isResizingObject && selectedItem != null) {
            if (selectedItem instanceof ShapeItem) {
                ShapeItem s = (ShapeItem) selectedItem;
                s.endX = x;
                s.endY = y;
                invalidate();
            } else if (selectedItem instanceof ImageItem) {
                ImageItem img = (ImageItem) selectedItem;
                float newW = Math.max(80f, x - img.x);
                float ratio = (float) img.bitmap.getHeight() / (float) img.bitmap.getWidth();
                img.width = newW;
                img.height = newW * ratio;
                invalidate();
            } else if (selectedItem instanceof TableItem) {
                TableItem tbl = (TableItem) selectedItem;
                float newTotalW = Math.max(120f * tbl.cols, x - tbl.startX);
                float newTotalH = Math.max(40f * tbl.rows, y - tbl.startY);

                tbl.defaultCellWidth = newTotalW / tbl.cols;
                tbl.cellHeight = newTotalH / tbl.rows;
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
            } else if (selectedItem instanceof TableItem) {
                TableItem tbl = (TableItem) selectedItem;
                tbl.startX = x - dragOffsetX;
                tbl.startY = y - dragOffsetY;
            } else if (selectedItem instanceof TextItem) {
                TextItem t = (TextItem) selectedItem;
                t.x = x - dragOffsetX;
                t.y = y - dragOffsetY;
            }
            invalidate();
        }
    }

    private void handleSelectUp() {
        if (isDraggingObject || isResizingObject) {
            isDraggingObject = false;
            isResizingObject = false;
            notifyChange();
        }
    }

    private void duplicateSelectedGroup() {
        if (selectedGroup.isEmpty()) return;

        List<Object> newClones = new ArrayList<>();
        float offset = 40f;

        for (Object obj : selectedGroup) {
            if (obj instanceof StrokeItem) {
                StrokeItem orig = (StrokeItem) obj;
                Path newPath = new Path(orig.path);
                newPath.offset(offset, offset);

                List<Point> newPoints = new ArrayList<>();
                for (Point p : orig.points) {
                    newPoints.add(new Point(p.x + offset, p.y + offset));
                }

                StrokeItem clone = new StrokeItem(newPath, new Paint(orig.paint), newPoints, orig.color, orig.strokeWidth, orig.isEraser);
                strokes.add(clone);
                historyStack.add(clone);
                newClones.add(clone);

            } else if (obj instanceof ShapeItem) {
                ShapeItem orig = (ShapeItem) obj;
                ShapeItem clone = new ShapeItem(orig.shapeType, orig.startX + offset, orig.startY + offset, orig.endX + offset, orig.endY + offset, orig.color, orig.strokeWidth);
                shapes.add(clone);
                historyStack.add(clone);
                newClones.add(clone);

            } else if (obj instanceof TextItem) {
                TextItem orig = (TextItem) obj;
                TextItem clone = new TextItem(orig.x + offset, orig.y + offset, orig.text, orig.color, orig.textSize);
                texts.add(clone);
                historyStack.add(clone);
                newClones.add(clone);

            } else if (obj instanceof ImageItem) {
                ImageItem orig = (ImageItem) obj;
                ImageItem clone = new ImageItem(orig.x + offset, orig.y + offset, orig.width, orig.height, orig.bitmap, orig.uriStr);
                images.add(clone);
                historyStack.add(clone);
                newClones.add(clone);

            } else if (obj instanceof TableItem) {
                TableItem orig = (TableItem) obj;
                TableItem clone = new TableItem(orig.startX + offset, orig.startY + offset, orig.rows, orig.cols);
                clone.defaultCellWidth = orig.defaultCellWidth;
                clone.cellHeight = orig.cellHeight;
                for (TableCell c : orig.cells) {
                    clone.cells.add(new TableCell(c.row, c.col, c.text));
                }
                tables.add(clone);
                historyStack.add(clone);
                newClones.add(clone);
            }
        }

        selectedGroup.clear();
        selectedGroup.addAll(newClones);
        calculateGroupBounds();
        notifyChange();
        invalidate();
    }

    private void handleLassoDown(float x, float y) {
        selectedItem = null;

        if (!selectedGroup.isEmpty()) {
            if (menuCopyBounds.contains(x, y)) {
                duplicateSelectedGroup();
                return;
            }

            if (menuDeleteBounds.contains(x, y)) {
                List<Object> itemsToDelete = new ArrayList<>(selectedGroup);
                for (Object obj : itemsToDelete) {
                    if (obj instanceof StrokeItem) strokes.remove((StrokeItem) obj);
                    else if (obj instanceof ShapeItem) shapes.remove((ShapeItem) obj);
                    else if (obj instanceof ImageItem) images.remove((ImageItem) obj);
                    else if (obj instanceof TextItem) texts.remove((TextItem) obj);
                    else if (obj instanceof TableItem) tables.remove((TableItem) obj);
                }
                redoStack.clear();
                historyStack.add(new DeleteAction(itemsToDelete));

                selectedGroup.clear();
                groupBounds.setEmpty();
                notifyChange();
                invalidate();
                return;
            }

            if (getGroupResizeHandle().contains(x, y)) {
                isResizingGroup = true;
                groupDragStartX = x;
                groupDragStartY = y;
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
        if (isResizingGroup && !selectedGroup.isEmpty()) {
            float oldW = groupBounds.width();
            float oldH = groupBounds.height();
            if (oldW > 10f && oldH > 10f) {
                float scaleX = (x - groupBounds.left) / oldW;
                float scaleY = (y - groupBounds.top) / oldH;
                float factor = Math.max(0.2f, Math.min(scaleX, scaleY));

                float pivotX = groupBounds.left;
                float pivotY = groupBounds.top;

                for (Object obj : selectedGroup) {
                    if (obj instanceof StrokeItem) {
                        StrokeItem stroke = (StrokeItem) obj;
                        for (Point p : stroke.points) {
                            p.x = pivotX + (p.x - pivotX) * factor;
                            p.y = pivotY + (p.y - pivotY) * factor;
                        }
                        Path newPath = new Path();
                        for (int i = 0; i < stroke.points.size(); i++) {
                            Point p = stroke.points.get(i);
                            if (i == 0) newPath.moveTo(p.x, p.y);
                            else newPath.lineTo(p.x, p.y);
                        }
                        stroke.path = newPath;
                    } else if (obj instanceof ShapeItem) {
                        ShapeItem s = (ShapeItem) obj;
                        s.startX = pivotX + (s.startX - pivotX) * factor;
                        s.startY = pivotY + (s.startY - pivotY) * factor;
                        s.endX = pivotX + (s.endX - pivotX) * factor;
                        s.endY = pivotY + (s.endY - pivotY) * factor;
                    } else if (obj instanceof ImageItem) {
                        ImageItem img = (ImageItem) obj;
                        img.x = pivotX + (img.x - pivotX) * factor;
                        img.y = pivotY + (img.y - pivotY) * factor;
                        img.width *= factor;
                        img.height *= factor;
                    } else if (obj instanceof TableItem) {
                        TableItem tbl = (TableItem) obj;
                        tbl.startX = pivotX + (tbl.startX - pivotX) * factor;
                        tbl.startY = pivotY + (tbl.startY - pivotY) * factor;
                        tbl.defaultCellWidth *= factor;
                        tbl.cellHeight *= factor;
                    }
                }
                calculateGroupBounds();
            }
            invalidate();
            return;
        }

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
        if (isDraggingGroup || isResizingGroup) {
            isDraggingGroup = false;
            isResizingGroup = false;
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

    private float calculateContentBottomY() {
        float maxBottom = getHeight() > 0 ? getHeight() : 1920f;

        for (StrokeItem s : strokes) {
            for (Point p : s.points) {
                if (p.y > maxBottom) maxBottom = p.y;
            }
        }
        for (ShapeItem s : shapes) {
            RectF b = s.getBounds();
            if (b.bottom > maxBottom) maxBottom = b.bottom;
        }
        for (TableItem t : tables) {
            RectF b = t.getBounds(textPaint);
            if (b.bottom > maxBottom) maxBottom = b.bottom;
        }
        for (ImageItem img : images) {
            RectF b = img.getBounds();
            if (b.bottom > maxBottom) maxBottom = b.bottom;
        }
        for (TextItem txt : texts) {
            RectF b = txt.getBounds(freeTextPaint);
            if (b.bottom > maxBottom) maxBottom = b.bottom;
        }

        return maxBottom;
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

        } else if (currentToolMode == ToolMode.CIRCLE) {
            activePath.reset();
            float left = Math.min(touchStartX, x);
            float top = Math.min(touchStartY, y);
            float right = Math.max(touchStartX, x);
            float bottom = Math.max(touchStartY, y);
            activePath.addOval(new RectF(left, top, right, bottom), Path.Direction.CW);

        } else if (currentToolMode == ToolMode.LINE) {
            activePath.reset();
            activePath.moveTo(touchStartX, touchStartY);
            activePath.lineTo(x, y);

        } else {
            if (isSnapShapeTriggered) {
                if (currentSnappedType == SnappedType.LINE && activePoints != null && !activePoints.isEmpty()) {
                    Point startP = activePoints.get(0);
                    activePath.reset();
                    activePath.moveTo(startP.x, startP.y);
                    activePath.lineTo(x, y);
                } else if (currentSnappedType == SnappedType.CIRCLE || currentSnappedType == SnappedType.RECTANGLE) {
                    float curMinX = Math.min(snappedMinX, x);
                    float curMinY = Math.min(snappedMinY, y);
                    float curMaxX = Math.max(snappedMaxX, x);
                    float curMaxY = Math.max(snappedMaxY, y);
                    activePath.reset();
                    if (currentSnappedType == SnappedType.CIRCLE) {
                        activePath.addOval(new RectF(curMinX, curMinY, curMaxX, curMaxY), Path.Direction.CW);
                    } else {
                        activePath.addRect(new RectF(curMinX, curMinY, curMaxX, curMaxY), Path.Direction.CW);
                    }
                }
            } else {
                activePath.lineTo(x, y);
                activePoints.add(new Point(x, y));
            }
        }
    }

    private void finishStroke() {
        if (activePath == null) return;

        if (currentToolMode == ToolMode.ERASER) {
            isErasing = false;
            eraserX = -1f;
            eraserY = -1f;
        }

        redoStack.clear();

        boolean isShape = (currentToolMode == ToolMode.RECTANGLE ||
                currentToolMode == ToolMode.SQUARE ||
                currentToolMode == ToolMode.CIRCLE ||
                currentToolMode == ToolMode.LINE);

        if (isShape) {
            ShapeItem newShape = new ShapeItem(currentToolMode, touchStartX, touchStartY, lastMoveX, lastMoveY, currentColor, currentStrokeWidth);
            shapes.add(newShape);
            historyStack.add(newShape);
        } else {
            boolean isEraser = (currentToolMode == ToolMode.ERASER);
            StrokeItem newStroke = new StrokeItem(
                    activePath,
                    new Paint(activePaint),
                    activePoints != null ? activePoints : new ArrayList<>(),
                    activePaint.getColor(),
                    activePaint.getStrokeWidth(),
                    isEraser
            );
            strokes.add(newStroke);
            historyStack.add(newStroke);
        }

        activePath = null;
        activePaint = null;
        activePoints = null;
        isSnapShapeTriggered = false;
        currentSnappedType = SnappedType.NONE;

        notifyChange();
    }

    private void notifyChange() {
        if (onDrawingChangeListener != null) {
            onDrawingChangeListener.onDrawingChanged(getDrawingJson());
        }
    }

    // =========================================================================
    // 6. KAMUYA AÇIK KONTROL METOTLARI
    // =========================================================================

    public void setPageGridMode(PageGridMode mode) {
        this.currentPageGridMode = mode;
        invalidate();
    }

    public PageGridMode getPageGridMode() {
        return this.currentPageGridMode;
    }

    public void setToolMode(ToolMode mode) {
        this.currentToolMode = mode;
        this.selectedItem = null;
        this.selectedGroup.clear();
        this.groupBounds.setEmpty();
        this.lassoPath = null;
        this.activePath = null;
        invalidate();
    }

    public ToolMode getCurrentToolMode() {
        return this.currentToolMode;
    }

    public void setColor(int color) {
        this.currentColor = color;
    }

    public void setStrokeWidth(float width) {
        this.currentStrokeWidth = width;
    }

    public void undo() {
        if (historyStack.isEmpty()) return;

        Object lastAction = historyStack.remove(historyStack.size() - 1);
        redoStack.add(lastAction);

        if (lastAction instanceof DeleteAction) {
            DeleteAction da = (DeleteAction) lastAction;
            for (Object item : da.deletedItems) {
                if (item instanceof StrokeItem && !strokes.contains(item)) strokes.add((StrokeItem) item);
                else if (item instanceof ShapeItem && !shapes.contains(item)) shapes.add((ShapeItem) item);
                else if (item instanceof TableItem && !tables.contains(item)) tables.add((TableItem) item);
                else if (item instanceof ImageItem && !images.contains(item)) images.add((ImageItem) item);
                else if (item instanceof TextItem && !texts.contains(item)) texts.add((TextItem) item);
            }
        } else if (lastAction instanceof ShapeItem) {
            shapes.remove(lastAction);
        } else if (lastAction instanceof StrokeItem) {
            strokes.remove(lastAction);
        } else if (lastAction instanceof TableItem) {
            tables.remove(lastAction);
        } else if (lastAction instanceof ImageItem) {
            images.remove(lastAction);
        } else if (lastAction instanceof TextItem) {
            texts.remove(lastAction);
        }

        selectedItem = null;
        selectedGroup.clear();
        groupBounds.setEmpty();

        notifyChange();
        invalidate();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        Object actionToRedo = redoStack.remove(redoStack.size() - 1);
        historyStack.add(actionToRedo);

        if (actionToRedo instanceof DeleteAction) {
            DeleteAction da = (DeleteAction) actionToRedo;
            for (Object item : da.deletedItems) {
                if (item instanceof StrokeItem) strokes.remove((StrokeItem) item);
                else if (item instanceof ShapeItem) shapes.remove((ShapeItem) item);
                else if (item instanceof TableItem) tables.remove((TableItem) item);
                else if (item instanceof ImageItem) images.remove((ImageItem) item);
                else if (item instanceof TextItem) texts.remove((TextItem) item);
            }
        } else if (actionToRedo instanceof ShapeItem) {
            shapes.add((ShapeItem) actionToRedo);
        } else if (actionToRedo instanceof StrokeItem) {
            strokes.add((StrokeItem) actionToRedo);
        } else if (actionToRedo instanceof TableItem) {
            tables.add((TableItem) actionToRedo);
        } else if (actionToRedo instanceof ImageItem) {
            images.add((ImageItem) actionToRedo);
        } else if (actionToRedo instanceof TextItem) {
            texts.add((TextItem) actionToRedo);
        }

        notifyChange();
        invalidate();
    }

    public void clearCanvas() {
        strokes.clear();
        shapes.clear();
        images.clear();
        texts.clear();
        tables.clear();
        historyStack.clear();
        redoStack.clear();
        selectedItem = null;
        selectedGroup.clear();
        groupBounds.setEmpty();
        lassoPath = null;
        activePath = null;
        offsetX = 0f;
        offsetY = 0f;
        scaleFactor = 1.0f;
        editingTextItem = null;
        editingTableCell = null;
        notifyChange();
        invalidate();
    }

    public void addImageToCanvas(Bitmap bitmap, String uriStr) {
        if (bitmap == null) return;
        float startX = 80f - offsetX;
        float startY = -offsetY + 150f;
        float targetWidth = 400f;
        float ratio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
        ImageItem item = new ImageItem(startX, startY, targetWidth, targetWidth * ratio, bitmap, uriStr);
        images.add(item);
        historyStack.add(item);
        redoStack.clear();
        selectedItem = item;
        currentToolMode = ToolMode.SELECT;
        notifyChange();
        invalidate();
    }

    public void addTextToCanvas(float x, float y, String text, int color) {
        if (text == null || text.trim().isEmpty()) return;
        TextItem t = new TextItem(x, y, text, color, 36f);
        texts.add(t);
        historyStack.add(t);
        redoStack.clear();
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
        deleteSingleSelectedItem(item);
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
        float startX = 60f - offsetX;
        float startY = -offsetY + 140f;
        addTableToCanvas(startX, startY, rows, cols);
    }

    public void addTableToCanvas(float x, float y, int rows, int cols) {
        TableItem table = new TableItem(x, y, rows, cols);
        tables.add(table);
        historyStack.add(table);
        redoStack.clear();
        selectedItem = table;
        currentToolMode = ToolMode.SELECT;
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

    public float getScaleFactor() {
        return scaleFactor;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void zoomIn() {
        scaleFactor = Math.min(MAX_SCALE, scaleFactor + 0.25f);
        clampOffsets();
        invalidate();
    }

    public void zoomOut() {
        scaleFactor = Math.max(MIN_SCALE, scaleFactor - 0.25f);
        clampOffsets();
        invalidate();
    }

    // =========================================================================
    // 7. JSON SERİLEŞTİRME
    // =========================================================================

    public String getDrawingJson() {
        try {
            JSONObject mainObj = new JSONObject();
            mainObj.put("pageMode", currentPageGridMode.name());
            mainObj.put("canvasTheme", currentCanvasTheme.name());
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
            tableObj.put("defaultCellWidth", table.defaultCellWidth);
            tableObj.put("cellHeight", table.cellHeight);

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
            historyStack.clear();
            redoStack.clear();

            if (!jsonStr.startsWith("{")) {
                parseStrokes(new JSONArray(jsonStr));
                return;
            }

            JSONObject mainObj = new JSONObject(jsonStr);
            if (mainObj.has("pageMode")) {
                try {
                    this.currentPageGridMode = PageGridMode.valueOf(mainObj.getString("pageMode"));
                } catch (Exception ignored) {}
            }

            if (mainObj.has("canvasTheme")) {
                try {
                    this.currentCanvasTheme = CanvasTheme.valueOf(mainObj.getString("canvasTheme"));
                } catch (Exception ignored) {}
            }

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
                    ShapeItem s = new ShapeItem(type, sx, sy, ex, ey, color, width);
                    shapes.add(s);
                    historyStack.add(s);
                }
            }

            if (mainObj.has("tables")) {
                JSONArray tablesArray = mainObj.getJSONArray("tables");
                for (int i = 0; i < tablesArray.length(); i++) {
                    JSONObject obj = tablesArray.getJSONObject(i);
                    TableItem table = new TableItem((float) obj.getDouble("startX"), (float) obj.getDouble("startY"), obj.getInt("rows"), obj.getInt("cols"));
                    if (obj.has("defaultCellWidth")) {
                        table.defaultCellWidth = (float) obj.getDouble("defaultCellWidth");
                    }
                    if (obj.has("cellHeight")) {
                        table.cellHeight = (float) obj.getDouble("cellHeight");
                    }
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
            StrokeItem stroke = new StrokeItem(path, paint, points, color, width, isEraser);
            strokes.add(stroke);
            historyStack.add(stroke);
        }
    }

    public Bitmap exportThumbnail(int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            targetWidth = 600;
            targetHeight = 200;
        }

        int viewWidth = getWidth() > 0 ? getWidth() : 1080;
        int viewHeight = getHeight() > 0 ? getHeight() : 1920;

        Bitmap fullBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas fullCanvas = new Canvas(fullBitmap);
        fullCanvas.drawColor(Color.TRANSPARENT);
        draw(fullCanvas);

        int minX = viewWidth, minY = viewHeight, maxX = 0, maxY = 0;
        boolean hasDrawing = false;

        int[] pixels = new int[viewWidth * viewHeight];
        fullBitmap.getPixels(pixels, 0, viewWidth, 0, 0, viewWidth, viewHeight);

        for (int y = 0; y < viewHeight; y += 4) {
            for (int x = 0; x < viewWidth; x += 4) {
                int alpha = Color.alpha(pixels[y * viewWidth + x]);
                if (alpha > 20) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                    hasDrawing = true;
                }
            }
        }

        if (!hasDrawing || minX >= maxX || minY >= maxY) {
            return null;
        }

        int padding = 30;
        minX = Math.max(0, minX - padding);
        minY = Math.max(0, minY - padding);
        maxX = Math.min(viewWidth, maxX + padding);
        maxY = Math.min(viewHeight, maxY + padding);

        int cropWidth = maxX - minX;
        int cropHeight = maxY - minY;

        Bitmap croppedBitmap = Bitmap.createBitmap(fullBitmap, minX, minY, cropWidth, cropHeight);
        return Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true);
    }

    public String getAllTextContent() {
        if (texts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (TextItem item : texts) {
            if (item.text != null && !item.text.trim().isEmpty()) {
                sb.append(item.text.trim()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    public void exportToPdf(Context context, String title) {
        if (context == null) return;

        int viewWidth = getWidth() > 0 ? getWidth() : 1080;
        int viewHeight = getHeight() > 0 ? getHeight() : 1920;

        android.graphics.pdf.PdfDocument pdfDocument = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create();
        android.graphics.pdf.PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas pdfCanvas = page.getCanvas();
        pdfCanvas.drawColor(currentCanvasTheme.bgColor);
        draw(pdfCanvas);

        pdfDocument.finishPage(page);

        String fileName = (title != null && !title.trim().isEmpty() ? title.trim() : "Not") + "_" + System.currentTimeMillis() + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    java.io.OutputStream os = context.getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        pdfDocument.writeTo(os);
                        os.close();
                    }
                    Toast.makeText(context, "PDF İndirilenler klasörüne kaydedildi", Toast.LENGTH_LONG).show();
                }
            } else {
                java.io.File file = new java.io.File(
                        android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                        fileName
                );
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                pdfDocument.writeTo(fos);
                fos.close();
                Toast.makeText(context, "PDF kaydedildi: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "PDF kaydedilirken hata", e);
            Toast.makeText(context, "PDF kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}