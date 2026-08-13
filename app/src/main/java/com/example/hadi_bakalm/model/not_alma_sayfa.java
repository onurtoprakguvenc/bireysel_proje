package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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

public class not_alma_sayfa extends AppCompatActivity {

    private ImageButton btnCloseEditor, btnPinNote;
    private EditText etNoteTitle;

    private DrawingView globalDrawingCanvas;
    private LinearLayout drawingToolBar;

    private FrameLayout frameToolPen, frameToolHighlighter;
    private List<NoteBlockModel> blockList;

    private ImageButton btnToolScroll, btnToolPen, btnToolHighlighter, btnToolEraser;
    private ImageButton btnToolShapes, btnToolUndo, btnToolRedo;
    private ImageButton btnColorPicker, btnClearCanvas;

    private ImageView colorBlack, colorBlue, colorRed, colorGreen;
    private Button btnAddTable, btnAddImage, btnRecordVoice;

    private boolean isPinned = false;
    private boolean isVoiceRecording = false;
    private int currentNoteId = -1;
    private float currentStrokeWidth = 8f;
    private DrawingView.ToolMode activeMode = DrawingView.ToolMode.PEN;

    private notdao noteDao;

    // GALERİ SEÇİCİ LAUNCHER
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();
        blockList = new ArrayList<>();

        // Galeri Dinleyicisini Başlat
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
                            e.printStackTrace();
                            Toast.makeText(this, "Görsel yüklenemedi", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        initViews();
        setupClickListeners();
        setupTableTouchListener();

        if (getIntent() != null) {
            currentNoteId = getIntent().getIntExtra("EXTRA_NOTE_ID", -1);
            String incomingTitle = getIntent().getStringExtra("EXTRA_NOTE_TITLE");

            if (incomingTitle != null && etNoteTitle != null) {
                etNoteTitle.setText(incomingTitle);
            }

            if (currentNoteId != -1 && noteDao != null) {
                notentity existingNote = noteDao.getNoteById(currentNoteId);
                if (existingNote != null && existingNote.blocks != null) {
                    for (NoteBlockModel block : existingNote.blocks) {
                        if (block.getType() == NoteBlockModel.BlockType.DRAWING && globalDrawingCanvas != null) {
                            globalDrawingCanvas.loadDrawingFromJson(block.getContent());
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

    private void initViews() {
        btnCloseEditor = findViewById(R.id.btnCloseEditor);
        btnPinNote = findViewById(R.id.btnPinNote);
        etNoteTitle = findViewById(R.id.etNoteTitle);

        globalDrawingCanvas = findViewById(R.id.globalDrawingCanvas);
        drawingToolBar = findViewById(R.id.drawingToolBar);

        frameToolPen = findViewById(R.id.frameToolPen);
        frameToolHighlighter = findViewById(R.id.frameToolHighlighter);

        btnToolScroll = findViewById(R.id.btnToolScroll);
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
        btnRecordVoice = findViewById(R.id.btnRecordVoice);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTableTouchListener() {
        if (globalDrawingCanvas != null) {
            globalDrawingCanvas.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float touchX = event.getX() / globalDrawingCanvas.getScaleFactor();
                    float touchY = (event.getY() / globalDrawingCanvas.getScaleFactor()) - globalDrawingCanvas.getOffsetY();

                    DrawingView.TableCellClickResult result = globalDrawingCanvas.checkTableCellClick(touchX, touchY);
                    if (result != null) {
                        showCellTextInputDialog(result);
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void showCellTextInputDialog(DrawingView.TableCellClickResult result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hücre Yazısı (" + (result.row + 1) + ". Satır, " + (result.col + 1) + ". Sütun)");

        final EditText input = new EditText(this);
        input.setPadding(32, 32, 32, 32);

        for (DrawingView.TableCell cell : result.table.cells) {
            if (cell.row == result.row && cell.col == result.col) {
                input.setText(cell.text);
                break;
            }
        }

        builder.setView(input);

        builder.setPositiveButton("Kaydet", (dialog, which) -> {
            String text = input.getText().toString().trim();
            globalDrawingCanvas.updateTableCellText(result.table, result.row, result.col, text);
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());
        builder.show();
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

        if (btnToolScroll != null) {
            btnToolScroll.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.SCROLL;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Kaydırma Modu (Dikey kaydırın)", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolPen != null) {
            btnToolPen.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.PEN;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                showStrokeWidthDialog();
            });
        }

        if (btnToolHighlighter != null) {
            btnToolHighlighter.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.HIGHLIGHTER;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Fosforlu Kalem Modu", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolEraser != null) {
            btnToolEraser.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.ERASER;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Silgi Modu", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolUndo != null && globalDrawingCanvas != null) {
            btnToolUndo.setOnClickListener(v -> globalDrawingCanvas.undo());
        }

        if (btnToolRedo != null && globalDrawingCanvas != null) {
            btnToolRedo.setOnClickListener(v -> globalDrawingCanvas.redo());
        }

        if (btnClearCanvas != null && globalDrawingCanvas != null) {
            btnClearCanvas.setOnClickListener(v -> globalDrawingCanvas.clearCanvas());
        }

        if (btnToolShapes != null) {
            btnToolShapes.setOnClickListener(v -> showShapePickerDialog());
        }

        // RENK SEÇME PALETİ DIALOG'UNU ÇAĞIRAN BUTON
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
            btnAddTable.setOnClickListener(v -> showTableCreationDialog());
        }

        // GÖRSEL BUTONUNA BASILINCA GALERİYİ AÇAR
        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                if (imagePickerLauncher != null) {
                    imagePickerLauncher.launch("image/*");
                }
            });
        }

        if (btnRecordVoice != null) {
            btnRecordVoice.setOnClickListener(v -> {
                isVoiceRecording = !isVoiceRecording;
                btnRecordVoice.setText(isVoiceRecording ? "Duraklat" : "Ses Kaydı");
            });
        }
    }

    // ÖZEL RENK PALETİ DIALOG PENCERESİ
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

        Button btnRectangle = dialogView.findViewById(R.id.btnShapeRectangle);
        Button btnCircle = dialogView.findViewById(R.id.btnShapeCircle);
        Button btnLine = dialogView.findViewById(R.id.btnShapeLine);

        if (btnRectangle != null) {
            btnRectangle.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.RECTANGLE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Dikdörtgen çizebilirsiniz", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (btnCircle != null) {
            btnCircle.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.CIRCLE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Daire çizebilirsiniz", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        if (btnLine != null) {
            btnLine.setOnClickListener(v -> {
                activeMode = DrawingView.ToolMode.LINE;
                if (globalDrawingCanvas != null) globalDrawingCanvas.setToolMode(activeMode);
                updateActiveToolUI(activeMode);
                Toast.makeText(this, "Düz çizgi çizebilirsiniz", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
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

            Toast.makeText(this, "Tablo tuvale eklendi. Hücreye tıklayarak yazı yazabilirsiniz.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveNoteAndExit() {
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

        notentity note = new notentity(title, "Çizim Notu", "Kişisel", "#0284C7", currentTime);
        note.isPinned = isPinned;
        note.blocks = blockList;

        if (noteDao != null) {
            if (currentNoteId != -1) {
                note.id = currentNoteId;
                noteDao.updateNote(note);
                Toast.makeText(this, "Not Güncellendi", Toast.LENGTH_SHORT).show();
            } else {
                noteDao.insertNote(note);
                Toast.makeText(this, "Yeni Not Kaydedildi", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
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
    }
}