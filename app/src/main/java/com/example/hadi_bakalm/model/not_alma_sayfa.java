package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.data.not_app_database;
import com.example.hadi_bakalm.data.notdao;
import com.example.hadi_bakalm.data.notentity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SpellCheckingInspection")
public class not_alma_sayfa extends AppCompatActivity {

    private static final String TAG = "not_alma_sayfa";
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR"));
    private static final SimpleDateFormat EXPIRY_FORMAT = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr", "TR"));
    private static final String PREF_IS_ADVANCED_MODE = "pref_is_advanced_mode";

    private ImageButton btnCloseEditor;
    private ImageButton btnPinNote;
    private ImageButton btnLockCanvas;
    private LinearLayout containerEphemeralBadge;
    private TextView tvEphemeralBadge;
    private EditText etNoteTitle;

    private ImageButton btnShareNote;
    private ImageButton btnSaveNote;
    private ImageButton btnMoreOptions;

    // Sayfa Çizgi Modu Butonları
    private ImageButton btnBlankPageToggle;
    private ImageButton btnGridToggle;
    private ImageButton btnHorizontalLinesToggle;
    private ImageButton btnVerticalLinesToggle;
    private ImageButton btnClearCanvas;

    private DrawingView globalDrawingCanvas;
    private EditText inlineTextEditor;
    private DrawingView.TextItem activeEditingTextObj = null;
    private DrawingView.TableCellClickResult activeEditingTableCell = null;

    private FrameLayout frameToolPen;
    private FrameLayout frameToolHighlighter;
    private FrameLayout frameToolText;
    private TextView btnToolText;
    private ImageView colorBlack, colorBlue, colorRed, colorGreen;

    private ImageButton btnToolHand;
    private ImageButton btnToolPen;
    private ImageButton btnToolHighlighter;
    private ImageButton btnToolEraser;
    private ImageButton btnToolSelect;
    private ImageButton btnToolLasso;

    private boolean isSaving = false;
    private boolean isPinned = false;
    private boolean isCanvasLocked = false;

    private boolean inVault = false;
    private int currentNoteId = -1;

    private int currentPenColor = 0xFF09090B;   // Çizim kalemi rengi (Mavi, Kırmızı vb.)
    private int currentTextColor = 0xFF0F172A;  // Metin yazı rengi (Varsayılan koyu/siyah)

    private boolean isEphemeral = false;
    private long expireTimestamp = 0L;
    private long tempSelectedExpireTimestamp = 0L;

    private float currentStrokeWidth = 8f;
    private DrawingView.ToolMode activeMode = DrawingView.ToolMode.PEN;

    private String currentCategory = "Kişisel";
    private notdao noteDao;

    private ActivityResultLauncher<String> imagePickerLauncher;

    private float pendingNewTextX = 0f;
    private float pendingNewTextY = 0f;
    private boolean isCreatingNewText = false;

