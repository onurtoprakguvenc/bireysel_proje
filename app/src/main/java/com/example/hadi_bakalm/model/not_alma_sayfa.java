package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.NoteBlockAdapter;

import java.util.ArrayList;
import java.util.List;

public class not_alma_sayfa extends AppCompatActivity {

    // Arayüz Elemanları
    private ImageButton btnCloseEditor, btnPinNote;
    private EditText etNoteTitle;
    private RecyclerView rvNoteBlocks;
    private HorizontalScrollView drawingToolBar;

    // Adaptör ve Veri Listesi
    private NoteBlockAdapter blockAdapter;
    private List<NoteBlockModel> blockList;

    // Araç Çubuğu Butonları
    private ImageButton btnToolScroll, btnToolPen, btnToolHighlighter, btnToolEraser, btnClearCanvas;
    private ImageView colorBlack, colorBlue;
    private Button btnAddTable, btnAddImage, btnRecordVoice;

    // Durum Değişkenleri
    private boolean isPinned = false;
    private boolean isVoiceRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        // Modern Geri Tuşu Mantığı
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveNoteAndExit();
                finish();
            }
        });

        initViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initViews() {
        btnCloseEditor = findViewById(R.id.btnCloseEditor);
        btnPinNote = findViewById(R.id.btnPinNote);
        etNoteTitle = findViewById(R.id.etNoteTitle);
        rvNoteBlocks = findViewById(R.id.rvNoteBlocks);
        drawingToolBar = findViewById(R.id.drawingToolBar);

        // Çizim ve Gezinme Araçları
        btnToolScroll = findViewById(R.id.btnToolScroll);
        btnToolPen = findViewById(R.id.btnToolPen);
        btnToolHighlighter = findViewById(R.id.btnToolHighlighter);
        btnToolEraser = findViewById(R.id.btnToolEraser);
        btnClearCanvas = findViewById(R.id.btnClearCanvas);
        colorBlack = findViewById(R.id.colorBlack);
        colorBlue = findViewById(R.id.colorBlue);

        // Medya Butonları
        btnAddTable = findViewById(R.id.btnAddTable);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnRecordVoice = findViewById(R.id.btnRecordVoice);
    }

    private void setupRecyclerView() {
        blockList = new ArrayList<>();
        // Açılışta ilk metin bloğunu varsayılan olarak ekle
        blockList.add(new NoteBlockModel(NoteBlockModel.BlockType.TEXT));

        blockAdapter = new NoteBlockAdapter(this, blockList);
        rvNoteBlocks.setLayoutManager(new LinearLayoutManager(this));
        rvNoteBlocks.setAdapter(blockAdapter);
    }

    private void setupClickListeners() {
        if (btnCloseEditor != null) {
            btnCloseEditor.setOnClickListener(v -> {
                saveNoteAndExit();
                finish();
            });
        }

        if (btnPinNote != null) {
            btnPinNote.setOnClickListener(v -> {
                isPinned = !isPinned;
                btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
                Toast.makeText(this, isPinned ? "Not sabitlendi" : "Sabitleme kaldırıldı", Toast.LENGTH_SHORT).show();
            });
        }

        // Gezinme (Sayfa Kaydırma) Modu Butonu
        if (btnToolScroll != null) {
            btnToolScroll.setOnClickListener(v -> {
                blockAdapter.setToolModeToActiveCanvas(DrawingView.ToolMode.SCROLL);
                Toast.makeText(this, "Gezinme Modu (Sayfa Kaydırılabilir)", Toast.LENGTH_SHORT).show();
            });
        }

        // Çizim Araçları
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

        if (btnClearCanvas != null) {
            btnClearCanvas.setOnClickListener(v -> blockAdapter.clearActiveCanvas());
        }

        // Renk Seçimleri
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

        // Tablo Ekleme
        if (btnAddTable != null) {
            btnAddTable.setOnClickListener(v -> {
                blockList.add(new NoteBlockModel(NoteBlockModel.BlockType.TABLE));
                blockAdapter.notifyItemInserted(blockList.size() - 1);
                rvNoteBlocks.scrollToPosition(blockList.size() - 1);
            });
        }

        // Görsel / Galeri Butonu
        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                Toast.makeText(this, "Galeriden görsel seçici açılıyor...", Toast.LENGTH_SHORT).show();
            });
        }

        // Ses Kaydı Butonu
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
        if (etNoteTitle == null) return;
        String title = etNoteTitle.getText().toString().trim();
        if (!TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Not kaydedildi", Toast.LENGTH_SHORT).show();
        }
    }
}