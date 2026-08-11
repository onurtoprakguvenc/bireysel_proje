package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hadi_bakalm.model.DrawingView;
import com.example.hadi_bakalm.R;

public class not_alma_sayfa extends AppCompatActivity {

    // Arayüz Elemanları
    private ImageButton btnCloseEditor, btnPinNote;
    private EditText etNoteTitle, etNoteContent;
    private LinearLayout cardVoiceNote;
    private HorizontalScrollView drawingToolBar;
    private TableLayout tableContainer;
    private DrawingView drawingCanvas;

    // Araç Çubuğu Butonları
    private ImageButton btnToolPen, btnToolHighlighter, btnToolEraser, btnClearCanvas;
    private ImageView colorBlack, colorBlue;
    private Button btnAddTable, btnAddImage, btnRecordVoice;

    // Durum Değişkenleri
    private boolean isPinned = false;
    private boolean isVoiceRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_sayfa);

        // Modern Geri Tuşu Mantığı (Deprecated onBackPressed yerine)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveNoteAndExit();
                finish();
            }
        });

        // Arayüz Elemanlarını Bağlama
        initViews();

        // Tıklama Dinleyicilerini Başlatma
        setupClickListeners();
    }

    private void initViews() {
        btnCloseEditor = findViewById(R.id.btnCloseEditor);
        btnPinNote = findViewById(R.id.btnPinNote);
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);

        cardVoiceNote = findViewById(R.id.cardVoiceNote);
        tableContainer = findViewById(R.id.tableContainer);
        drawingCanvas = findViewById(R.id.drawingCanvas);
        drawingToolBar = findViewById(R.id.drawingToolBar);

        // Çizim Araçları
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

    private void setupClickListeners() {
        // Editörden Çıkış
        if (btnCloseEditor != null) {
            btnCloseEditor.setOnClickListener(v -> {
                saveNoteAndExit();
                finish();
            });
        }

        // Notu Sabitleme
        if (btnPinNote != null) {
            btnPinNote.setOnClickListener(v -> {
                isPinned = !isPinned;
                btnPinNote.setColorFilter(isPinned ? 0xFFEAB308 : 0xFF94A3B8);
                Toast.makeText(this, isPinned ? "Not sabitlendi" : "Sabitleme kaldırıldı", Toast.LENGTH_SHORT).show();
            });
        }

        // Çizim Araçları Seçimi
        if (btnToolPen != null) {
            btnToolPen.setOnClickListener(v -> {
                if (drawingCanvas != null) drawingCanvas.setToolMode(DrawingView.ToolMode.PEN);
            });
        }

        if (btnToolHighlighter != null) {
            btnToolHighlighter.setOnClickListener(v -> {
                if (drawingCanvas != null) drawingCanvas.setToolMode(DrawingView.ToolMode.HIGHLIGHTER);
            });
        }

        if (btnToolEraser != null) {
            btnToolEraser.setOnClickListener(v -> {
                if (drawingCanvas != null) drawingCanvas.setToolMode(DrawingView.ToolMode.ERASER);
            });
        }

        if (btnClearCanvas != null) {
            btnClearCanvas.setOnClickListener(v -> {
                if (drawingCanvas != null) drawingCanvas.clearCanvas();
            });
        }

        // Renk Seçimleri
        if (colorBlack != null) {
            colorBlack.setOnClickListener(v -> {
                if (drawingCanvas != null) drawingCanvas.setColor(0xFF09090B);
            });
        }

        if (colorBlue != null) {
            colorBlue.setOnClickListener(v -> {
                if (drawingCanvas != null) drawingCanvas.setColor(0xFF0284C7);
            });
        }

        // Hızlı 3x5 Tablo Ekleme
        if (btnAddTable != null) {
            btnAddTable.setOnClickListener(v -> insertQuickTable());
        }

        // Ses Kaydı Başlatma/Durdurma
        if (btnRecordVoice != null) {
            btnRecordVoice.setOnClickListener(v -> toggleVoiceRecord());
        }

        // Görsel Ekleme
        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> {
                Toast.makeText(this, "Galeriden görsel seçici açılıyor...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // 3x5 Dinamik Tablo Oluşturma
    private void insertQuickTable() {
        if (tableContainer == null) return;

        if (tableContainer.getChildCount() > 0) {
            Toast.makeText(this, "Tablo zaten ekli", Toast.LENGTH_SHORT).show();
            return;
        }

        tableContainer.setVisibility(View.VISIBLE);

        for (int r = 0; r < 5; r++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            for (int c = 0; c < 3; c++) {
                EditText cell = new EditText(this);
                cell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                cell.setTextSize(12);
                cell.setPadding(12, 12, 12, 12);
                cell.setBackgroundResource(android.R.drawable.editbox_background);

                if (r == 0) {
                    cell.setHint("Sütun " + (c + 1));
                    cell.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    cell.setHint("...");
                }

                row.addView(cell);
            }
            tableContainer.addView(row);
        }
    }

    // Ses Kaydı Kartı Kontrolü
    private void toggleVoiceRecord() {
        if (cardVoiceNote == null || btnRecordVoice == null) return;

        isVoiceRecording = !isVoiceRecording;
        if (isVoiceRecording) {
            cardVoiceNote.setVisibility(View.VISIBLE);
            btnRecordVoice.setText("Duraklat");
            Toast.makeText(this, "Ses kaydı başladı...", Toast.LENGTH_SHORT).show();
        } else {
            btnRecordVoice.setText("Ses Kaydı");
            Toast.makeText(this, "Ses kaydı durduruldu", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNoteAndExit() {
        if (etNoteTitle == null || etNoteContent == null) return;

        String title = etNoteTitle.getText().toString().trim();
        String content = etNoteContent.getText().toString().trim();

        if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Not kaydedildi", Toast.LENGTH_SHORT).show();
        }
    }
}