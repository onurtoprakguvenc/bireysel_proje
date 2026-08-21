package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

@SuppressWarnings("SpellCheckingInspection")
public class not_alma_sayfa extends AppCompatActivity {

    private static final String TAG = "not_alma_sayfa";
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR"));

    private ImageButton btnCloseEditor;
    private ImageButton btnPinNote;
    private ImageButton btnEphemeralTimer;
    private ImageButton btnGridToggle;
    private EditText etNoteTitle;

    private DrawingView globalDrawingCanvas;
    private EditText inlineTextEditor;
    private DrawingView.TextItem activeEditingTextObj = null;
    private DrawingView.TableCellClickResult activeEditingTableCell = null;

    private FrameLayout frameToolPen;
    private FrameLayout frameToolHighlighter;
    private FrameLayout frameToolText;
    private TextView btnToolText;

    private ImageButton btnToolPen;
    private ImageButton btnToolHighlighter;
    private ImageButton btnToolEraser;
    private ImageButton btnToolSelect;
    private ImageButton btnToolLasso;

    private boolean isPinned = false;
    private int currentNoteId = -1;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();

        setupImagePicker();
        initViews();
        setupClickListeners();
        setupCanvasTouchListener();
        setupInlineEditorListener();
        loadInitialIntentData();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveNoteAndExit();
            }
        });
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
                Toast.makeText(this, "Görsel tuvale eklendi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Görsel yüklenirken hata oluştu", e);
            Toast.makeText(this, "Görsel yüklenemedi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        commitInlineText();
        autoSaveNote();
    }

    private void initViews() {
        btnCloseEditor = findViewById(R.id.btnCloseEditor);
        btnPinNote = findViewById(R.id.btnPinNote);
        btnEphemeralTimer = findViewById(R.id.btnEphemeralTimer);
        btnGridToggle = findViewById(R.id.btnGridToggle);
        etNoteTitle = findViewById(R.id.etNoteTitle);

        globalDrawingCanvas = findViewById(R.id.globalDrawingCanvas);
        inlineTextEditor = findViewById(R.id.inlineTextEditor);

        frameToolPen = findViewById(R.id.frameToolPen);
        frameToolHighlighter = findViewById(R.id.frameToolHighlighter);
        frameToolText = findViewById(R.id.frameToolText);
        btnToolText = findViewById(R.id.btnToolText);

        btnToolPen = findViewById(R.id.btnToolPen);
        btnToolHighlighter = findViewById(R.id.btnToolHighlighter);
        btnToolEraser = findViewById(R.id.btnToolEraser);
        btnToolSelect = findViewById(R.id.btnToolSelect);
        btnToolLasso = findViewById(R.id.btnToolLasso);
    }

    private void loadInitialIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        // Geçici not verilerini oku
        boolean incomingIsEphemeral = intent.getBooleanExtra("EXTRA_IS_EPHEMERAL", false);
        long incomingExpireTimestamp = intent.getLongExtra("EXTRA_EXPIRE_TIMESTAMP", 0L);

        if (incomingIsEphemeral && incomingExpireTimestamp > 0) {
            this.isEphemeral = true;
            this.expireTimestamp = incomingExpireTimestamp;
            if (btnEphemeralTimer != null) {
                btnEphemeralTimer.setColorFilter(0xFFD32F2F); // Geçici ikonunu kırmızı yapar
            }
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

                    if (existingNote.category != null) {
                        currentCategory = existingNote.category;
                    }

                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;

                        if (btnPinNote != null) {
                            btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
                        }

                        if (btnEphemeralTimer != null && isEphemeral) {
                            btnEphemeralTimer.setColorFilter(0xFFD32F2F);
                        }

                        if (existingNote.blocks != null && globalDrawingCanvas != null) {
                            for (NoteBlockModel block : existingNote.blocks) {
                                if (block != null && block.getType() == NoteBlockModel.BlockType.DRAWING) {
                                    globalDrawingCanvas.loadDrawingFromJson(block.getContent());
                                }
                            }
                        }
                    });
                }
            });
        }
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

        inlineTextEditor.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                commitInlineText();
            }
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

        float screenX = cellX * scale;
        float screenY = (cellY + globalDrawingCanvas.getOffsetY()) * scale;

        inlineTextEditor.setX(screenX + 8f);
        inlineTextEditor.setY(screenY + 8f);
    }

    private void openInlineTextEditor(float x, float y, DrawingView.TextItem textObj) {
        commitInlineText();
        activeEditingTableCell = null;

        if (textObj != null) {
            isCreatingNewText = false;
            activeEditingTextObj = textObj;
            inlineTextEditor.setText(textObj.text);
            inlineTextEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textObj.textSize > 0 ? textObj.textSize : 36f);
            if (globalDrawingCanvas != null) {
                globalDrawingCanvas.setEditingTextItem(textObj);
            }
        } else {
            isCreatingNewText = true;
            activeEditingTextObj = null;
            pendingNewTextX = x;
            pendingNewTextY = y;
            inlineTextEditor.setText("");
            inlineTextEditor.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, 36f);
            if (globalDrawingCanvas != null) {
                globalDrawingCanvas.setEditingTextItem(null);
            }
        }

        if (globalDrawingCanvas == null) return;

        float scale = globalDrawingCanvas.getScaleFactor();
        float screenX = x * scale;
        float textSize = (textObj != null && textObj.textSize > 0) ? textObj.textSize : 36f;
        float screenY = ((y - textSize) + globalDrawingCanvas.getOffsetY()) * scale;

        inlineTextEditor.setX(screenX);
        inlineTextEditor.setY(screenY);
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

        String text = inlineTextEditor.getText().toString().trim();

        if (isCreatingNewText) {
            if (!text.isEmpty() && globalDrawingCanvas != null) {
                globalDrawingCanvas.addTextToCanvas(pendingNewTextX, pendingNewTextY, text, 0xFF0F172A);
            }
        } else if (activeEditingTextObj != null) {
            if (globalDrawingCanvas != null) {
                if (text.isEmpty()) {
                    globalDrawingCanvas.removeTextObject(activeEditingTextObj);
                } else {
                    globalDrawingCanvas.updateTextObject(activeEditingTextObj, text);
                }
            }
        } else if (activeEditingTableCell != null && globalDrawingCanvas != null) {
            globalDrawingCanvas.updateTableCellText(activeEditingTableCell.table, activeEditingTableCell.row, activeEditingTableCell.col, text);
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

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (inlineTextEditor != null && inlineTextEditor.getVisibility() == View.VISIBLE) {
                    commitInlineText();
                }

                float touchX = event.getX() / globalDrawingCanvas.getScaleFactor();
                float touchY = (event.getY() / globalDrawingCanvas.getScaleFactor()) - globalDrawingCanvas.getOffsetY();

                DrawingView.TableCellClickResult result = globalDrawingCanvas.checkTableCellClick(touchX, touchY);
                if (result != null) {
                    openInlineTableCellEditor(result);
                    return true;
                }

                DrawingView.TextItem clickedText = globalDrawingCanvas.checkTextClick(touchX, touchY);
                if (clickedText != null) {
                    openInlineTextEditor(clickedText.x, clickedText.y, clickedText);
                } else {
                    openInlineTextEditor(touchX, touchY, null);
                }
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        ImageButton btnToolUndo = findViewById(R.id.btnToolUndo);
        ImageButton btnToolRedo = findViewById(R.id.btnToolRedo);
        ImageButton btnColorPicker = findViewById(R.id.btnColorPicker);
        ImageButton btnClearCanvas = findViewById(R.id.btnClearCanvas);

        ImageButton btnAddTable = findViewById(R.id.btnAddTable);
        ImageButton btnAddImage = findViewById(R.id.btnAddImage);

        ImageView colorBlack = findViewById(R.id.colorBlack);
        ImageView colorBlue = findViewById(R.id.colorBlue);
        ImageView colorRed = findViewById(R.id.colorRed);
        ImageView colorGreen = findViewById(R.id.colorGreen);

        if (btnCloseEditor != null) {
            btnCloseEditor.setOnClickListener(v -> saveNoteAndExit());
        }

        if (btnPinNote != null) {
            btnPinNote.setOnClickListener(v -> {
                isPinned = !isPinned;
                btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
                Toast.makeText(this, isPinned ? "Not sabitlendi" : "Sabitleme kaldırıldı", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnEphemeralTimer != null) {
            btnEphemeralTimer.setOnClickListener(v -> {
                commitInlineText();
                showEphemeralDialog();
            });
        }

        if (btnGridToggle != null && globalDrawingCanvas != null) {
            btnGridToggle.setOnClickListener(v -> {
                globalDrawingCanvas.toggleGrid();
                btnGridToggle.setColorFilter(globalDrawingCanvas.isGridVisible() ? 0xFF0284C7 : 0xFF64748B);
            });
        }

        if (btnToolSelect != null) {
            btnToolSelect.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.SELECT;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Seçim Modu: Nesneye dokunup taşıyın", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolLasso != null) {
            btnToolLasso.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.LASSO;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Kement Modu: Çizerek seçin", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolPen != null) {
            btnToolPen.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.PEN;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showStrokeWidthDialog();
            });
        }

        if (btnToolHighlighter != null) {
            btnToolHighlighter.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.HIGHLIGHTER;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showStrokeSizeDialog("Fosforlu Kalem Kalınlığı", 10f, 60f, 15f, 30f, 50f);
            });
        }

        if (btnToolText != null) {
            btnToolText.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.TEXT;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Metin Modu: Tuvale dokunup yazın", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolEraser != null) {
            btnToolEraser.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.ERASER;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showEraserWidthDialog();
            });
        }

        if (btnToolUndo != null && globalDrawingCanvas != null) {
            btnToolUndo.setOnClickListener(v -> {
                commitInlineText();
                globalDrawingCanvas.undo();
            });
        }

        if (btnToolRedo != null && globalDrawingCanvas != null) {
            btnToolRedo.setOnClickListener(v -> {
                commitInlineText();
                globalDrawingCanvas.redo();
            });
        }

        if (btnClearCanvas != null && globalDrawingCanvas != null) {
            btnClearCanvas.setOnClickListener(v -> {
                commitInlineText();
                globalDrawingCanvas.clearCanvas();
            });
        }

        if (btnAddTable != null) {
            btnAddTable.setOnClickListener(v -> {
                commitInlineText();
                showTableCreationDialog();
            });
        }

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                commitInlineText();
                if (imagePickerLauncher != null) {
                    imagePickerLauncher.launch("image/*");
                }
            });
        }

        if (btnColorPicker != null) {
            btnColorPicker.setOnClickListener(v -> showColorPickerDialog());
        }

        if (colorBlack != null) {
            colorBlack.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFF09090B);
            });
        }

        if (colorBlue != null) {
            colorBlue.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFF0284C7);
            });
        }

        if (colorRed != null) {
            colorRed.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFFEF4444);
            });
        }

        if (colorGreen != null) {
            colorGreen.setOnClickListener(v -> {
                if (globalDrawingCanvas != null) globalDrawingCanvas.setColor(0xFF10B981);
            });
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
                    Toast.makeText(this, "Lütfen satır ve sütun sayılarını girin", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int rows = Integer.parseInt(rowStr);
                    int cols = Integer.parseInt(colStr);

                    if (rows <= 0 || cols <= 0 || rows > 50 || cols > 20) {
                        Toast.makeText(this, "Geçerli bir boyut girin (Maks: 50 satır, 20 sütun)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (globalDrawingCanvas != null) {
                        float currentCanvasOffsetY = globalDrawingCanvas.getOffsetY();
                        float spawnX = 60f;
                        float spawnY = -currentCanvasOffsetY + 140f;
                        globalDrawingCanvas.addTableToCanvas(spawnX, spawnY, rows, cols);
                    }

                    Toast.makeText(this, "Tablo eklendi. Hücreye tıklayarak yazabilirsiniz.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Lütfen sadece sayısal değerler girin", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void showEphemeralDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.gecici_not_uyari, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView txtDialogMessage = dialogView.findViewById(R.id.txtDialogMessage);
        Button btn1Hour = dialogView.findViewById(R.id.btnDuration1Hour);
        Button btn24Hours = dialogView.findViewById(R.id.btnDuration24Hours);
        Button btnCustom = dialogView.findViewById(R.id.btnDurationCustom);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tempSelectedExpireTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);

        if (btn1Hour != null) {
            btn1Hour.setOnClickListener(v -> {
                tempSelectedExpireTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);
                if (txtDialogMessage != null) {
                    txtDialogMessage.setText("Süre: 1 Saat sonra Geri Dönüşüm Kutusuna taşınacak.");
                }
            });
        }

        if (btn24Hours != null) {
            btn24Hours.setOnClickListener(v -> {
                tempSelectedExpireTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
                if (txtDialogMessage != null) {
                    txtDialogMessage.setText("Süre: 24 Saat sonra Geri Dönüşüm Kutusuna taşınacak.");
                }
            });
        }

        if (btnCustom != null) {
            btnCustom.setOnClickListener(v -> {
                Calendar takvim = Calendar.getInstance();
                DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                    takvim.set(Calendar.YEAR, year);
                    takvim.set(Calendar.MONTH, month);
                    takvim.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePicker = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                        takvim.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        takvim.set(Calendar.MINUTE, minute);
                        takvim.set(Calendar.SECOND, 0);

                        long chosenTime = takvim.getTimeInMillis();
                        if (chosenTime > System.currentTimeMillis()) {
                            tempSelectedExpireTimestamp = chosenTime;
                            if (txtDialogMessage != null) {
                                txtDialogMessage.setText("Bitiş: " + dayOfMonth + "/" + (month + 1) + "/" + year + " " + String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                            }
                        } else {
                            Toast.makeText(this, "Geçmiş bir zaman seçemezsiniz!", Toast.LENGTH_SHORT).show();
                        }
                    }, takvim.get(Calendar.HOUR_OF_DAY), takvim.get(Calendar.MINUTE), true);
                    timePicker.show();
                }, takvim.get(Calendar.YEAR), takvim.get(Calendar.MONTH), takvim.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                isEphemeral = true;
                expireTimestamp = tempSelectedExpireTimestamp;
                if (btnEphemeralTimer != null) {
                    btnEphemeralTimer.setColorFilter(0xFFD32F2F);
                }
                Toast.makeText(this, "Not geçici olarak ayarlandı", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void updateActiveToolUI(DrawingView.ToolMode mode) {
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

    private void autoSaveNote() {
        String title = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            title = "Başlıksız Not";
        }

        String currentTime = DATE_FORMAT.format(new Date());

        List<NoteBlockModel> blocks = new ArrayList<>();
        if (globalDrawingCanvas != null) {
            NoteBlockModel drawingBlock = new NoteBlockModel(NoteBlockModel.BlockType.DRAWING);
            drawingBlock.setContent(globalDrawingCanvas.getDrawingJson());
            blocks.add(drawingBlock);
        }

        notentity note = new notentity(title, "Çizim Notu", currentCategory, "#0284C7", currentTime);
        note.isPinned = isPinned;
        note.blocks = blocks;
        note.isEphemeral = isEphemeral;
        note.expireTimestamp = expireTimestamp;

        if (noteDao != null) {
            final int idToUpdate = currentNoteId;
            DB_EXECUTOR.execute(() -> {
                if (idToUpdate != -1) {
                    note.id = idToUpdate;
                    noteDao.updateNote(note);
                } else {
                    long newId = noteDao.insertNote(note);
                    currentNoteId = (int) newId;
                }
            });
        }
    }

    private void saveNoteAndExit() {
        commitInlineText();
        autoSaveNote();
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
}