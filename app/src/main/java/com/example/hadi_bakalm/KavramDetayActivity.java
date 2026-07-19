package com.example.hadi_bakalm;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.TransitionManager;

import com.example.hadi_bakalm.adapter.ConceptAdapter;
import com.example.hadi_bakalm.data.ConceptRepository;
import com.example.hadi_bakalm.model.Concept;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KavramDetayActivity extends AppCompatActivity {

    // **kelime** kalıbını yakalayan regex: iki yıldız arasındaki her şeyi grup olarak alır
    private static final Pattern LINK_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");

    private TextView tvKavramIsmi;
    private TextView tvKavramAciklama;
    private TextView tvKisiselNot;

    private View contentOrnekDiyaloglar;
    private View contentPratikOnemi;
    private View contentVucutEtkisi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kavram_detay);

        // 1. Hangi kavrama tıklandığını Intent'ten oku
        String kavramAdi = getIntent().getStringExtra(ConceptAdapter.EXTRA_CONCEPT_NAME);

        // 2. Bu isme karşılık gelen Concept verisini merkezi depodan (repository) bul
        Concept concept = ConceptRepository.getConceptByName(kavramAdi);

        // 3. View'ları bağla
        tvKavramIsmi = findViewById(R.id.tvKavramIsmi);
        tvKavramAciklama = findViewById(R.id.tvKavramAciklama);
        tvKisiselNot = findViewById(R.id.tvKisiselNot);

        View cardOrnekDiyaloglar = findViewById(R.id.cardOrnekDiyaloglar);
        View cardPratikOnemi = findViewById(R.id.cardPratikOnemi);
        View cardVucutEtkisi = findViewById(R.id.cardVucutEtkisi);

        contentOrnekDiyaloglar = findViewById(R.id.contentOrnekDiyaloglar);
        contentPratikOnemi = findViewById(R.id.contentPratikOnemi);
        contentVucutEtkisi = findViewById(R.id.contentVucutEtkisi);

        // 4. Veriyi ekrana bas — artık setText yerine setClickableConceptText kullanılıyor
        if (concept != null) {
            tvKavramIsmi.setText(concept.getName());
            setClickableConceptText(tvKavramAciklama, concept.getAciklama());
            setClickableConceptText(tvKisiselNot, concept.getKisiselNot());
            setClickableConceptText((TextView) contentOrnekDiyaloglar, concept.getOrnekDiyaloglar());
            setClickableConceptText((TextView) contentPratikOnemi, concept.getPratikOnemi());
            setClickableConceptText((TextView) contentVucutEtkisi, concept.getVucutEtkisi());
        } else {
            tvKavramIsmi.setText(kavramAdi); // en azından ismi göster
        }

        // 5. Üç kartın da aynı genişle/daralt davranışını tek fonksiyonla bağla
        cardOrnekDiyaloglar.setOnClickListener(v -> toggleExpand(contentOrnekDiyaloglar));
        cardPratikOnemi.setOnClickListener(v -> toggleExpand(contentPratikOnemi));
        cardVucutEtkisi.setOnClickListener(v -> toggleExpand(contentVucutEtkisi));
    }

    /**
     * Verilen içerik alanı gizliyse gösterir, gösteriliyorsa gizler.
     * Tüm kartlar bu tek fonksiyonu kullanır, kart başına ayrı kod yazmaya gerek yoktur.
     */
    private void toggleExpand(View contentView) {
        TransitionManager.beginDelayedTransition((android.view.ViewGroup) contentView.getParent());
        if (contentView.getVisibility() == View.VISIBLE) {
            contentView.setVisibility(View.GONE);
        } else {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Metin içindeki **kavram** işaretlerini bulur, yıldızları temizler ve o kelimeyi
     * tıklanabilir link haline getirir. Tıklanınca, o isimde bir Concept repository'de
     * varsa, aynı ekran o kavramla yeniden açılır (Amigdala -> Kortizol -> PFC gibi zincirleme gezinme).
     *
     * concept null/boş gelirse TextView'i boş bırakır, hata vermez (kişisel not gibi
     * opsiyonel alanlar için güvenlidir).
     */
    private void setClickableConceptText(TextView textView, String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            textView.setText("");
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        Matcher matcher = LINK_PATTERN.matcher(rawText);

        int lastEnd = 0;
        while (matcher.find()) {
            // Yıldızlardan önceki normal metni olduğu gibi ekle
            builder.append(rawText, lastEnd, matcher.start());

            // Yıldızlar arasındaki kavram adı (yıldızlar temizlenmiş hali)
            final String kavramAdi = matcher.group(1);

            int spanStart = builder.length();
            builder.append(kavramAdi);
            int spanEnd = builder.length();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@androidx.annotation.NonNull View widget) {
                    // Tıklanan kelime repository'de gerçekten bir kavramsa yeni ekran aç
                    Concept hedefConcept = ConceptRepository.getConceptByName(kavramAdi);
                    if (hedefConcept != null) {
                        Intent intent = new Intent(widget.getContext(), KavramDetayActivity.class);
                        intent.putExtra(ConceptAdapter.EXTRA_CONCEPT_NAME, kavramAdi);
                        widget.getContext().startActivity(intent);
                    }
                    // Repository'de yoksa hiçbir şey yapmaz, sadece görsel olarak link gibi durur
                }
            };

            builder.setSpan(clickableSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            lastEnd = matcher.end();
        }

        // Son yıldızlı kelimeden sonra kalan normal metni ekle
        builder.append(rawText, lastEnd, rawText.length());

        textView.setText(builder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
