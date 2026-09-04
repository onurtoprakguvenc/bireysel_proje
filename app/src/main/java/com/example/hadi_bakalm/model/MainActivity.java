package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.hadi_bakalm.EskiMainActivity;
import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.adapter.NoteAdapter;
import com.example.hadi_bakalm.data.NoteCleanupWorker;
import com.example.hadi_bakalm.data.not_app_database;
import com.example.hadi_bakalm.data.notdao;
import com.example.hadi_bakalm.data.notentity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Locale TR_LOCALE = new Locale("tr", "TR");
    private static final String PREFS_NAME = "NoteAppSettingsPrefs";
    private static final String KEY_SHOW_DONATE = "key_show_donate_btn";
    private static final String KEY_DARK_MODE = "key_dark_mode_enabled";
    private static final String KEY_SHOW_PREVIEWS = "key_show_note_previews";

    // Arayüz Elemanları
    private RecyclerView rvNotes;
    private EditText etSearch;
    //private ImageButton btnLibraryBridge;
    private ImageButton btnToggleLayout;
    private ImageButton btnSettings;
    private ImageButton btnCloseVault;
    private FloatingActionButton fabAddNote;
    private FloatingActionButton fabQuickNote;
    private ImageButton fabDonateCoffee;
    private TextView tvNoteCount;
    private View titleView;

    private View layoutEmptyState;
    private TextView tvEmptyStateTitle;
    private TextView tvEmptyStateSubtitle;

    private ActivityResultLauncher<String> backupExportLauncher;
    private ActivityResultLauncher<String> backupImportLauncher;

    private LinearLayout categoryChipContainer;

    // Adaptör ve Veri Yönetimi
    private NoteAdapter noteAdapter;
    private final List<NoteModel> noteList = new ArrayList<>();
    private boolean isGridMode = false;

    private boolean isVaultMode = false;

    // Room Veritabanı
    private notdao noteDao;

    private long selectedEphemeralTimestamp = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asil_ana_sayfa);

        noteDao = not_app_database.getInstance(this).noteDao();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchListener();
        setupBackupLaunchers();
        setupPeriodicCleanupWorker();

        // Telefonun geri tuşuna / geri kaydırma hareketine basıldığında:
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isVaultMode) {
                    exitVaultMode();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void setupPeriodicCleanupWorker() {
        PeriodicWorkRequest cleanupRequest = new PeriodicWorkRequest.Builder(
                NoteCleanupWorker.class,
                1, TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NoteAutoCleanupWork",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
        );
    }

    private void setupBackupLaunchers() {
        backupExportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri != null) {
                        exportAllNotesToJson(uri);
                    }
                }
        );

        backupImportLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        confirmAndImportNotesFromJson(uri);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAllNotesFromDb();
        updateDonateButtonVisibility();
    }

    private void exportAllNotesToJson(android.net.Uri uri) {
        if (noteDao == null) return;
        DB_EXECUTOR.execute(() -> {
            try {
                List<notentity> allNotes = noteDao.getAllNotes();
                org.json.JSONArray array = new org.json.JSONArray();

                for (notentity note : allNotes) {
                    org.json.JSONObject obj = new org.json.JSONObject();
                    obj.put("title", note.title);
                    obj.put("content", note.content);
                    obj.put("category", note.category);
                    obj.put("timestamp", note.timestamp);
                    obj.put("isPinned", note.isPinned);
                    obj.put("isEphemeral", note.isEphemeral);
                    obj.put("expireTimestamp", note.expireTimestamp);
                    obj.put("isLocked", note.isLocked);
                    obj.put("inVault", note.inVault);

                    // GSON YERİNE ANDROID'İN KENDİ YERLİ JSON YAPISI:
                    if (note.blocks != null) {
                        org.json.JSONArray blocksArray = new org.json.JSONArray();
                        for (NoteBlockModel b : note.blocks) {
                            if (b != null) {
                                org.json.JSONObject bObj = new org.json.JSONObject();
                                bObj.put("type", b.getType() != null ? b.getType().name() : "DRAWING");
                                bObj.put("content", b.getContent() != null ? b.getContent() : "");
                                blocksArray.put(bObj);
                            }
                        }
                        obj.put("blocks", blocksArray);
                    }
                    array.put(obj);
                }

                java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                if (os != null) {
                    os.write(array.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    os.flush();
                    os.close();
                    runOnUiThread(() -> Toast.makeText(this, allNotes.size() + " adet not başarıyla yedeklendi", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Yedekleme başarısız oldu", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmAndImportNotesFromJson(android.net.Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Yedeği Geri Yükle")
                .setMessage("Yedek dosyasındaki tüm notlar mevcut notlarınıza eklenecektir. Onaylıyor musunuz?")
                .setPositiveButton("Geri Yükle", (dialog, which) -> importNotesFromJson(uri))
                .setNegativeButton("İptal", null)
                .show();
    }

    private void importNotesFromJson(android.net.Uri uri) {
        if (noteDao == null) return;
        DB_EXECUTOR.execute(() -> {
            try {
                java.io.InputStream is = getContentResolver().openInputStream(uri);
                if (is == null) return;

                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                is.close();

                String jsonStr = new String(buffer.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
                org.json.JSONArray array = new org.json.JSONArray(jsonStr);
                int importedCount = 0;

                for (int i = 0; i < array.length(); i++) {
                    org.json.JSONObject obj = array.getJSONObject(i);
                    String title = obj.optString("title", "Başlıksız Not");
                    String content = obj.optString("content", "");
                    String category = obj.optString("category", "Genel");
                    String color = obj.optString("color", "#0284C7");
                    String timestamp = obj.optString("timestamp", "");

                    notentity newNote = new notentity(title, content, category, color, timestamp);
                    newNote.isPinned = obj.optBoolean("isPinned", false);
                    newNote.isEphemeral = obj.optBoolean("isEphemeral", false);
                    newNote.expireTimestamp = obj.optLong("expireTimestamp", 0L);
                    newNote.isLocked = obj.optBoolean("isLocked", false);
                    newNote.inVault = obj.optBoolean("inVault", false);

                    if (obj.has("blocks")) {
                        String blocksJson = obj.getString("blocks");
                        java.lang.reflect.Type blockListType = new com.google.gson.reflect.TypeToken<List<NoteBlockModel>>(){}.getType();
                        newNote.blocks = new com.google.gson.Gson().fromJson(blocksJson, blockListType);
                    }

                    noteDao.insertNote(newNote);
                    importedCount++;
                }

                final int finalCount = importedCount;
                runOnUiThread(() -> {
                    refreshAllNotesFromDb();
                    Toast.makeText(this, finalCount + " adet not başarıyla geri yüklendi", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Yedek dosyası okunamadı", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void refreshAllNotesFromDb() {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            long now = System.currentTimeMillis();
            noteDao.moveExpiredNotesToTrash(now);

            List<notentity> dbEntities = noteDao.getAllNotes();
            List<notentity> filteredEntities = new ArrayList<>();

            for (notentity entity : dbEntities) {
                if (entity != null) {
                    if (isVaultMode && entity.inVault) {
                        filteredEntities.add(entity);
                    } else if (!isVaultMode && !entity.inVault) {
                        filteredEntities.add(entity);
                    }
                }
            }

            List<NoteModel> updatedList = mapEntitiesToModels(filteredEntities);
            List<String> dynamicCategories = extractDynamicCategories(filteredEntities);

            runOnUiThread(() -> {
                if (titleView instanceof TextView) {
                    ((TextView) titleView).setText(isVaultMode ? "Gizli Kasa" : "Notlarım");
                }
                if (btnCloseVault != null) {
                    btnCloseVault.setVisibility(isVaultMode ? View.VISIBLE : View.GONE);
                }

                applyListUpdate(updatedList);
                renderCategoryChips(dynamicCategories);
            });
        });
    }

    private void renderCategoryChips(List<String> dynamicCategories) {
        if (categoryChipContainer == null) return;

        categoryChipContainer.removeAllViews();

        for (String categoryName : dynamicCategories) {
            TextView chip = new TextView(this);
            chip.setText(categoryName);
            chip.setTextSize(12f);
            chip.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, dpToPx(6), 0);
            chip.setLayoutParams(params);

            if (categoryName.equalsIgnoreCase("Tümü")) {
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }

            chip.setOnClickListener(v -> {
                resetChipStyles();
                chip.setBackgroundResource(R.drawable.bg_chip_active);
                chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                if (categoryName.equalsIgnoreCase("Tümü")) {
                    refreshAllNotesFromDb();
                } else {
                    filterNotesByCategory(categoryName);
                }
            });

            categoryChipContainer.addView(chip);
        }
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rvNotes);
        etSearch = findViewById(R.id.etSearch);
        btnToggleLayout = findViewById(R.id.btnToggleLayout);
        btnSettings = findViewById(R.id.btnSettings);
        fabAddNote = findViewById(R.id.fabAddNote);
        fabQuickNote = findViewById(R.id.fabQuickNote);
        fabDonateCoffee = findViewById(R.id.fabDonateCoffee);
        categoryChipContainer = findViewById(R.id.categoryChipContainer);
        titleView = findViewById(R.id.tvTitle);
        btnCloseVault = findViewById(R.id.btnCloseVault);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyStateTitle = findViewById(R.id.tvEmptyStateTitle);
        tvEmptyStateSubtitle = findViewById(R.id.tvEmptyStateSubtitle);

        if (btnCloseVault != null) {
            btnCloseVault.setVisibility(View.GONE);
        }

        updateDonateButtonVisibility();
    }

    private void updateDonateButtonVisibility() {
        boolean showDonate = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SHOW_DONATE, true);
        if (fabDonateCoffee != null) {
            fabDonateCoffee.setVisibility(showDonate ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyStateUI(boolean isEmpty) {
        if (layoutEmptyState == null || rvNotes == null) return;

        if (isEmpty) {
            rvNotes.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

            String searchQ = (etSearch != null && etSearch.getText() != null) ? etSearch.getText().toString().trim() : "";

            if (isVaultMode) {
                if (tvEmptyStateTitle != null) tvEmptyStateTitle.setText("Gizli Kasa Boş");
                if (tvEmptyStateSubtitle != null) tvEmptyStateSubtitle.setText("Bu alana henüz gizlenmiş bir not eklemediniz.");
            } else if (!searchQ.isEmpty()) {
                if (tvEmptyStateTitle != null) tvEmptyStateTitle.setText("Sonuç Bulunamadı");
                if (tvEmptyStateSubtitle != null) tvEmptyStateSubtitle.setText("\"" + searchQ + "\" aramasıyla eşleşen bir not yok.");
            } else {
                if (tvEmptyStateTitle != null) tvEmptyStateTitle.setText("Henüz Not Yok");
                if (tvEmptyStateSubtitle != null) tvEmptyStateSubtitle.setText("Yeni bir not veya çizim eklemek için aşağıdaki + butonuna dokunun.");
            }
        } else {
            rvNotes.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        noteAdapter = new NoteAdapter(noteList);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NoteModel note, int position) {
                if (note == null) return;

                if (note.isLocked()) {
                    promptForPassword(() -> openNoteEditor(note));
                } else {
                    openNoteEditor(note);
                }
            }

            @Override
            public void onPinClick(NoteModel note, int position) {
                if (note == null) return;
                boolean newPinnedState = !note.isPinned();
                note.setPinned(newPinnedState);
                toggleNotePinInDatabase(note.getId(), newPinnedState);
            }

            @Override
            public void onDeleteClick(NoteModel note, int position) {
                if (note == null) return;
                deleteNoteFromDatabase(note.getId());
            }

            @Override
            public void onLockClick(NoteModel note, int position) {
                if (note == null) return;
                toggleNoteLockInDatabase(note);
            }
        });

        if (rvNotes != null) {
            rvNotes.setLayoutManager(new LinearLayoutManager(this));
            rvNotes.setAdapter(noteAdapter);
        }

        // Önizleme ayarını SharedPreferences'tan oku ve adaptöre aktar:
        boolean showPreviews = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SHOW_PREVIEWS, true);
        if (noteAdapter != null) {
            noteAdapter.setShowPreviews(showPreviews);
        }
    }

    private void toggleNoteLockInDatabase(NoteModel note) {
        if (note == null || noteDao == null) return;

        if (!note.isLocked()) {
            if (!SecurityHelper.isPasswordSet(this)) {
                showSetupPasswordDialog(() -> performLockNote(note.getId(), true));
            } else {
                performLockNote(note.getId(), true);
            }
        } else {
            promptForPassword(() -> performLockNote(note.getId(), false));
        }
    }

    private void performLockNote(int noteId, boolean lock) {
        DB_EXECUTOR.execute(() -> {
            notentity entity = noteDao.getNoteById(noteId);
            if (entity != null) {
                entity.isLocked = lock;
                noteDao.updateNote(entity);
            }
            runOnUiThread(this::refreshAllNotesFromDb);
        });
    }

    private void deleteNoteFromDatabase(int noteId) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.moveToTrash(noteId, System.currentTimeMillis());

            runOnUiThread(() -> {
                refreshAllNotesFromDb();

                if (rvNotes != null) {
                    Snackbar.make(rvNotes, "Not çöp kutusuna taşındı", Snackbar.LENGTH_LONG)
                            .setAction("Geri Al", v -> restoreNote(noteId))
                            .setActionTextColor(Color.parseColor("#38BDF8"))
                            .show();
                }
            });
        });
    }

    private void restoreNote(int noteId) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.restoreNoteFromTrash(noteId);
            runOnUiThread(this::refreshAllNotesFromDb);
        });
    }

    private void toggleNotePinInDatabase(int noteId, boolean isPinned) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            try {
                noteDao.updatePinStatus(noteId, isPinned);
            } catch (Exception ignored) {}

            runOnUiThread(this::refreshAllNotesFromDb);
        });
    }

    private void setupClickListeners() {
        //  if (btnLibraryBridge != null) {
        //      btnLibraryBridge.setOnClickListener(v -> openLibraryActivity());
        //  }

        if (btnToggleLayout != null) {
            btnToggleLayout.setOnClickListener(v -> {
                isGridMode = !isGridMode;
                if (rvNotes != null) {
                    if (isGridMode) {
                        rvNotes.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                        btnToggleLayout.setImageResource(R.drawable.ic_list);
                    } else {
                        rvNotes.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        btnToggleLayout.setImageResource(R.drawable.ic_grid);
                    }
                }
            });
        }

        if (btnCloseVault != null) {
            btnCloseVault.setOnClickListener(v -> exitVaultMode());
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> showNoteAppSettingsDialog());
        }

        if (fabAddNote != null) {
            fabAddNote.setOnClickListener(v -> showCustomNoteCreationDialog());
        }

        if (titleView != null) {
            titleView.setOnLongClickListener(v -> {
                if (isVaultMode) {
                    exitVaultMode();
                } else {
                    openVaultWithAuth();
                }
                return true;
            });
        }

        if (fabQuickNote != null) {
            fabQuickNote.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
                String timeStamp = new SimpleDateFormat("dd MMM, HH:mm", TR_LOCALE).format(new Date());
                String defaultTitle = "Hızlı Not (" + timeStamp + ")";

                intent.putExtra("EXTRA_NOTE_TITLE", defaultTitle);
                intent.putExtra("EXTRA_NOTE_CATEGORY", isVaultMode ? "Gizli Kasa" : "Hızlı Not");
                intent.putExtra("EXTRA_NOTE_ID", -1);
                intent.putExtra("EXTRA_IN_VAULT", isVaultMode);
                startActivity(intent);
            });
        }

        if (fabDonateCoffee != null) {
            fabDonateCoffee.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, not_bagis_sayfa.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void exitVaultMode() {
        isVaultMode = false;
        refreshAllNotesFromDb();
        Toast.makeText(this, "Gizli Kasa kilitlendi", Toast.LENGTH_SHORT).show();
    }

    private void openVaultWithAuth() {
        promptForPassword(() -> {
            isVaultMode = true;
            refreshAllNotesFromDb();
            Toast.makeText(this, "Gizli Kasa Açıldı", Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("InflateParams")
    private void showNoteAppSettingsDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.not_ayarlar, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageButton btnCloseSettings = dialogView.findViewById(R.id.btnCloseSettings);
        MaterialSwitch switchDarkMode = dialogView.findViewById(R.id.switchDarkMode);
        MaterialSwitch switchShowDonate = dialogView.findViewById(R.id.switchShowDonate);
        MaterialSwitch switchShowPreviews = dialogView.findViewById(R.id.switchShowPreviews); // <-- EKLENDİ
        LinearLayout rowOpenTrashPage = dialogView.findViewById(R.id.rowOpenTrashPage);
        LinearLayout rowEmptyTrashDirect = dialogView.findViewById(R.id.rowEmptyTrashDirect);
        LinearLayout rowOpenDonatePage = dialogView.findViewById(R.id.rowOpenDonatePage);
        LinearLayout rowExportBackup = dialogView.findViewById(R.id.rowExportBackup);
        LinearLayout rowImportBackup = dialogView.findViewById(R.id.rowImportBackup);

        if (rowOpenTrashPage != null) {
            rowOpenTrashPage.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, GeriDonusumActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (rowExportBackup != null) {
            rowExportBackup.setOnClickListener(v -> {
                dialog.dismiss();
                backupExportLauncher.launch("hadi_bakalim_yedek_" + System.currentTimeMillis() + ".json");
            });
        }

        if (rowImportBackup != null) {
            rowImportBackup.setOnClickListener(v -> {
                dialog.dismiss();
                backupImportLauncher.launch("application/json");
            });
        }

        boolean isDonateVisible = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SHOW_DONATE, true);
        if (switchShowDonate != null) {
            switchShowDonate.setChecked(isDonateVisible);
            switchShowDonate.setOnCheckedChangeListener((btn, isChecked) -> {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_SHOW_DONATE, isChecked).apply();
                updateDonateButtonVisibility();
            });
        }

        boolean isDarkMode = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_DARK_MODE, false);
        if (switchDarkMode != null) {
            switchDarkMode.setChecked(isDarkMode);
            switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });
        }

        // Önizleme Switch Kontrolü:
        boolean isPreviewsActive = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean(KEY_SHOW_PREVIEWS, true);
        if (switchShowPreviews != null) {
            switchShowPreviews.setChecked(isPreviewsActive);
            switchShowPreviews.setOnCheckedChangeListener((btn, isChecked) -> {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putBoolean(KEY_SHOW_PREVIEWS, isChecked).apply();
                if (noteAdapter != null) {
                    noteAdapter.setShowPreviews(isChecked);
                }
            });
        }

        if (rowEmptyTrashDirect != null) {
            rowEmptyTrashDirect.setOnClickListener(v -> {
                dialog.dismiss();
                confirmEmptyTrashDirectly();
            });
        }

        if (rowOpenDonatePage != null) {
            rowOpenDonatePage.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, not_bagis_sayfa.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (btnCloseSettings != null) {
            btnCloseSettings.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void promptForPassword(Runnable onSuccess) {
        if (!SecurityHelper.isPasswordSet(this)) {
            showSetupPasswordDialog(onSuccess);
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 16);

        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("Kasa Parolanızı Girin");
        inputPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Kasa Parolası")
                .setView(layout)
                .setPositiveButton("Aç", (dialog, which) -> {
                    String pass = inputPassword.getText().toString();
                    if (SecurityHelper.checkPassword(this, pass)) {
                        if (onSuccess != null) onSuccess.run();
                    } else {
                        Toast.makeText(this, "Hatalı parola!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Şifremi Unuttum", (dialog, which) -> showForgotPasswordDialog(onSuccess))
                .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Biyometrik varsa diyalog içine tıklanabilir bağlantı eklenir (İptal butonu ezilmez)
        if (BiometricHelper.canAuthenticate(this)) {
            TextView btnBiometric = new TextView(this);
            btnBiometric.setText("Parmak İzi / Desen ile Aç");
            btnBiometric.setTextColor(Color.parseColor("#0284C7"));
            btnBiometric.setTextSize(14f);
            btnBiometric.setPadding(0, 24, 0, 8);
            btnBiometric.setOnClickListener(v -> {
                dialog.dismiss();
                BiometricHelper.showBiometricPrompt(
                        this,
                        "Kasa Doğrulaması",
                        "Parmak izinizi veya ekran kilidinizi kullanın",
                        new BiometricHelper.BiometricCallback() {
                            @Override
                            public void onSuccess() {
                                if (onSuccess != null) onSuccess.run();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(MainActivity.this, "Doğrulama başarısız", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            });
            layout.addView(btnBiometric);
        }

        dialog.show();
    }

    private void showSetupPasswordDialog(Runnable onSuccess) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etPass = new EditText(this);
        etPass.setHint("Yeni Kasa Parolası");
        layout.addView(etPass);

        final EditText etQuestion = new EditText(this);
        etQuestion.setHint("Güvenlik Sorusu (Örn: İlk evcil hayvanınız?)");
        layout.addView(etQuestion);

        final EditText etAnswer = new EditText(this);
        etAnswer.setHint("Güvenlik Sorusu Cevabı");
        layout.addView(etAnswer);

        new AlertDialog.Builder(this)
                .setTitle("Kasa Parolası Oluştur")
                .setMessage("Notlarınızı kilitlemek için bir parola ve kurtarma sorusu belirleyin.")
                .setView(layout)
                .setPositiveButton("Kaydet", (dialog, which) -> {
                    String p = etPass.getText().toString().trim();
                    String q = etQuestion.getText().toString().trim();
                    String a = etAnswer.getText().toString().trim();

                    if (!p.isEmpty() && !q.isEmpty() && !a.isEmpty()) {
                        SecurityHelper.setPasswordAndQuestion(this, p, q, a);
                        Toast.makeText(this, "Kasa parolası oluşturuldu", Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) onSuccess.run();
                    } else {
                        Toast.makeText(this, "Tüm alanları doldurmalısınız!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void showForgotPasswordDialog(Runnable onSuccess) {
        String question = SecurityHelper.getSecurityQuestion(this);

        final EditText etAnswer = new EditText(this);
        etAnswer.setHint("Cevabınız");
        etAnswer.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Parola Sıfırlama")
                .setMessage("Güvenlik Sorusu: " + question)
                .setView(etAnswer)
                .setPositiveButton("Doğrula", (dialog, which) -> {
                    String ans = etAnswer.getText().toString().trim();
                    if (SecurityHelper.checkSecurityAnswer(this, ans)) {
                        showNewPasswordOnlyDialog(onSuccess);
                    } else {
                        Toast.makeText(this, "Güvenlik sorusu cevabı yanlış!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    private void showNewPasswordOnlyDialog(Runnable onSuccess) {
        final EditText etNewPass = new EditText(this);
        etNewPass.setHint("Yeni Parolanızı Girin");
        etNewPass.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Yeni Parola Belirleyin")
                .setView(etNewPass)
                .setPositiveButton("Güncelle", (dialog, which) -> {
                    String newP = etNewPass.getText().toString().trim();
                    if (!newP.isEmpty()) {
                        SecurityHelper.resetPassword(this, newP);
                        Toast.makeText(this, "Parolanız güncellendi!", Toast.LENGTH_SHORT).show();
                        if (onSuccess != null) onSuccess.run();
                    }
                })
                .show();
    }

    private void confirmEmptyTrashDirectly() {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            noteDao.emptyTrash();
            runOnUiThread(() -> {
                Toast.makeText(this, "Çöp kutusu tamamen boşaltıldı", Toast.LENGTH_SHORT).show();
                refreshAllNotesFromDb();
            });
        });
    }

    @SuppressLint("InflateParams")
    private void showCustomNoteCreationDialog() {
        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.not_ekleme, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etNewNoteTitle = dialogView.findViewById(R.id.etNewNoteTitle);
        ImageButton btnCloseDialog = dialogView.findViewById(R.id.btnCloseDialog);
        TextView btnCancelDialog = dialogView.findViewById(R.id.btnCancelDialog);

        RelativeLayout cardCategoryPersonal = dialogView.findViewById(R.id.cardCategoryPersonal);
        RelativeLayout cardCategoryEphemeral = dialogView.findViewById(R.id.cardCategoryEphemeral);
        RelativeLayout cardCategoryWork = dialogView.findViewById(R.id.cardCategoryWork);
        TextView btnAddCustomCategory = dialogView.findViewById(R.id.btnAddCustomCategory);

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnCancelDialog != null) {
            btnCancelDialog.setOnClickListener(v -> dialog.dismiss());
        }

        if (cardCategoryPersonal != null) {
            cardCategoryPersonal.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                openNoteEditor(title, "Kişisel");
                dialog.dismiss();
            });
        }

        if (cardCategoryEphemeral != null) {
            cardCategoryEphemeral.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                dialog.dismiss();
                showEphemeralDurationDialog(title);
            });
        }

        if (cardCategoryWork != null) {
            cardCategoryWork.setOnClickListener(v -> {
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                openNoteEditor(title, "Fikir & Taslak");
                dialog.dismiss();
            });
        }

        if (btnAddCustomCategory != null) {
            btnAddCustomCategory.setOnClickListener(v -> {
                dialog.dismiss();
                String title = etNewNoteTitle != null ? etNewNoteTitle.getText().toString().trim() : "";
                showCustomCategoryInputDialog(title);
            });
        }

        dialog.show();
    }

    private void showCustomCategoryInputDialog(String prefilledTitle) {
        final EditText input = new EditText(this);
        input.setHint("Kategori adını girin (Örn: Proje, Alışveriş)");
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Yeni Kategori İsmi")
                .setView(input)
                .setPositiveButton("Oluştur ve Not Aç", (dialog, which) -> {
                    String customCategory = input.getText().toString().trim();
                    if (customCategory.isEmpty()) {
                        customCategory = "Genel";
                    }
                    openNoteEditor(prefilledTitle, customCategory);
                })
                .setNegativeButton("İptal", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void openNoteEditor(NoteModel note) {
        if (note == null) return;
        Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
        intent.putExtra("EXTRA_NOTE_ID", note.getId());
        intent.putExtra("EXTRA_NOTE_TITLE", note.getTitle());
        intent.putExtra("EXTRA_NOTE_CONTENT", note.getContent());
        intent.putExtra("EXTRA_NOTE_CATEGORY", note.getCategory());
        intent.putExtra("EXTRA_IN_VAULT", note.isInVault());
        startActivity(intent);
    }

    private void openNoteEditor(String title, String category) {
        Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
        if (!title.isEmpty()) {
            intent.putExtra("EXTRA_NOTE_TITLE", title);
        }
        intent.putExtra("EXTRA_NOTE_CATEGORY", category);
        intent.putExtra("EXTRA_IN_VAULT", isVaultMode);
        startActivity(intent);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void showEphemeralDurationDialog(String noteTitle) {
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

        selectedEphemeralTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);

        btnQuick1Hour.setOnClickListener(v -> {
            selectedEphemeralTimestamp = System.currentTimeMillis() + (60 * 60 * 1000L);
            setDurationTabActive(btnQuick1Hour, btnQuick24Hours, btnQuick3Days);
            txtSelectedExpiryInfo.setText("Süre: 1 Saat sonra geri dönüşüme taşınacak");
        });

        btnQuick24Hours.setOnClickListener(v -> {
            selectedEphemeralTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
            setDurationTabActive(btnQuick24Hours, btnQuick1Hour, btnQuick3Days);
            txtSelectedExpiryInfo.setText("Süre: 24 Saat sonra geri dönüşüme taşınacak");
        });

        btnQuick3Days.setOnClickListener(v -> {
            selectedEphemeralTimestamp = System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000L);
            setDurationTabActive(btnQuick3Days, btnQuick1Hour, btnQuick24Hours);
            txtSelectedExpiryInfo.setText("Süre: 3 Gün sonra geri dönüşüme taşınacak");
        });

        btnCustomDateTime.setOnClickListener(v -> {
            Calendar takvim = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                takvim.set(Calendar.YEAR, year);
                takvim.set(Calendar.MONTH, month);
                takvim.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePicker = new TimePickerDialog(this, (tView, hourOfDay, minute) -> {
                    takvim.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    takvim.set(Calendar.MINUTE, minute);
                    takvim.set(Calendar.SECOND, 0);

                    long chosen = takvim.getTimeInMillis();
                    if (chosen > System.currentTimeMillis()) {
                        selectedEphemeralTimestamp = chosen;
                        resetDurationTabs(btnQuick1Hour, btnQuick24Hours, btnQuick3Days);
                        txtSelectedExpiryInfo.setText("Bitiş: " + dayOfMonth + "/" + (month + 1) + "/" + year + " " + String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    } else {
                        Toast.makeText(this, "Geçmiş bir zaman seçemezsiniz!", Toast.LENGTH_SHORT).show();
                    }
                }, takvim.get(Calendar.HOUR_OF_DAY), takvim.get(Calendar.MINUTE), true);
                timePicker.show();
            }, takvim.get(Calendar.YEAR), takvim.get(Calendar.MONTH), takvim.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnCancelExpiry.setOnClickListener(v -> dialog.dismiss());

        btnConfirmExpiry.setOnClickListener(v -> {
            dialog.dismiss();
            openEphemeralNoteEditor(noteTitle, selectedEphemeralTimestamp);
        });

        dialog.show();
    }

    private void setDurationTabActive(TextView active, TextView... inactives) {
        active.setBackgroundResource(R.drawable.bg_black_pill);
        active.setTextColor(Color.WHITE);
        for (TextView in : inactives) {
            in.setBackgroundResource(R.drawable.bg_chip_inactive);
            in.setTextColor(Color.parseColor("#475569"));
        }
    }

    private void resetDurationTabs(TextView... tabs) {
        for (TextView t : tabs) {
            t.setBackgroundResource(R.drawable.bg_chip_inactive);
            t.setTextColor(Color.parseColor("#475569"));
        }
    }

    private void openEphemeralNoteEditor(String title, long expireTimestamp) {
        Intent intent = new Intent(MainActivity.this, not_alma_sayfa.class);
        if (!title.isEmpty()) {
            intent.putExtra("EXTRA_NOTE_TITLE", title);
        }
        intent.putExtra("EXTRA_NOTE_CATEGORY", "Geçici");
        intent.putExtra("EXTRA_IS_EPHEMERAL", true);
        intent.putExtra("EXTRA_EXPIRE_TIMESTAMP", expireTimestamp);
        intent.putExtra("EXTRA_IN_VAULT", isVaultMode);
        startActivity(intent);
    }

    private void setupSearchListener() {
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = (s != null) ? s.toString() : "";
                filterNotesFromDatabase(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterNotesFromDatabase(String query) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> dbEntities;
            if (query.trim().isEmpty()) {
                dbEntities = noteDao.getAllNotes();
            } else {
                dbEntities = noteDao.searchNotes(query);
            }

            List<notentity> filtered = new ArrayList<>();
            for (notentity entity : dbEntities) {
                if (entity != null) {
                    if (isVaultMode && entity.inVault) {
                        filtered.add(entity);
                    } else if (!isVaultMode && !entity.inVault) {
                        filtered.add(entity);
                    }
                }
            }

            List<NoteModel> filteredList = mapEntitiesToModels(filtered);
            runOnUiThread(() -> applyListUpdate(filteredList));
        });
    }

    private List<String> extractDynamicCategories(List<notentity> allNotes) {
        List<String> dynamicCategories = new ArrayList<>();
        dynamicCategories.add("Tümü");
        dynamicCategories.add("Kişisel");
        dynamicCategories.add("Geçici");

        if (allNotes != null) {
            for (notentity note : allNotes) {
                if (note != null && note.category != null && !note.category.trim().isEmpty()) {
                    String cat = note.category.trim();
                    if (!dynamicCategories.contains(cat)) {
                        dynamicCategories.add(cat);
                    }
                }
            }
        }
        return dynamicCategories;
    }

    private void filterNotesByCategory(String category) {
        if (noteDao == null) return;

        DB_EXECUTOR.execute(() -> {
            List<notentity> dbEntities = noteDao.getNotesByCategory(category);
            List<notentity> filtered = new ArrayList<>();
            for (notentity entity : dbEntities) {
                if (entity != null) {
                    if (isVaultMode && entity.inVault) {
                        filtered.add(entity);
                    } else if (!isVaultMode && !entity.inVault) {
                        filtered.add(entity);
                    }
                }
            }
            List<NoteModel> filteredList = mapEntitiesToModels(filtered);

            runOnUiThread(() -> applyListUpdate(filteredList));
        });
    }

    private List<NoteModel> mapEntitiesToModels(List<notentity> dbEntities) {
        List<NoteModel> models = new ArrayList<>();
        if (dbEntities != null) {
            for (notentity entity : dbEntities) {
                if (entity != null) {
                    NoteModel model = new NoteModel(
                            entity.id,
                            entity.title,
                            entity.content,
                            entity.timestamp,
                            entity.category,
                            entity.isPinned,
                            entity.isEphemeral,
                            entity.expireTimestamp
                    );
                    model.setLocked(entity.isLocked);
                    model.setInVault(entity.inVault);
                    models.add(model);
                }
            }
        }
        return models;
    }

    private void applyListUpdate(List<NoteModel> newList) {
        noteList.clear();
        noteList.addAll(newList);
        if (noteAdapter != null) {
            noteAdapter.updateList(noteList);
        }
        updateNoteCount(noteList.size());
        updateEmptyStateUI(noteList.isEmpty());
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void resetChipStyles() {
        for (int i = 0; i < categoryChipContainer.getChildCount(); i++) {
            View child = categoryChipContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setBackgroundResource(R.drawable.bg_chip_inactive);
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    private void openLibraryActivity() {
        Intent intent = new Intent(MainActivity.this, EskiMainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateNoteCount(int count) {
        if (tvNoteCount != null) {
            tvNoteCount.setText(String.format(TR_LOCALE, "Toplam %d kayıtlı not", count));
        }
    }
}