package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.NoteBlockAdapter;
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
    private RecyclerView rvNoteBlocks;
    private LinearLayout drawingToolBar;

    private NoteBlockAdapter blockAdapter;
    private List<NoteBlockModel> blockList;

    private ImageButton btnToolScroll, btnToolPen, btnToolHighlighter, btnToolEraser;
    private ImageButton btnToolShapes, btnToolUndo, btnToolRedo, btnToolStrokeWidth;
    private ImageButton btnColorPicker, btnClearCanvas, btnReservePool;
    private ImageView colorBlack, colorBlue;
    private Button btnAddTable, btnAddImage, btnRecordVoice;

    private boolean isPinned = false;
    private boolean isVoiceRecording = false;
    private int currentNoteId = -1;
    private float currentStrokeWidth = 8f;

    private notdao noteDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();

        initViews();
        setupRecyclerView();
        setupClickListeners();

        if (getIntent() != null) {
            currentNoteId = getIntent().getIntExtra("EXTRA_NOTE_ID", -1);
            String incomingTitle = getIntent().getStringExtra("EXTRA_NOTE_TITLE");
            String incomingContent = getIntent().getStringExtra("EXTRA_NOTE_CONTENT");

            if (incomingTitle != null && etNoteTitle != null) {
                etNoteTitle.setText(incomingTitle);
            }

            if (incomingContent != null && blockList != null && !blockList.isEmpty()) {
                blockList.get(0).setContent(incomingContent);
                if (blockAdapter != null) {
                    blockAdapter.notifyItemChanged(0);
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
        rvNoteBlocks = findViewById(R.id.rvNoteBlocks);
        drawingToolBar = findViewById(R.id.drawingToolBar);

        btnToolScroll = findViewById(R.id.btnToolScroll);
        btnToolPen = findViewById(R.id.btnToolPen);
        btnToolHighlighter = findViewById(R.id.btnToolHighlighter);
        btnToolEraser = findViewById(R.id.btnToolEraser);

        btnToolShapes = findViewById(R.id.btnToolShapes);
        btnToolUndo = findViewById(R.id.btnToolUndo);
        btnToolRedo = findViewById(R.id.btnToolRedo);
        btnToolStrokeWidth = findViewById(R.id.btnToolStrokeWidth);
        btnColorPicker = findViewById(R.id.btnColorPicker);
        btnClearCanvas = findViewById(R.id.btnClearCanvas);
        btnReservePool = findViewById(R.id.btnReservePool);

        colorBlack = findViewById(R.id.colorBlack);
        colorBlue = findViewById(R.id.colorBlue);

        btnAddTable = findViewById(R.id.btnAddTable);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnRecordVoice = findViewById(R.id.btnRecordVoice);
    }

    private void setupRecyclerView() {
        blockList = new ArrayList<>();
        blockList.add(new NoteBlockModel(NoteBlockModel.BlockType.TEXT));

        blockAdapter = new NoteBlockAdapter(this, blockList);
        rvNoteBlocks.setLayoutManager(new LinearLayoutManager(this));
        rvNoteBlocks.setAdapter(blockAdapter);
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
                blockAdapter.setToolModeToActiveCanvas(DrawingView.ToolMode.SCROLL);
            });
        }

        if (btnToolPen != null) {
            btnToolPen.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                blockAdapter.setToolModeToActiveCanvas(DrawingView.ToolMode.PEN);
            });
        }

        if (btnToolHighlighter != null) {
            btnToolHighlighter.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                blockAdapter.setToolModeToActiveCanvas(DrawingView.ToolMode.HIGHLIGHTER);
            });
        }

        if (btnToolEraser != null) {
            btnToolEraser.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                blockAdapter.setToolModeToActiveCanvas(DrawingView.ToolMode.ERASER);
            });
        }

        if (btnToolUndo != null) {
            btnToolUndo.setOnClickListener(v -> {
                if (blockAdapter != null) {
                    blockAdapter.undoActiveCanvas();
                }
            });
        }

        if (btnToolRedo != null) {
            btnToolRedo.setOnClickListener(v -> {
                if (blockAdapter != null) {
                    blockAdapter.redoActiveCanvas();
                }
            });
        }

        if (btnClearCanvas != null) {
            btnClearCanvas.setOnClickListener(v -> {
                if (blockAdapter != null) {
                    blockAdapter.clearActiveCanvas();
                }
            });
        }

        if (btnToolStrokeWidth != null) {
            btnToolStrokeWidth.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                currentStrokeWidth = (currentStrokeWidth >= 32f) ? 4f : currentStrokeWidth + 8f;
                blockAdapter.setStrokeWidthToActiveCanvas(currentStrokeWidth);
                Toast.makeText(this, "Kalınlık: " + (int) currentStrokeWidth + "px", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnToolShapes != null) {
            btnToolShapes.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                blockAdapter.setToolModeToActiveCanvas(DrawingView.ToolMode.RECTANGLE);
                Toast.makeText(this, "Dikdörtgen Çizim Modu", Toast.LENGTH_SHORT).show();
            });
        }

        if (colorBlack != null) {
            colorBlack.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                blockAdapter.setColorToActiveCanvas(0xFF09090B);
            });
        }

        if (colorBlue != null) {
            colorBlue.setOnClickListener(v -> {
                ensureDrawingBlockExists();
                blockAdapter.setColorToActiveCanvas(0xFF0284C7);
            });
        }

        // TABLO EKLEME PENCERESİNİ AÇAN MANTIK
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
                if (isVoiceRecording) {
                    blockList.add(new NoteBlockModel(NoteBlockModel.BlockType.VOICE));
                    blockAdapter.notifyItemInserted(blockList.size() - 1);
                    rvNoteBlocks.scrollToPosition(blockList.size() - 1);
                }
            });
        }
    }

    // TABLO SEÇİM XML'İNİ EKRANA DİYALOG OLARAK ÇIKARAN METOD
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

            // DÜZELTİLEN KISIM: Diyalogdan girilen satır ve sütun sayıları modele iletiliyor
            NoteBlockModel tableBlock = new NoteBlockModel(NoteBlockModel.BlockType.TABLE, rows, cols);
            blockList.add(tableBlock);
            blockAdapter.notifyItemInserted(blockList.size() - 1);
            rvNoteBlocks.scrollToPosition(blockList.size() - 1);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void ensureDrawingBlockExists() {
        boolean hasDrawingBlock = false;
        for (NoteBlockModel block : blockList) {
            if (block.getType() == NoteBlockModel.BlockType.DRAWING) {
                hasDrawingBlock = true;
                break;
            }
        }
        if (!hasDrawingBlock) {
            blockList.add(new NoteBlockModel(NoteBlockModel.BlockType.DRAWING));
            blockAdapter.notifyItemInserted(blockList.size() - 1);
            rvNoteBlocks.scrollToPosition(blockList.size() - 1);
        }
    }

    private void saveNoteAndExit() {
        String title = etNoteTitle != null ? etNoteTitle.getText().toString().trim() : "";
        String content = "İçerik yok";

        if (rvNoteBlocks != null && rvNoteBlocks.getChildCount() > 0) {
            View firstBlockView = rvNoteBlocks.getChildAt(0);
            if (firstBlockView != null) {
                EditText etBlockContent = firstBlockView.findViewById(R.id.etBlockText);
                if (etBlockContent != null && !TextUtils.isEmpty(etBlockContent.getText().toString().trim())) {
                    content = etBlockContent.getText().toString().trim();
                }
            }
        }

        if (TextUtils.isEmpty(title) && content.equals("İçerik yok")) {
            finish();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            title = "Başlıksız Not";
        }

        String currentTime = new SimpleDateFormat("dd MMM, HH:mm", new Locale("tr", "TR")).format(new Date());

        notentity note = new notentity(title, content, "Kişisel", "#0284C7", currentTime);
        note.isPinned = isPinned;

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
}