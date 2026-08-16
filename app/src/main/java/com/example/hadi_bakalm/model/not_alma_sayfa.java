package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("SpellCheckingInspection")
public class not_alma_sayfa extends AppCompatActivity {

    private ImageButton btnCloseEditor, btnPinNote;
    private EditText etNoteTitle;

    private DrawingView globalDrawingCanvas;
    private EditText inlineTextEditor;
    private DrawingView.TextItem activeEditingTextObj = null;
    private DrawingView.TableCellClickResult activeEditingTableCell = null;

    private FrameLayout frameToolPen, frameToolHighlighter, frameToolText;
    private TextView btnToolText;
    private List<NoteBlockModel> blockList;

    private ImageButton btnToolScroll, btnToolSelect, btnToolLasso, btnToolPen, btnToolHighlighter, btnToolEraser;
    private ImageButton btnToolShapes, btnToolUndo, btnToolRedo;
    private ImageButton btnColorPicker, btnClearCanvas;

    private ImageView colorBlack, colorBlue, colorRed, colorGreen;
    private Button btnAddTable, btnAddImage;

    private boolean isPinned = false;
    private int currentNoteId = -1;

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
        blockList = new ArrayList<>();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            if (globalDrawingCanvas != null) {
                                globalDrawingCanvas.addImageToCanvas(bitmap, uri.toString());
                                Toast.makeText(this, "Görsel tuvale eklendi", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("not_alma_sayfa", "Görsel yüklenirken hata oluştu", e);
                            Toast.makeText(this, "Görsel yüklenemedi", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        initViews();
        setupClickListeners();
        setupCanvasTouchListener();
        setupInlineEditorListener();

        if (getIntent() != null) {
            currentNoteId = getIntent().getIntExtra("EXTRA_NOTE_ID", -1);
            String incomingTitle = getIntent().getStringExtra("EXTRA_NOTE_TITLE");

            String incomingCategory = getIntent().getStringExtra("EXTRA_NOTE_CATEGORY");
            if (incomingCategory != null && !incomingCategory.trim().isEmpty()) {
                currentCategory = incomingCategory.trim();
            }

            if (incomingTitle != null && etNoteTitle != null) {
                etNoteTitle.setText(incomingTitle);
            }

            if (currentNoteId != -1 && noteDao != null) {
                notentity existingNote = noteDao.getNoteById(currentNoteId);
                if (existingNote != null) {
                    isPinned = existingNote.isPinned;
                    if (btnPinNote != null) {
                        btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
                    }

                    if (existingNote.category != null) {
                        currentCategory = existingNote.category;
                    }
                    if (existingNote.blocks != null && globalDrawingCanvas != null) {
                        for (NoteBlockModel block : existingNote.blocks) {
                            if (block.getType() == NoteBlockModel.BlockType.DRAWING) {
                                globalDrawingCanvas.loadDrawingFromJson(block.getContent());
                            }
                        }
                    }
                }
            }
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveNoteAndExit();
            }
        });
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
        etNoteTitle = findViewById(R.id.etNoteTitle);

        globalDrawingCanvas = findViewById(R.id.globalDrawingCanvas);
        inlineTextEditor = findViewById(R.id.inlineTextEditor);

        frameToolPen = findViewById(R.id.frameToolPen);
        frameToolHighlighter = findViewById(R.id.frameToolHighlighter);
        frameToolText = findViewById(R.id.frameToolText);
        btnToolText = findViewById(R.id.btnToolText);

        btnToolScroll = findViewById(R.id.btnToolScroll);
        btnToolSelect = findViewById(R.id.btnToolSelect);
        btnToolLasso = findViewById(R.id.btnToolLasso);
        btnToolPen = findViewById(R.id.btnToolPen);
        btnToolHighlighter = findViewById(R.id.btnToolHighlighter);
        btnToolEraser = findViewById(R.id.btnToolEraser);

        btnToolShapes = findViewById(R.id.btnToolShapes);
        btnToolUndo = findViewById(R.id.btnToolUndo);
        btnToolRedo = findViewById(R.id.btnToolRedo);
        btnColorPicker = findViewById(R.id.btnColorPicker);
        btnClearCanvas = findViewById(R.id.btnClearCanvas);

        colorBlack = findViewById(R.id.colorBlack);
        colorBlue = findViewById(R.id.colorBlue);
        colorRed = findViewById(R.id.colorRed);
        colorGreen = findViewById(R.id.colorGreen);

        btnAddTable = findViewById(R.id.btnAddTable);
        btnAddImage = findViewById(R.id.btnAddImage);
    }

