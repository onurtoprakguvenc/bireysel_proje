package com.example.hadi_bakalm.data;

import com.example.hadi_bakalm.model.NoteBlockModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Notlarda arama için kullanılan yardımcı sınıf.
 * "content" sütunu çoğu zaman "DRAWING_BASE64:..." önizleme verisi taşıdığından,
 * arama başlık ve tuval içindeki metin/tablo hücreleri üzerinden yapılır.
 */
public final class NoteSearchHelper {

    private static final Locale TR_LOCALE = new Locale("tr", "TR");
    private static final String DRAWING_PREFIX = "DRAWING_BASE64:";

    private NoteSearchHelper() {
        throw new UnsupportedOperationException("Bu yardımcı sınıf nesneleştirilemez.");
    }

    public static String normalizeQuery(String query) {
        return query == null ? "" : query.trim().toLowerCase(TR_LOCALE);
    }

    // normalizedQuery, normalizeQuery() ile hazırlanmış olmalıdır
    public static boolean matches(notentity note, String normalizedQuery) {
        if (note == null) return false;
        if (normalizedQuery == null || normalizedQuery.isEmpty()) return true;
        return getSearchableText(note).toLowerCase(TR_LOCALE).contains(normalizedQuery);
    }

    private static String getSearchableText(notentity note) {
        StringBuilder sb = new StringBuilder();
        if (note.title != null) {
            sb.append(note.title).append('\n');
        }

        if (note.content != null && !note.content.startsWith(DRAWING_PREFIX) && !"Çizim Notu".equals(note.content)) {
            sb.append(note.content).append('\n');
        }

        if (note.blocks != null) {
            for (NoteBlockModel block : note.blocks) {
                if (block != null && block.getType() == NoteBlockModel.BlockType.DRAWING) {
                    appendDrawingText(block.getContent(), sb);
                }
            }
        }
        return sb.toString();
    }

    private static void appendDrawingText(String drawingJson, StringBuilder sb) {
        if (drawingJson == null || !drawingJson.startsWith("{")) return;
        try {
            JSONObject mainObj = new JSONObject(drawingJson);

            JSONArray texts = mainObj.optJSONArray("texts");
            if (texts != null) {
                for (int i = 0; i < texts.length(); i++) {
                    JSONObject t = texts.optJSONObject(i);
                    if (t != null) sb.append(t.optString("text", "")).append('\n');
                }
            }

            JSONArray tables = mainObj.optJSONArray("tables");
            if (tables != null) {
                for (int i = 0; i < tables.length(); i++) {
                    JSONObject table = tables.optJSONObject(i);
                    JSONArray cells = table != null ? table.optJSONArray("cells") : null;
                    if (cells == null) continue;
                    for (int j = 0; j < cells.length(); j++) {
                        JSONObject c = cells.optJSONObject(j);
                        if (c != null) sb.append(c.optString("text", "")).append('\n');
                    }
                }
            }
        } catch (Exception ignored) {
            // Bozuk çizim verisi aramayı engellememeli
        }
    }
}