    private final Handler liveTimerHandler = new Handler(Looper.getMainLooper());
    private final Runnable liveTimerRunnable = new Runnable() {
        @Override
        public void run() {
            updateLiveBadgeUI();
            liveTimerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();

        setupImagePicker();
        initViews();
        setupClickListeners();
        setupEditorModeToggle();
        setupCanvasTouchListener();
        setupInlineEditorListener();
        loadInitialIntentData();

        // Klavye açıldığında alt araç çubuğunu klavyenin tam üstüne kaydırır
        View footerToolbar = findViewById(R.id.editorFooterToolbar);
        if (footerToolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(footerToolbar, (v, insets) -> {
                int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                v.setTranslationY(-imeHeight);
                return insets;
            });
        }

        selectColor(0xFF09090B, colorBlack);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveNoteAndExit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        liveTimerHandler.post(liveTimerRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        liveTimerHandler.removeCallbacks(liveTimerRunnable);
        commitInlineText();
        autoSaveNote();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        loadBitmapFromUri(uri);
                    }
                }
        );
    }

    private void setupEditorModeToggle() {
        View layoutAdvancedToolsRow = findViewById(R.id.layoutAdvancedToolsRow);
        View containerBasicColors = findViewById(R.id.containerBasicColors);
        ImageButton btnToggleEditorMode = findViewById(R.id.btnToggleEditorMode);

        if (btnToggleEditorMode == null || layoutAdvancedToolsRow == null) return;

        SharedPreferences prefs = getSharedPreferences("NoteEditorPrefs", MODE_PRIVATE);
        boolean isAdvanced = prefs.getBoolean(PREF_IS_ADVANCED_MODE, false);

        applyEditorMode(isAdvanced, layoutAdvancedToolsRow, containerBasicColors, btnToggleEditorMode);

        btnToggleEditorMode.setOnClickListener(v -> {
            boolean newMode = (layoutAdvancedToolsRow.getVisibility() != View.VISIBLE);
            prefs.edit().putBoolean(PREF_IS_ADVANCED_MODE, newMode).apply();
            applyEditorMode(newMode, layoutAdvancedToolsRow, containerBasicColors, btnToggleEditorMode);
        });
    }

    private void makeSelectedTextBold(EditText editText) {
        if (editText == null) return;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();

        if (start >= 0 && end > start) {
            Spannable spannable = editText.getText();
            StyleSpan[] spans = spannable.getSpans(start, end, StyleSpan.class);
            boolean isAlreadyBold = false;

            for (StyleSpan span : spans) {
                if (span.getStyle() == Typeface.BOLD) {
                    spannable.removeSpan(span);
                    isAlreadyBold = true;
                }
            }

            if (!isAlreadyBold) {
                spannable.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }

    private void applyTextColorToSelection(int color) {
        // 1. Durum: Klavye açık ve metin içinde imleçle seçim yapılmışsa
        if (inlineTextEditor != null && inlineTextEditor.getVisibility() == View.VISIBLE) {
            int start = inlineTextEditor.getSelectionStart();
            int end = inlineTextEditor.getSelectionEnd();
            if (start >= 0 && end > start) {
                Spannable spannable = inlineTextEditor.getText();
                spannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return;
            }
        }

        // 2. Durum: Klavye kapalıyken metne tek tıklanıp alttan A butonuna basılmışsa
        if (lastClickedTextObj != null && globalDrawingCanvas != null) {
            SpannableString sp = new SpannableString(lastClickedTextObj.text);
            sp.setSpan(new ForegroundColorSpan(color), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            globalDrawingCanvas.updateTextObject(lastClickedTextObj, sp);
        }
    }

    private void applyHighlightColorToSelection(int highlightColor) {
        // 1. Durum: Klavye açıkken imleçle seçim
        if (inlineTextEditor != null && inlineTextEditor.getVisibility() == View.VISIBLE) {
            int start = inlineTextEditor.getSelectionStart();
            int end = inlineTextEditor.getSelectionEnd();
            if (start >= 0 && end > start) {
                Spannable spannable = inlineTextEditor.getText();
                BackgroundColorSpan[] spans = spannable.getSpans(start, end, BackgroundColorSpan.class);
                boolean hasHighlight = false;
                for (BackgroundColorSpan span : spans) {
                    spannable.removeSpan(span);
                    hasHighlight = true;
                }
                if (!hasHighlight) {
                    spannable.setSpan(new BackgroundColorSpan(highlightColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                return;
            }
        }

        // 2. Durum: Klavye kapalıyken tek tıkla seçilen metni komple vurgulama/vurguyu kaldırma
        if (lastClickedTextObj != null && globalDrawingCanvas != null) {
            SpannableString sp = new SpannableString(lastClickedTextObj.text);
            BackgroundColorSpan[] spans = sp.getSpans(0, sp.length(), BackgroundColorSpan.class);
            if (spans.length > 0) {
                for (BackgroundColorSpan span : spans) sp.removeSpan(span);
            } else {
                sp.setSpan(new BackgroundColorSpan(highlightColor), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            globalDrawingCanvas.updateTextObject(lastClickedTextObj, sp);
        }
    }

    private void applyEditorMode(boolean isAdvanced, View advancedRow, View basicColors, ImageButton toggleBtn) {
        if (isAdvanced) {
            advancedRow.setVisibility(View.VISIBLE);
            if (basicColors != null) basicColors.setVisibility(View.INVISIBLE);
            toggleBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0F2FE));
            toggleBtn.setColorFilter(0xFF0284C7);
        } else {
            advancedRow.setVisibility(View.GONE);
            if (basicColors != null) basicColors.setVisibility(View.VISIBLE);
            toggleBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF1F5F9));
            toggleBtn.setColorFilter(0xFF64748B);
        }
    }

    private void scheduleNoteDeletionWarning(String notBaslik, long silinmeZamaniMillis) {
        long simdikiZaman = System.currentTimeMillis();
        long toplamSureMillis = silinmeZamaniMillis - simdikiZaman;

        if (toplamSureMillis <= 60 * 1000L) {
            return;
        }

        long bildirimGecikmesiMillis;
        String uyariMetni;

        if (toplamSureMillis > 15 * 60 * 1000L) {
            bildirimGecikmesiMillis = toplamSureMillis - (10 * 60 * 1000L);
            uyariMetni = "\"" + notBaslik + "\" başlıklı notunuz yaklaşık 10 dakika içinde silinecektir.";
        } else if (toplamSureMillis >= 5 * 60 * 1000L) {
            long kalanDakika = Math.max(1, (toplamSureMillis / (2 * 60 * 1000L)));
            bildirimGecikmesiMillis = toplamSureMillis - (kalanDakika * 60 * 1000L);
            uyariMetni = "\"" + notBaslik + "\" başlıklı notunuz yaklaşık " + kalanDakika + " dakika içinde silinecektir.";
        } else {
            bildirimGecikmesiMillis = toplamSureMillis - (60 * 1000L);
            uyariMetni = "\"" + notBaslik + "\" başlıklı notunuz 1 dakika içinde silinecektir.";
        }

        if (bildirimGecikmesiMillis > 0) {
            Data inputData = new Data.Builder()
                    .putString("not_baslik", notBaslik)
                    .putString("uyari_metni", uyariMetni)
                    .build();

            OneTimeWorkRequest warningRequest = new OneTimeWorkRequest.Builder(NoteWarningWorker.class)
                    .setInitialDelay(bildirimGecikmesiMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag("gecici_not_uyari")
                    .build();

            WorkManager.getInstance(this).enqueue(warningRequest);
        }
    }

    private void loadBitmapFromUri(Uri uri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }

            if (globalDrawingCanvas != null && bitmap != null) {
                globalDrawingCanvas.addImageToCanvas(bitmap, uri.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Görsel yüklenirken hata oluştu", e);
        }
    }

    private void initViews() {
        btnCloseEditor = findViewById(R.id.btnCloseEditor);
        btnPinNote = findViewById(R.id.btnPinNote);
        btnLockCanvas = findViewById(R.id.btnLockCanvas);
        containerEphemeralBadge = findViewById(R.id.containerEphemeralBadge);
        tvEphemeralBadge = findViewById(R.id.tvEphemeralBadge);
        etNoteTitle = findViewById(R.id.etNoteTitle);
        btnShareNote = findViewById(R.id.btnShareNote);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);

        colorBlack = findViewById(R.id.colorBlack);
        colorBlue = findViewById(R.id.colorBlue);
        colorRed = findViewById(R.id.colorRed);
        colorGreen = findViewById(R.id.colorGreen);

        btnBlankPageToggle = findViewById(R.id.btnBlankPageToggle);
        btnGridToggle = findViewById(R.id.btnGridToggle);
        btnHorizontalLinesToggle = findViewById(R.id.btnHorizontalLinesToggle);
        btnVerticalLinesToggle = findViewById(R.id.btnVerticalLinesToggle);

        globalDrawingCanvas = findViewById(R.id.globalDrawingCanvas);
        inlineTextEditor = findViewById(R.id.inlineTextEditor);

        if (inlineTextEditor != null) {
            inlineTextEditor.setPadding(0, 0, 0, 0);
            inlineTextEditor.setIncludeFontPadding(false);

            inlineTextEditor.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                private static final int MENU_BOLD_ID = 1001;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    menu.add(Menu.NONE, MENU_BOLD_ID, Menu.NONE, "Kalın");
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == MENU_BOLD_ID) {
                        makeSelectedTextBold(inlineTextEditor);
                        mode.finish();
                        return true;
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {}
            });
        }

        frameToolPen = findViewById(R.id.frameToolPen);
        frameToolHighlighter = findViewById(R.id.frameToolHighlighter);
        btnClearCanvas = findViewById(R.id.btnClearCanvas);
        frameToolText = findViewById(R.id.frameToolText);
        btnToolText = findViewById(R.id.btnToolText);

        btnToolHand = findViewById(R.id.btnToolHand);
        btnToolPen = findViewById(R.id.btnToolPen);
        btnToolHighlighter = findViewById(R.id.btnToolHighlighter);
        btnToolEraser = findViewById(R.id.btnToolEraser);
        btnToolSelect = findViewById(R.id.btnToolSelect);
        btnToolLasso = findViewById(R.id.btnToolLasso);
    }

    private void loadInitialIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        this.inVault = intent.getBooleanExtra("EXTRA_IN_VAULT", false);

        boolean incomingIsEphemeral = intent.getBooleanExtra("EXTRA_IS_EPHEMERAL", false);
        long incomingExpireTimestamp = intent.getLongExtra("EXTRA_EXPIRE_TIMESTAMP", 0L);

        if (incomingIsEphemeral && incomingExpireTimestamp > 0) {
            this.isEphemeral = true;
            this.expireTimestamp = incomingExpireTimestamp;
        }

        currentNoteId = intent.getIntExtra("EXTRA_NOTE_ID", -1);
        String incomingTitle = intent.getStringExtra("EXTRA_NOTE_TITLE");
        String incomingCategory = intent.getStringExtra("EXTRA_NOTE_CATEGORY");

        if (incomingCategory != null && !incomingCategory.trim().isEmpty()) {
            currentCategory = incomingCategory.trim();
        }

        if (incomingTitle != null && etNoteTitle != null) {
            etNoteTitle.setText(incomingTitle);
        }

        if (currentNoteId != -1 && noteDao != null) {
            DB_EXECUTOR.execute(() -> {
                notentity existingNote = noteDao.getNoteById(currentNoteId);
                if (existingNote != null) {
                    isPinned = existingNote.isPinned;
                    isEphemeral = existingNote.isEphemeral;
                    expireTimestamp = existingNote.expireTimestamp;
                    inVault = existingNote.inVault;

                    if (existingNote.category != null) {
                        currentCategory = existingNote.category;
                    }

                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;

                        if (btnPinNote != null) {
                            btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
                        }

                        updateLiveBadgeUI();

                        if (existingNote.blocks != null && globalDrawingCanvas != null) {
                            for (NoteBlockModel block : existingNote.blocks) {
                                if (block != null && block.getType() == NoteBlockModel.BlockType.DRAWING) {
                                    globalDrawingCanvas.loadDrawingFromJson(block.getContent());
                                    updatePageGridButtonsUI(globalDrawingCanvas.getPageGridMode());
                                }
                            }
                        }
                    });
                }
            });
        } else {
            updateLiveBadgeUI();
            updatePageGridButtonsUI(DrawingView.PageGridMode.BLANK);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateLiveBadgeUI() {
        if (!isEphemeral || expireTimestamp <= 0) {
            if (containerEphemeralBadge != null) containerEphemeralBadge.setVisibility(View.GONE);
            return;
        }

        long diff = expireTimestamp - System.currentTimeMillis();
        if (diff <= 0) {
            if (tvEphemeralBadge != null) tvEphemeralBadge.setText("Süresi Doldu");
            if (containerEphemeralBadge != null) containerEphemeralBadge.setVisibility(View.VISIBLE);
            return;
        }

        long totalSeconds = diff / 1000;
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String formattedTime;
        if (days > 0) {
            formattedTime = days + " gün " + hours + " sa";
        } else if (hours > 0) {
            formattedTime = hours + " sa " + minutes + " dk";
        } else if (minutes > 0) {
            formattedTime = minutes + " dk " + seconds + " sn";
        } else {
            formattedTime = seconds + " sn kaldı";
        }

        if (tvEphemeralBadge != null) tvEphemeralBadge.setText(formattedTime);
        if (containerEphemeralBadge != null) containerEphemeralBadge.setVisibility(View.VISIBLE);
    }

    private void setupInlineEditorListener() {
        if (inlineTextEditor == null) return;

        inlineTextEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (activeEditingTableCell != null && globalDrawingCanvas != null) {
                    globalDrawingCanvas.updateTableCellText(
                            activeEditingTableCell.table,
                            activeEditingTableCell.row,
                            activeEditingTableCell.col,
                            s.toString()
                    );
                    updateInlineTableCellPosition();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        inlineTextEditor.setOnEditorActionListener((v, actionId, event) -> {
            commitInlineText();
            return true;
        });
    }

    private void updateInlineTableCellPosition() {
        if (activeEditingTableCell == null || globalDrawingCanvas == null || inlineTextEditor == null) return;

        float scale = globalDrawingCanvas.getScaleFactor();
        Paint tempPaint = new Paint();
        tempPaint.setTextSize(32f);
        float[] colWidths = activeEditingTableCell.table.getColumnWidths(tempPaint);

        float cellX = activeEditingTableCell.table.startX;
        for (int c = 0; c < activeEditingTableCell.col; c++) {
            cellX += (c < colWidths.length ? colWidths[c] : activeEditingTableCell.table.defaultCellWidth);
        }
        float cellY = activeEditingTableCell.table.startY + (activeEditingTableCell.row * activeEditingTableCell.table.cellHeight);

        float screenX = (cellX + globalDrawingCanvas.getOffsetX()) * scale;
        float screenY = (cellY + globalDrawingCanvas.getOffsetY()) * scale;

        inlineTextEditor.setX(screenX);
        inlineTextEditor.setY(screenY);
    }

    private void openInlineTextEditor(float x, float y, DrawingView.TextItem textObj) {
        commitInlineText();
        activeEditingTableCell = null;

        if (globalDrawingCanvas == null) return;

        float currentScale = globalDrawingCanvas.getScaleFactor();
        float baseTextSize = (textObj != null && textObj.textSize > 0) ? textObj.textSize : 36f;

        if (textObj != null) {
            isCreatingNewText = false;
            activeEditingTextObj = textObj;
            inlineTextEditor.setText(textObj.text);
            inlineTextEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, baseTextSize * currentScale);
            globalDrawingCanvas.setEditingTextItem(textObj);
        } else {
            isCreatingNewText = true;
            activeEditingTextObj = null;
            pendingNewTextX = x;
            pendingNewTextY = y;
            inlineTextEditor.setText("");
            inlineTextEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, baseTextSize * currentScale);
            globalDrawingCanvas.setEditingTextItem(null);
        }

        float targetScreenX = (x + globalDrawingCanvas.getOffsetX()) * currentScale;
        float targetScreenY = (y + globalDrawingCanvas.getOffsetY()) * currentScale;

        int padLeft = inlineTextEditor.getTotalPaddingLeft();
        int padTop = inlineTextEditor.getTotalPaddingTop();

        inlineTextEditor.setX(targetScreenX - padLeft);
        inlineTextEditor.setY(targetScreenY - padTop);

        if (inlineTextEditor.getText() != null) {
            inlineTextEditor.setSelection(inlineTextEditor.getText().length());
        }

        inlineTextEditor.setVisibility(View.VISIBLE);
        inlineTextEditor.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(inlineTextEditor, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void openInlineTableCellEditor(DrawingView.TableCellClickResult result) {
        float currentScale = globalDrawingCanvas.getScaleFactor();
        inlineTextEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 32f * currentScale);
        if (result == null || globalDrawingCanvas == null || inlineTextEditor == null) return;

        commitInlineText();

        activeEditingTableCell = result;
        activeEditingTextObj = null;

        globalDrawingCanvas.setEditingTableCell(result);

        String currentText = "";
        if (result.table.cells != null) {
            for (DrawingView.TableCell cell : result.table.cells) {
                if (cell.row == result.row && cell.col == result.col) {
                    currentText = cell.text != null ? cell.text : "";
                    break;
                }
            }
        }

        inlineTextEditor.setText(currentText);
        inlineTextEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 32f);
        if (inlineTextEditor.getText() != null) {
            inlineTextEditor.setSelection(inlineTextEditor.getText().length());
        }

        updateInlineTableCellPosition();

        inlineTextEditor.setVisibility(View.VISIBLE);
        inlineTextEditor.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(inlineTextEditor, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void commitInlineText() {
        if (inlineTextEditor == null || inlineTextEditor.getVisibility() != View.VISIBLE) return;

        Editable editable = inlineTextEditor.getText();

        if (editable != null && editable.length() > 0) {
            SpannableString textCopy = new SpannableString(editable);

            if (isCreatingNewText) {
                if (globalDrawingCanvas != null) {
                    globalDrawingCanvas.addTextToCanvas(pendingNewTextX, pendingNewTextY, textCopy, currentTextColor);
                }
            } else if (activeEditingTextObj != null) {
                if (globalDrawingCanvas != null) {
                    globalDrawingCanvas.updateTextObject(activeEditingTextObj, textCopy);
                }
            }
        } else if (activeEditingTextObj != null && globalDrawingCanvas != null) {
            globalDrawingCanvas.removeTextObject(activeEditingTextObj);
        }

        if (globalDrawingCanvas != null) {
            globalDrawingCanvas.setEditingTextItem(null);
            globalDrawingCanvas.setEditingTableCell(null);
        }

        isCreatingNewText = false;
        activeEditingTextObj = null;
        activeEditingTableCell = null;
        inlineTextEditor.setVisibility(View.GONE);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(inlineTextEditor.getWindowToken(), 0);
        }
    }

    // Sınıf seviyesine şu iki takip değişkenini ekleyin:
    private long lastTextClickTime = 0L;
    private DrawingView.TextItem lastClickedTextObj = null;
    private static final long DOUBLE_TAP_TIMEOUT = 350L; // Milisaniye cinsinden çift tık aralığı

    @SuppressLint("ClickableViewAccessibility")
    private void setupCanvasTouchListener() {
        if (globalDrawingCanvas == null) return;

        globalDrawingCanvas.setOnTouchListener((v, event) -> {
            if (activeMode != DrawingView.ToolMode.TEXT) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (inlineTextEditor != null && inlineTextEditor.getVisibility() == View.VISIBLE) {
                        commitInlineText();
                    }
                }
                return false;
            }

            if (isCanvasLocked) {
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float touchX = (event.getX() / globalDrawingCanvas.getScaleFactor()) - globalDrawingCanvas.getOffsetX();
                float touchY = (event.getY() / globalDrawingCanvas.getScaleFactor()) - globalDrawingCanvas.getOffsetY();

                DrawingView.TableCellClickResult result = globalDrawingCanvas.checkTableCellClick(touchX, touchY);
                if (result != null) {
                    openInlineTableCellEditor(result);
                    return true;
                }

                DrawingView.TextItem clickedText = globalDrawingCanvas.checkTextClick(touchX, touchY);
                if (clickedText != null) {
                    long now = System.currentTimeMillis();

                    // ÇİFT TIK KONTROLÜ:
                    if (clickedText == lastClickedTextObj && (now - lastTextClickTime) < DOUBLE_TAP_TIMEOUT) {
                        // Kullanıcı çift tıkladı -> Şimdi klavyeyi aç ve düzenlemeye izin ver
                        openInlineTextEditor(clickedText.x, clickedText.y, clickedText);
                        lastClickedTextObj = null;
                        lastTextClickTime = 0L;
                    } else {
                        // TEK TIKLANDI -> Klavyeyi AÇMA! Sadece metni seç ve açık klavye varsa kapat
                        if (inlineTextEditor != null && inlineTextEditor.getVisibility() == View.VISIBLE) {
                            commitInlineText();
                        }
                        lastClickedTextObj = clickedText;
                        lastTextClickTime = now;
                    }
                    return true;
                } else {
                    // Boş alana tıklandığında:
                    if (inlineTextEditor != null && inlineTextEditor.getVisibility() == View.VISIBLE) {
                        commitInlineText(); // Açık klavyeyi kapat
                    } else {
                        // Boş yere tıklandı -> Yeni metin kutusu aç
                        openInlineTextEditor(touchX, touchY, null);
                    }
                    lastClickedTextObj = null;
                    lastTextClickTime = 0L;
                    return true;
                }
            }
            return false;
        });
    }

    private void shareAsText() {
        String title = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            title = "Başlıksız Not";
        }

        String canvasTexts = "";
        if (globalDrawingCanvas != null) {
            canvasTexts = globalDrawingCanvas.getAllTextContent();
        }

        String playStoreUrl = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String deepLinkUrl = "hadibakalim://note?id=" + currentNoteId;

        StringBuilder shareBody = new StringBuilder();
        shareBody.append("📝 ").append(title).append("\n\n");

        if (!canvasTexts.isEmpty()) {
            shareBody.append(canvasTexts).append("\n\n");
        }

        shareBody.append("Uygulamada Notu Aç:\n").append(deepLinkUrl).append("\n\n");
        shareBody.append("Uygulamayı İndir:\n").append(playStoreUrl);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody.toString());

        startActivity(Intent.createChooser(shareIntent, "Notu Paylaş"));
    }

    private void setupClickListeners() {
        ImageButton btnToolUndo = findViewById(R.id.btnToolUndo);
        ImageButton btnToolRedo = findViewById(R.id.btnToolRedo);
        ImageButton btnColorPicker = findViewById(R.id.btnColorPicker);
        ImageButton btnZoomIn = findViewById(R.id.btnZoomIn);
        ImageButton btnZoomOut = findViewById(R.id.btnZoomOut);
        TextView btnTextColor = findViewById(R.id.btnTextColor);
        ImageButton btnTextHighlight = findViewById(R.id.btnTextHighlight);

        View layoutRightSidePanel = findViewById(R.id.layoutRightSidePanel);
        ImageButton btnToggleRightPanel = findViewById(R.id.btnToggleRightPanel);
        ImageButton btnAddTable = findViewById(R.id.btnAddTable);
        ImageButton btnAddImage = findViewById(R.id.btnAddImage);
        ImageView colorBlackBasic = findViewById(R.id.colorBlackBasic);
        ImageView colorBlueBasic = findViewById(R.id.colorBlueBasic);
        ImageView colorRedBasic = findViewById(R.id.colorRedBasic);

        ImageButton btnResetZoom = findViewById(R.id.btnResetZoom);
        if (btnResetZoom != null && globalDrawingCanvas != null) {
            btnResetZoom.setOnClickListener(v -> globalDrawingCanvas.resetZoomAndPosition());
        }

        // Doğru ve tekil A (Renk) ve Vurgu ataması:
        if (btnTextColor != null) {
            btnTextColor.setOnClickListener(v -> applyTextColorToSelection(currentPenColor));
        }

        if (btnTextHighlight != null) {
            btnTextHighlight.setOnClickListener(v -> applyHighlightColorToSelection(0x88FACC15));
        }

        ImageView themeWhiteToggle = findViewById(R.id.themeWhiteToggle);
        ImageView themeSepiaToggle = findViewById(R.id.themeSepiaToggle);
        ImageView themeDarkToggle = findViewById(R.id.themeDarkToggle);

        if (themeWhiteToggle != null && globalDrawingCanvas != null) {
            themeWhiteToggle.setOnClickListener(v -> globalDrawingCanvas.setCanvasTheme(DrawingView.CanvasTheme.WHITE));
        }
        if (themeSepiaToggle != null && globalDrawingCanvas != null) {
            themeSepiaToggle.setOnClickListener(v -> globalDrawingCanvas.setCanvasTheme(DrawingView.CanvasTheme.SEPIA));
        }
        if (themeDarkToggle != null && globalDrawingCanvas != null) {
            themeDarkToggle.setOnClickListener(v -> globalDrawingCanvas.setCanvasTheme(DrawingView.CanvasTheme.DARK));
        }

        if (btnCloseEditor != null) {
            btnCloseEditor.setOnClickListener(v -> saveNoteAndExit());
        }

        if (btnMoreOptions != null) {
            btnMoreOptions.setOnClickListener(v -> {
                commitInlineText();
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, v);
                popup.getMenu().add(0, 1, 0, " Geçici Not Süresi");
                popup.getMenu().add(0, 2, 1, " PDF Olarak Dışa Aktar");
                popup.getMenu().add(0, 3, 2, " PNG (Görsel) Olarak Kaydet / Paylaş");
                popup.getMenu().add(0, 4, 3, inVault ? " Kasadan Çıkar" : " Gizli Kasaya Taşı");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        showEphemeralSelectionDialog();
                        return true;
                    } else if (item.getItemId() == 2) {
                        String title = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "Not";
                        if (globalDrawingCanvas != null) {
                            globalDrawingCanvas.exportToPdf(this, title);
                        }
                        return true;
                    } else if (item.getItemId() == 3) {
                        exportCanvasOrSelectionToPng();
                        return true;
                    } else if (item.getItemId() == 4) {
                        inVault = !inVault;
                        autoSaveNote();
                        Toast.makeText(this, inVault ? "Not gizli kasaya taşındı" : "Not normal alana çıkarıldı", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        if (btnZoomIn != null && globalDrawingCanvas != null) {
            btnZoomIn.setOnClickListener(v -> globalDrawingCanvas.zoomIn());
        }

        if (btnZoomOut != null && globalDrawingCanvas != null) {
            btnZoomOut.setOnClickListener(v -> globalDrawingCanvas.zoomOut());
        }

        if (colorBlackBasic != null) colorBlackBasic.setOnClickListener(v -> selectColor(0xFF09090B, colorBlackBasic));
        if (colorBlueBasic != null) colorBlueBasic.setOnClickListener(v -> selectColor(0xFF0284C7, colorBlueBasic));
        if (colorRedBasic != null) colorRedBasic.setOnClickListener(v -> selectColor(0xFFEF4444, colorRedBasic));

        if (btnToggleRightPanel != null && layoutRightSidePanel != null) {
            btnToggleRightPanel.setOnClickListener(v -> {
                if (layoutRightSidePanel.getVisibility() == View.VISIBLE) {
                    layoutRightSidePanel.setVisibility(View.GONE);
                    btnToggleRightPanel.setRotation(0f);
                } else {
                    layoutRightSidePanel.setVisibility(View.VISIBLE);
                    btnToggleRightPanel.setRotation(180f);
                }
            });
        }

        if (btnShareNote != null) {
            btnShareNote.setOnClickListener(v -> {
                commitInlineText();
                shareAsText();
            });
        }

        if (btnLockCanvas != null) {
            btnLockCanvas.setOnClickListener(v -> {
                isCanvasLocked = !isCanvasLocked;
                btnLockCanvas.setColorFilter(isCanvasLocked ? 0xFF0284C7 : 0xFF64748B);
            });
        }

        if (btnSaveNote != null) {
            btnSaveNote.setOnClickListener(v -> saveNoteAndExit());
        }

        if (colorBlack != null) {
            colorBlack.setOnClickListener(v -> selectColor(0xFF09090B, colorBlack));
        }

        if (colorBlue != null) {
            colorBlue.setOnClickListener(v -> selectColor(0xFF0284C7, colorBlue));
        }

        if (colorRed != null) {
            colorRed.setOnClickListener(v -> selectColor(0xFFEF4444, colorRed));
        }

        if (colorGreen != null) {
            colorGreen.setOnClickListener(v -> selectColor(0xFF10B981, colorGreen));
        }

        if (btnClearCanvas != null) {
            btnClearCanvas.bringToFront();
            btnClearCanvas.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                showClearCanvasDialog();
            });
        }

        ImageButton btnToolShapes = findViewById(R.id.btnToolShapes);
        if (btnToolShapes != null) {
            btnToolShapes.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                showShapePickerDialog();
            });
        }

        if (btnPinNote != null) {
            btnPinNote.setOnClickListener(v -> {
                isPinned = !isPinned;
                btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
            });
        }

        if (containerEphemeralBadge != null) {
            containerEphemeralBadge.setOnClickListener(v -> {
                commitInlineText();
                showEphemeralSelectionDialog();
            });
        }

        if (btnBlankPageToggle != null && globalDrawingCanvas != null) {
            btnBlankPageToggle.setOnClickListener(v -> {
                globalDrawingCanvas.setPageGridMode(DrawingView.PageGridMode.BLANK);
                updatePageGridButtonsUI(DrawingView.PageGridMode.BLANK);
            });
        }

        if (btnGridToggle != null && globalDrawingCanvas != null) {
            btnGridToggle.setOnClickListener(v -> {
                DrawingView.PageGridMode current = globalDrawingCanvas.getPageGridMode();
                DrawingView.PageGridMode next = (current == DrawingView.PageGridMode.GRID) ? DrawingView.PageGridMode.BLANK : DrawingView.PageGridMode.GRID;
                globalDrawingCanvas.setPageGridMode(next);
                updatePageGridButtonsUI(next);
            });
        }

        if (btnHorizontalLinesToggle != null && globalDrawingCanvas != null) {
            btnHorizontalLinesToggle.setOnClickListener(v -> {
                DrawingView.PageGridMode current = globalDrawingCanvas.getPageGridMode();
                DrawingView.PageGridMode next = (current == DrawingView.PageGridMode.HORIZONTAL_LINES) ? DrawingView.PageGridMode.BLANK : DrawingView.PageGridMode.HORIZONTAL_LINES;
                globalDrawingCanvas.setPageGridMode(next);
                updatePageGridButtonsUI(next);
            });
        }

        if (btnVerticalLinesToggle != null && globalDrawingCanvas != null) {
            btnVerticalLinesToggle.setOnClickListener(v -> {
                DrawingView.PageGridMode current = globalDrawingCanvas.getPageGridMode();
                DrawingView.PageGridMode next = (current == DrawingView.PageGridMode.VERTICAL_LINES) ? DrawingView.PageGridMode.BLANK : DrawingView.PageGridMode.VERTICAL_LINES;
                globalDrawingCanvas.setPageGridMode(next);
                updatePageGridButtonsUI(next);
            });
        }

        if (btnToolHand != null) {
            btnToolHand.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.HAND;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
            });
        }

        if (btnToolSelect != null) {
            btnToolSelect.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                activeMode = DrawingView.ToolMode.SELECT;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
            });
        }

        if (btnToolLasso != null) {
            btnToolLasso.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                activeMode = DrawingView.ToolMode.LASSO;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
            });
        }

        if (btnToolPen != null) {
            btnToolPen.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                activeMode = DrawingView.ToolMode.PEN;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showStrokeWidthDialog();
            });
        }

        if (btnToolHighlighter != null) {
            btnToolHighlighter.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                activeMode = DrawingView.ToolMode.HIGHLIGHTER;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showStrokeSizeDialog("Fosforlu Kalem Kalınlığı", 10f, 60f, 15f, 30f, 50f);
            });
        }

        if (btnToolText != null) {
            btnToolText.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                activeMode = DrawingView.ToolMode.TEXT;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
            });
        }

        if (btnToolEraser != null) {
            btnToolEraser.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                activeMode = DrawingView.ToolMode.ERASER;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showEraserWidthDialog();
            });
        }

        if (btnToolUndo != null && globalDrawingCanvas != null) {
            btnToolUndo.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                globalDrawingCanvas.undo();
            });
        }

        if (btnToolRedo != null && globalDrawingCanvas != null) {
            btnToolRedo.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                globalDrawingCanvas.redo();
            });
        }

        if (btnAddTable != null) {
            btnAddTable.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                showTableCreationDialog();
            });
        }

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                commitInlineText();
                if (imagePickerLauncher != null) {
                    imagePickerLauncher.launch("image/*");
                }
            });
        }

        if (btnColorPicker != null) {
            btnColorPicker.setOnClickListener(v -> {
                if (isCanvasLocked) return;
                showColorPickerDialog();
            });
        }
    }

    private void exportCanvasOrSelectionToPng() {
        if (globalDrawingCanvas == null) return;

        Bitmap bitmap = globalDrawingCanvas.exportThumbnail(1080, 1920);
        if (bitmap == null) return;

        try {
            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            cachePath.mkdirs();
            java.io.File file = new java.io.File(cachePath, "not_cizim_" + System.currentTimeMillis() + ".png");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Çizimi / Alanı Paylaş"));
            }
        } catch (Exception e) {
            Log.e(TAG, "PNG dışa aktarılırken hata", e);
        }
    }

    private void selectColor(int color, ImageView selectedView) {
        this.currentPenColor = color;

        if (globalDrawingCanvas != null) {
            globalDrawingCanvas.setColor(color);
        }

        View indicatorTextColor = findViewById(R.id.indicatorTextColor);
        if (indicatorTextColor != null) {
            indicatorTextColor.setBackgroundColor(color);
        }

        ImageView colorBlackBasic = findViewById(R.id.colorBlackBasic);
        ImageView colorBlueBasic = findViewById(R.id.colorBlueBasic);
        ImageView colorRedBasic = findViewById(R.id.colorRedBasic);

        ImageView[] allColors = {colorBlack, colorBlue, colorRed, colorGreen, colorBlackBasic, colorBlueBasic, colorRedBasic};
        for (ImageView img : allColors) {
            if (img != null) {
                img.setBackground(null);
                img.setPadding(4, 4, 4, 4);
            }
        }

        if (selectedView != null) {
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.OVAL);
            border.setColor(Color.TRANSPARENT);
            border.setStroke(6, 0xFF0284C7);
            selectedView.setBackground(border);
            selectedView.setPadding(6, 6, 6, 6);
        }
    }

    @SuppressLint("InflateParams")
    private void showClearCanvasDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_not_silme_uyari, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button btnCancelDelete = dialogView.findViewById(R.id.btnCancelDelete);
        Button btnConfirmDelete = dialogView.findViewById(R.id.btnConfirmDelete);

        if (btnCancelDelete != null) {
            btnCancelDelete.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirmDelete != null) {
            btnConfirmDelete.setOnClickListener(v -> {
                dialog.dismiss();
                if (globalDrawingCanvas != null) {
                    globalDrawingCanvas.clearCanvas();
                }
            });
        }

        dialog.show();
    }

    private void updatePageGridButtonsUI(DrawingView.PageGridMode mode) {
        if (btnBlankPageToggle != null) {
            btnBlankPageToggle.setColorFilter(mode == DrawingView.PageGridMode.BLANK ? 0xFF0284C7 : 0xFF64748B);
        }
        if (btnGridToggle != null) {
            btnGridToggle.setColorFilter(mode == DrawingView.PageGridMode.GRID ? 0xFF0284C7 : 0xFF64748B);
        }
        if (btnHorizontalLinesToggle != null) {
            btnHorizontalLinesToggle.setColorFilter(mode == DrawingView.PageGridMode.HORIZONTAL_LINES ? 0xFF0284C7 : 0xFF64748B);
        }
        if (btnVerticalLinesToggle != null) {
            btnVerticalLinesToggle.setColorFilter(mode == DrawingView.PageGridMode.VERTICAL_LINES ? 0xFF0284C7 : 0xFF64748B);
        }
    }

    private void updateActiveToolUI(DrawingView.ToolMode mode) {
        if (btnToolHand != null) {
            btnToolHand.setColorFilter(mode == DrawingView.ToolMode.HAND ? 0xFF0284C7 : 0xFF64748B);
        }

        if (frameToolPen != null) {
            boolean isPen = (mode == DrawingView.ToolMode.PEN);
            frameToolPen.setBackgroundResource(isPen ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            if (btnToolPen != null) {
                btnToolPen.setColorFilter(isPen ? 0xFF0284C7 : 0xFF475569);
            }
        }

        if (frameToolHighlighter != null) {
            boolean isHighlighter = (mode == DrawingView.ToolMode.HIGHLIGHTER);
            frameToolHighlighter.setBackgroundResource(isHighlighter ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            if (btnToolHighlighter != null) {
                btnToolHighlighter.setColorFilter(isHighlighter ? 0xFF0284C7 : 0xFF475569);
            }
        }

        if (frameToolText != null) {
            boolean isText = (mode == DrawingView.ToolMode.TEXT);
            frameToolText.setBackgroundResource(isText ? R.drawable.bg_chip_active : R.drawable.bg_chip_inactive);
            if (btnToolText != null) {
                btnToolText.setTextColor(isText ? 0xFF0284C7 : 0xFF475569);
            }
        }

        if (btnToolEraser != null) {
            btnToolEraser.setColorFilter(mode == DrawingView.ToolMode.ERASER ? 0xFF0284C7 : 0xFF475569);
        }

        if (btnToolSelect != null) {
            btnToolSelect.setColorFilter(mode == DrawingView.ToolMode.SELECT ? 0xFF0284C7 : 0xFF475569);
        }

        if (btnToolLasso != null) {
            btnToolLasso.setColorFilter(mode == DrawingView.ToolMode.LASSO ? 0xFF0284C7 : 0xFF475569);
        }
    }

    @SuppressLint("InflateParams")
    private void showTableCreationDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_table_config, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etRows = dialogView.findViewById(R.id.etRows);
        EditText etCols = dialogView.findViewById(R.id.etCols);
        Button btnCreateTable = dialogView.findViewById(R.id.btnCreateTable);

        if (btnCreateTable != null) {
            btnCreateTable.setOnClickListener(v -> {
                String rowStr = etRows != null ? etRows.getText().toString().trim() : "";
                String colStr = etCols != null ? etCols.getText().toString().trim() : "";

                if (TextUtils.isEmpty(rowStr) || TextUtils.isEmpty(colStr)) {
                    return;
                }

                try {
                    int rows = Integer.parseInt(rowStr);
                    int cols = Integer.parseInt(colStr);

                    if (rows <= 0 || cols <= 0 || rows > 50 || cols > 20) {
                        return;
                    }

                    if (globalDrawingCanvas != null) {
                        globalDrawingCanvas.addTableToCanvas(rows, cols);
                    }

                    dialog.dismiss();
                } catch (NumberFormatException ignored) {}
            });
        }

        dialog.show();
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showEphemeralSelectionDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gecici_sure_secimi, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtSelectedExpiryInfo = dialogView.findViewById(R.id.txtSelectedExpiryInfo);
        TextView btnQuick1Hour = dialogView.findViewById(R.id.btnQuick1Hour);
        TextView btnQuick24Hours = dialogView.findViewById(R.id.btnQuick24Hours);
        TextView btnQuick3Days = dialogView.findViewById(R.id.btnQuick3Days);
        TextView btnCustomDateTime = dialogView.findViewById(R.id.btnCustomDateTime);

        TextView btnCancelExpiry = dialogView.findViewById(R.id.btnCancelExpiry);
        TextView btnConfirmExpiry = dialogView.findViewById(R.id.btnConfirmExpiry);

        if (isEphemeral && expireTimestamp > System.currentTimeMillis()) {
            tempSelectedExpireTimestamp = expireTimestamp;
            txtSelectedExpiryInfo.setText("Mevcut Kapanış: " + EXPIRY_FORMAT.format(new Date(tempSelectedExpireTimestamp)));
        } else {
            tempSelectedExpireTimestamp = System.currentTimeMillis() + (5 * 60 * 1000L);
            txtSelectedExpiryInfo.setText("Süre: 5 Dakika sonra (" + EXPIRY_FORMAT.format(new Date(tempSelectedExpireTimestamp)) + ")");
        }

        btnQuick1Hour.setText("1 Saat");
        btnQuick1Hour.setOnClickListener(v -> {
            tempSelectedExpireTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);
            setDurationTabActive(btnQuick1Hour, btnQuick24Hours, btnQuick3Days, btnCustomDateTime);
            txtSelectedExpiryInfo.setText("Süre: 1 Saat sonra (" + EXPIRY_FORMAT.format(new Date(tempSelectedExpireTimestamp)) + ")");
        });

        btnQuick24Hours.setOnClickListener(v -> {
            tempSelectedExpireTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
            setDurationTabActive(btnQuick24Hours, btnQuick1Hour, btnQuick3Days, btnCustomDateTime);
            txtSelectedExpiryInfo.setText("Süre: 24 Saat sonra (" + EXPIRY_FORMAT.format(new Date(tempSelectedExpireTimestamp)) + ")");
        });

        btnQuick3Days.setOnClickListener(v -> {
            tempSelectedExpireTimestamp = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L);
            setDurationTabActive(btnQuick3Days, btnQuick1Hour, btnQuick24Hours, btnCustomDateTime);
            txtSelectedExpiryInfo.setText("Süre: 3 Gün sonra (" + EXPIRY_FORMAT.format(new Date(tempSelectedExpireTimestamp)) + ")");
        });

        btnCustomDateTime.setOnClickListener(v -> {
            final Calendar secilenZaman = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        secilenZaman.set(Calendar.YEAR, year);
                        secilenZaman.set(Calendar.MONTH, month);
                        secilenZaman.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePicker = new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    secilenZaman.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    secilenZaman.set(Calendar.MINUTE, minute);
                                    secilenZaman.set(Calendar.SECOND, 0);
                                    secilenZaman.set(Calendar.MILLISECOND, 0);

                                    long chosen = secilenZaman.getTimeInMillis();
                                    if (chosen > System.currentTimeMillis()) {
                                        tempSelectedExpireTimestamp = chosen;
                                        setDurationTabActive(btnCustomDateTime, btnQuick1Hour, btnQuick24Hours, btnQuick3Days);
                                        txtSelectedExpiryInfo.setText("Bitiş Anı: " + EXPIRY_FORMAT.format(new Date(chosen)));
                                    }
                                },
                                secilenZaman.get(Calendar.HOUR_OF_DAY),
                                secilenZaman.get(Calendar.MINUTE),
                                true
                        );
                        timePicker.setTitle("Kapanma Saatini Seçin");
                        timePicker.show();
                    },
                    secilenZaman.get(Calendar.YEAR),
                    secilenZaman.get(Calendar.MONTH),
                    secilenZaman.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.setTitle("Kapanma Tarihini Seçin");
            datePicker.show();
        });

        btnCancelExpiry.setOnClickListener(v -> dialog.dismiss());

        btnConfirmExpiry.setOnClickListener(v -> {
            if (tempSelectedExpireTimestamp <= System.currentTimeMillis()) {
                return;
            }

            this.isEphemeral = true;
            this.expireTimestamp = tempSelectedExpireTimestamp;
            this.currentCategory = "Geçici";

            updateLiveBadgeUI();

            String noteTitle = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "";
            scheduleNoteDeletionWarning(noteTitle, expireTimestamp);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void setDurationTabActive(TextView active, TextView... inactives) {
        active.setBackgroundResource(R.drawable.bg_chip_active);
        active.setTextColor(Color.WHITE);
        for (TextView in : inactives) {
            in.setBackgroundResource(R.drawable.bg_chip_inactive);
            in.setTextColor(Color.parseColor("#475569"));
        }
    }

    @SuppressLint("InflateParams")
    private void showColorPickerDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_color_picker, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        ImageView colorRedDialog = dialogView.findViewById(R.id.colorRed);
        ImageView colorGreenDialog = dialogView.findViewById(R.id.colorGreen);
        ImageView colorPurpleDialog = dialogView.findViewById(R.id.colorPurple);
        ImageView colorOrangeDialog = dialogView.findViewById(R.id.colorOrange);
        ImageView colorYellowDialog = dialogView.findViewById(R.id.colorYellow);
        Button btnCloseColorPicker = dialogView.findViewById(R.id.btnCloseColorPicker);

        if (colorRedDialog != null) {
            colorRedDialog.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFFEF4444);
                dialog.dismiss();
            });
        }

        if (colorGreenDialog != null) {
            colorGreenDialog.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFF22C55E);
                dialog.dismiss();
            });
        }

        if (colorPurpleDialog != null) {
            colorPurpleDialog.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFFA855F7);
                dialog.dismiss();
            });
        }

        if (colorOrangeDialog != null) {
            colorOrangeDialog.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFFF97316);
                dialog.dismiss();
            });
        }

        if (colorYellowDialog != null) {
            colorYellowDialog.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFFEAB308);
                dialog.dismiss();
            });
        }

        if (btnCloseColorPicker != null) {
            btnCloseColorPicker.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    @SuppressLint("InflateParams")
    private void showStrokeWidthDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_stroke_width, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        SeekBar seekBarStrokeWidth = dialogView.findViewById(R.id.seekBarStrokeWidth);
        Button btnThin = dialogView.findViewById(R.id.btnThin);
        Button btnMedium = dialogView.findViewById(R.id.btnMedium);
        Button btnThick = dialogView.findViewById(R.id.btnThick);

        if (seekBarStrokeWidth != null) {
            seekBarStrokeWidth.setProgress((int) currentStrokeWidth);

            seekBarStrokeWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentStrokeWidth = Math.max(2, progress);
                    if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnThin != null) {
            btnThin.setOnClickListener(v -> {
                currentStrokeWidth = 4f;
                if (seekBarStrokeWidth != null) seekBarStrokeWidth.setProgress(4);
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnMedium != null) {
            btnMedium.setOnClickListener(v -> {
                currentStrokeWidth = 12f;
                if (seekBarStrokeWidth != null) seekBarStrokeWidth.setProgress(12);
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnThick != null) {
            btnThick.setOnClickListener(v -> {
                currentStrokeWidth = 28f;
                if (seekBarStrokeWidth != null) seekBarStrokeWidth.setProgress(28);
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private synchronized void autoSaveNote() {
        if (isSaving) return;
        isSaving = true;

        String title = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            title = "Başlıksız Not";
        }

        String currentTime = DATE_FORMAT.format(new Date());

        String drawingJson = "";
        String base64Thumbnail = "";

        List<NoteBlockModel> blocks = new ArrayList<>();
        if (globalDrawingCanvas != null) {
            drawingJson = globalDrawingCanvas.getDrawingJson();
            NoteBlockModel drawingBlock = new NoteBlockModel(NoteBlockModel.BlockType.DRAWING);
            drawingBlock.setContent(drawingJson);
            blocks.add(drawingBlock);

            try {
                Bitmap thumb = globalDrawingCanvas.exportThumbnail(480, 270);
                if (thumb != null) {
                    java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                    thumb.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();
                    base64Thumbnail = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);
                }
            } catch (Exception ignored) {}
        }

        String contentToSave = !base64Thumbnail.isEmpty()
                ? "DRAWING_BASE64:" + base64Thumbnail
                : "Çizim Notu";

        notentity note = new notentity(title, contentToSave, currentCategory, "#0284C7", currentTime);
        note.isPinned = isPinned;
        note.blocks = blocks;
        note.isEphemeral = this.isEphemeral;
        note.expireTimestamp = this.expireTimestamp;
        note.inVault = this.inVault;

        if (noteDao != null) {
            final int idToUpdate = currentNoteId;
            DB_EXECUTOR.execute(() -> {
                try {
                    if (idToUpdate != -1) {
                        note.id = idToUpdate;
                        noteDao.updateNote(note);
                    } else {
                        long newId = noteDao.insertNote(note);
                        currentNoteId = (int) newId;
                    }
                } finally {
                    isSaving = false;
                }
            });
        } else {
            isSaving = false;
        }
    }

    private void saveNoteAndExit() {
        commitInlineText();
        finish();
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void showEraserWidthDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.silgi_kalinlik_ayarlama, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        SeekBar seekBarEraserWidth = dialogView.findViewById(R.id.seekBarEraserWidth);
        Button btnEraserThin = dialogView.findViewById(R.id.btnEraserThin);
        Button btnEraserMedium = dialogView.findViewById(R.id.btnEraserMedium);
        Button btnEraserThick = dialogView.findViewById(R.id.btnEraserThick);
        Button btnCloseEraserDialog = dialogView.findViewById(R.id.btnCloseEraserDialog);

        if (seekBarEraserWidth != null) {
            seekBarEraserWidth.setProgress((int) currentStrokeWidth);

            seekBarEraserWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentStrokeWidth = Math.max(6, progress);
                    if (globalDrawingCanvas != null) {
                        globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnEraserThin != null) {
            btnEraserThin.setOnClickListener(v -> {
                currentStrokeWidth = 10f;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnEraserMedium != null) {
            btnEraserMedium.setOnClickListener(v -> {
                currentStrokeWidth = 26f;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnEraserThick != null) {
            btnEraserThick.setOnClickListener(v -> {
                currentStrokeWidth = 55f;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnCloseEraserDialog != null) {
            btnCloseEraserDialog.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void showStrokeSizeDialog(String title, float minSize, float maxSize, float defaultThin, float defaultMedium, float defaultThick) {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.silgi_kalinlik_ayarlama, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialogView instanceof LinearLayout && ((LinearLayout) dialogView).getChildCount() > 0) {
            View firstChild = ((LinearLayout) dialogView).getChildAt(0);
            if (firstChild instanceof TextView) {
                ((TextView) firstChild).setText(title);
            }
        }

        SeekBar seekBarWidth = dialogView.findViewById(R.id.seekBarEraserWidth);
        Button btnThin = dialogView.findViewById(R.id.btnEraserThin);
        Button btnMedium = dialogView.findViewById(R.id.btnEraserMedium);
        Button btnThick = dialogView.findViewById(R.id.btnEraserThick);
        Button btnClose = dialogView.findViewById(R.id.btnCloseEraserDialog);

        if (seekBarWidth != null) {
            seekBarWidth.setMax((int) maxSize);
            seekBarWidth.setProgress((int) currentStrokeWidth);

            seekBarWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    currentStrokeWidth = Math.max(minSize, progress);
                    if (globalDrawingCanvas != null) {
                        globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnThin != null) {
            btnThin.setOnClickListener(v -> {
                currentStrokeWidth = defaultThin;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnMedium != null) {
            btnMedium.setOnClickListener(v -> {
                currentStrokeWidth = defaultMedium;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnThick != null) {
            btnThick.setOnClickListener(v -> {
                currentStrokeWidth = defaultThick;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setStrokeWidth(currentStrokeWidth);
                dialog.dismiss();
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    @SuppressLint("InflateParams")
    private void showShapePickerDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_shape_picker, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Button btnSquare = dialogView.findViewById(R.id.btnShapeSquare);
        Button btnRectangle = dialogView.findViewById(R.id.btnShapeRectangle);
        Button btnCircle = dialogView.findViewById(R.id.btnShapeCircle);
        Button btnLine = dialogView.findViewById(R.id.btnShapeLine);

        if (btnSquare != null) {
            btnSquare.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.SQUARE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
            });
        }

        if (btnRectangle != null) {
            btnRectangle.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.RECTANGLE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
            });
        }

        if (btnCircle != null) {
            btnCircle.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.CIRCLE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
            });
        }

        if (btnLine != null) {
            btnLine.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.LINE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    public static class NoteWarningWorker extends Worker {
        private static final String CHANNEL_ID = "gecici_not_uyari_kanali";

        public NoteWarningWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            String notBaslik = getInputData().getString("not_baslik");
            String uyariMetni = getInputData().getString("uyari_metni");

            if (notBaslik == null || notBaslik.trim().isEmpty()) {
                notBaslik = "Geçici Not";
            }
            if (uyariMetni == null || uyariMetni.trim().isEmpty()) {
                uyariMetni = "\"" + notBaslik + "\" başlıklı notunuz silinmek üzere.";
            }

            sendNotification(uyariMetni);
            return Result.success();
        }

        private void sendNotification(String uyariMetni) {
            Context context = getApplicationContext();
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Geçici Not Uyarıları",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Geçici notların silinmesine az süre kala uyarı bildirimi gönderir.");
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_time)
                    .setContentTitle("Notunuz Silinmek Üzere")
                    .setContentText(uyariMetni)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (manager != null) {
                int notificationId = (int) System.currentTimeMillis();
                manager.notify(notificationId, builder.build());
            }
        }
    }
}