    private void setupInlineEditorListener() {
        if (inlineTextEditor == null) return;

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

    private void openInlineTextEditor(float x, float y, DrawingView.TextItem textObj) {
        commitInlineText();

        activeEditingTableCell = null;

        if (textObj != null) {
            isCreatingNewText = false;
            activeEditingTextObj = textObj;
            inlineTextEditor.setText(textObj.text);
        } else {
            isCreatingNewText = true;
            activeEditingTextObj = null;
            pendingNewTextX = x;
            pendingNewTextY = y;
            inlineTextEditor.setText("");
        }

        float scale = globalDrawingCanvas.getScaleFactor();
        float screenX = x * scale;
        float screenY = (y + globalDrawingCanvas.getOffsetY()) * scale;

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

    private void commitInlineText() {
        if (inlineTextEditor == null || inlineTextEditor.getVisibility() != View.VISIBLE) return;

        String text = inlineTextEditor.getText().toString().trim();

        if (isCreatingNewText) {
            if (!text.isEmpty() && globalDrawingCanvas != null) {
                globalDrawingCanvas.addTextToCanvas(pendingNewTextX, pendingNewTextY, text, 0xFF0F172A);
            }
        } else if (activeEditingTextObj != null) {
            if (text.isEmpty()) {
                globalDrawingCanvas.removeTextObject(activeEditingTextObj);
            } else {
                globalDrawingCanvas.updateTextObject(activeEditingTextObj, text);
            }
        } else if (activeEditingTableCell != null) {
            globalDrawingCanvas.updateTableCellText(activeEditingTableCell.table, activeEditingTableCell.row, activeEditingTableCell.col, text);
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

    private void openInlineTableCellEditor(DrawingView.TableCellClickResult result) {
        commitInlineText();

        activeEditingTableCell = result;
        activeEditingTextObj = null;

        float scale = globalDrawingCanvas.getScaleFactor();
        float cellX = result.table.startX + (result.col * result.table.cellWidth);
        float cellY = result.table.startY + (result.row * result.table.cellHeight);

        float screenX = cellX * scale;
        float screenY = (cellY + globalDrawingCanvas.getOffsetY()) * scale;

        inlineTextEditor.setX(screenX + 8f);
        inlineTextEditor.setY(screenY + 8f);

        String currentText = "";
        for (DrawingView.TableCell cell : result.table.cells) {
            if (cell.row == result.row && cell.col == result.col) {
                currentText = cell.text;
                break;
            }
        }

        inlineTextEditor.setText(currentText);
        inlineTextEditor.setSelection(inlineTextEditor.getText().length());
        inlineTextEditor.setVisibility(View.VISIBLE);
        inlineTextEditor.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(inlineTextEditor, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCanvasTouchListener() {
        if (globalDrawingCanvas != null) {
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
    }

    private void setupClickListeners() {
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

        if (btnToolSelect != null) {
            btnToolSelect.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.SELECT;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Seçim Modu: Düzenlemek veya silmek için nesneye dokunun", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolLasso != null) {
            btnToolLasso.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.LASSO;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Kement Modu: Seçmek istediğiniz alanı parmağınızla çizin", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolScroll != null) {
            btnToolScroll.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.SCROLL;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Kaydırma Modu", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Metin Modu: İstediğiniz yere dokunup yazın", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolEraser != null) {
            btnToolEraser.setOnClickListener(v -> {
                commitInlineText();
                activeMode = DrawingView.ToolMode.ERASER;
                if (globalDrawingCanvas != null) {
                    globalDrawingCanvas.setToolMode(activeMode);
                }
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

        if (btnToolShapes != null) {
            btnToolShapes.setOnClickListener(v -> {
                commitInlineText();
                showShapePickerDialog();
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

        if (btnToolScroll != null) {
            boolean isScroll = (mode == DrawingView.ToolMode.SCROLL);
            btnToolScroll.setColorFilter(isScroll ? 0xFF0284C7 : 0xFF475569);
        }

        if (btnToolSelect != null) {
            boolean isSelect = (mode == DrawingView.ToolMode.SELECT);
            btnToolSelect.setColorFilter(isSelect ? 0xFF0284C7 : 0xFF475569);
        }

        if (btnToolLasso != null) {
            boolean isLasso = (mode == DrawingView.ToolMode.LASSO);
            btnToolLasso.setColorFilter(isLasso ? 0xFF0284C7 : 0xFF475569);
        }

        if (btnToolEraser != null) {
            boolean isEraser = (mode == DrawingView.ToolMode.ERASER);
            btnToolEraser.setColorFilter(isEraser ? 0xFF0284C7 : 0xFF475569);
        }
    }

    private void showColorPickerDialog() {
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

    private void showStrokeWidthDialog() {
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

    private void showShapePickerDialog() {
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
                showStrokeSizeDialog("Kare Çizgi Kalınlığı", 2f, 40f, 4f, 10f, 20f);
            });
        }

        if (btnRectangle != null) {
            btnRectangle.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.RECTANGLE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
                showStrokeSizeDialog("Dikdörtgen Çizgi Kalınlığı", 2f, 40f, 4f, 10f, 20f);
            });
        }

        if (btnCircle != null) {
            btnCircle.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.CIRCLE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
                showStrokeSizeDialog("Daire Çizgi Kalınlığı", 2f, 40f, 4f, 10f, 20f);
            });
        }

        if (btnLine != null) {
            btnLine.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.LINE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                dialog.dismiss();
                showStrokeSizeDialog("Çizgi Kalınlığı", 2f, 40f, 4f, 10f, 20f);
            });
        }

        dialog.show();
    }

    private void showTableCreationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_table_config, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        EditText etRows = dialogView.findViewById(R.id.etRows);
        EditText etCols = dialogView.findViewById(R.id.etCols);
        Button btnCreateTable = dialogView.findViewById(R.id.btnCreateTable);

        btnCreateTable.setOnClickListener(v -> {
            String rowStr = etRows.getText().toString().trim();
            String colStr = etCols.getText().toString().trim();

            if (TextUtils.isEmpty(rowStr) || TextUtils.isEmpty(colStr)) {
                Toast.makeText(this, "Lütfen satır ve sütun sayılarını girin", Toast.LENGTH_SHORT).show();
                return;
            }

            int rows = Integer.parseInt(rowStr);
            int cols = Integer.parseInt(colStr);

            if (rows <= 0 || cols <= 0) {
                Toast.makeText(this, "Geçerli bir boyut girin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (globalDrawingCanvas != null) {
                globalDrawingCanvas.addTableToCanvas(rows, cols);
            }

            Toast.makeText(this, "Tablo eklendi. Hücreye tıklayarak doğrudan yazabilirsiniz.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void autoSaveNote() {
        String title = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "";
        if (TextUtils.isEmpty(title)) {
            title = "Başlıksız Not";
        }

        String currentTime = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR")).format(new Date());

        blockList.clear();
        if (globalDrawingCanvas != null) {
            NoteBlockModel drawingBlock = new NoteBlockModel(NoteBlockModel.BlockType.DRAWING);
            drawingBlock.setContent(globalDrawingCanvas.getDrawingJson());
            blockList.add(drawingBlock);
        }

        notentity note = new notentity(title, "Çizim Notu", currentCategory, "#0284C7", currentTime);
        note.isPinned = isPinned;
        note.blocks = blockList;

        if (noteDao != null) {
            if (currentNoteId != -1) {
                note.id = currentNoteId;
                noteDao.updateNote(note);
            } else {
                long newId = noteDao.insertNote(note);
                currentNoteId = (int) newId;
            }
        }
    }

    private void saveNoteAndExit() {
        commitInlineText();
        autoSaveNote();
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void showEraserWidthDialog() {
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

    @SuppressLint("SetTextI18n")
    private void showStrokeSizeDialog(String title, float minSize, float maxSize, float defaultThin, float defaultMedium, float defaultThick) {
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