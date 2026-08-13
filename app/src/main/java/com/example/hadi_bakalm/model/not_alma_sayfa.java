package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
    private ImageView colorBlack, colorBlue;
    private Button btnAddTable, btnAddImage, btnRecordVoice;

    private boolean isPinned = false;
    private boolean isVoiceRecording = false;
    private int currentNoteId = -1;
    private float currentStrokeWidth = 8f;
    private DrawingView.ToolMode activeMode = DrawingView.ToolMode.PEN;

    private notdao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();
        blockList = new ArrayList<>();

        initViews();
        setupClickListeners();

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

        btnAddTable = findViewById(R.id.btnAddTable);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnRecordVoice = findViewById(R.id.btnRecordVoice);
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

        // TABLO BUTONU TEKRAR AKTİF
        if (btnAddTable != null) {
            btnAddTable.setOnClickListener(v -> showTableCreationDialog());
        }

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> Toast.makeText(this, "Galeriden görsel seçici açılıyor...", Toast.LENGTH_SHORT).show());
        }

        if (btnRecordVoice != null) {
            btnRecordVoice.setOnClickListener(v -> {
                isVoiceRecording = !isVoiceRecording;
                btnRecordVoice.setText(isVoiceRecording ? "Duraklat" : "Ses Kaydı");
            });
        }
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

    // GERİ GETİRİLEN TABLO OLUŞTURMA PENCERESİ
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

            // Tablo çizimini tuval üzerine vektörel ızgara olarak düşürür
            if (globalDrawingCanvas != null) {
                globalDrawingCanvas.addTableToCanvas(rows, cols);
            }

            Toast.makeText(this, "Tablo tuvale eklendi", Toast.LENGTH_SHORT).show();
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