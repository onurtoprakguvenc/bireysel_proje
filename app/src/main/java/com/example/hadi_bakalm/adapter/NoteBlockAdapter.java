package com.example.hadi_bakalm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hadi_bakalm.R;
import com.example.hadi_bakalm.model.DrawingView;
import com.example.hadi_bakalm.model.NoteBlockModel;

import java.util.List;

public class NoteBlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_DRAWING = 1;
    private static final int TYPE_TABLE = 2;
    private static final int TYPE_VOICE = 3;

    private Context context;
    private List<NoteBlockModel> blocks;
    private DrawingView activeDrawingCanvas;

    public NoteBlockAdapter(Context context, List<NoteBlockModel> blocks) {
        this.context = context;
        this.blocks = blocks;
    }

    @Override
    public int getItemViewType(int position) {
        switch (blocks.get(position).getType()) {
            case DRAWING:
                return TYPE_DRAWING;
            case TABLE:
                return TYPE_TABLE;
            case VOICE:
                return TYPE_VOICE;
            case TEXT:
            default:
                return TYPE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_DRAWING) {
            View view = inflater.inflate(R.layout.item_block_drawing, parent, false);
            return new DrawingViewHolder(view);
        } else if (viewType == TYPE_TABLE) {
            View view = inflater.inflate(R.layout.item_block_table, parent, false);
            return new TableViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_block_text, parent, false);
            return new TextViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteBlockModel block = blocks.get(position);

        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).etContent.setText(block.getContent());
        } else if (holder instanceof DrawingViewHolder) {
            activeDrawingCanvas = ((DrawingViewHolder) holder).drawingCanvas;
            // KAYITLI ÇİZİM VERİSİ VARSA TUVALE YÜKLENİR
            if (block.getContent() != null && !block.getContent().isEmpty()) {
                activeDrawingCanvas.loadDrawingFromJson(block.getContent());
            }
        } else if (holder instanceof TableViewHolder) {
            TableViewHolder tableHolder = (TableViewHolder) holder;
            buildTableLayout(tableHolder.tableContainer, block.getRows(), block.getCols());
        }
    }

    private void buildTableLayout(TableLayout tableLayout, int rows, int cols) {
        if (tableLayout == null) return;
        tableLayout.removeAllViews();

        int rowCount = rows > 0 ? rows : 3;
        int colCount = cols > 0 ? cols : 3;

        for (int r = 0; r < rowCount; r++) {
            TableRow tableRow = new TableRow(context);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
            ));

            for (int c = 0; c < colCount; c++) {
                EditText cell = new EditText(context);
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );
                params.setMargins(4, 4, 4, 4);
                cell.setLayoutParams(params);
                cell.setPadding(12, 12, 12, 12);
                cell.setBackgroundResource(R.drawable.bg_chip_inactive);
                cell.setTextSize(14);
                cell.setHint((r + 1) + "," + (c + 1));
                tableRow.addView(cell);
            }
            tableLayout.addView(tableRow);
        }
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    // AKTİF ÇİZİM TUVALİNİ SAYFA DÜZEYİNDE ALMAK İÇİN GETTER METODU
    public DrawingView getActiveDrawingCanvas() {
        return activeDrawingCanvas;
    }

    public void undoActiveCanvas() {
        if (activeDrawingCanvas != null) activeDrawingCanvas.undo();
    }

    public void redoActiveCanvas() {
        if (activeDrawingCanvas != null) activeDrawingCanvas.redo();
    }

    public void clearActiveCanvas() {
        if (activeDrawingCanvas != null) activeDrawingCanvas.clearCanvas();
    }

    public void setToolModeToActiveCanvas(DrawingView.ToolMode mode) {
        if (activeDrawingCanvas != null) activeDrawingCanvas.setToolMode(mode);
    }

    public void setColorToActiveCanvas(int color) {
        if (activeDrawingCanvas != null) activeDrawingCanvas.setColor(color);
    }

    public void setStrokeWidthToActiveCanvas(float width) {
        if (activeDrawingCanvas != null) activeDrawingCanvas.setStrokeWidth(width);
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        EditText etContent;
        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            etContent = itemView.findViewById(R.id.etBlockText);
        }
    }

    public static class DrawingViewHolder extends RecyclerView.ViewHolder {
        DrawingView drawingCanvas;
        public DrawingViewHolder(@NonNull View itemView) {
            super(itemView);
            drawingCanvas = itemView.findViewById(R.id.drawingCanvas);
        }
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TableLayout tableContainer;
        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableContainer = itemView.findViewById(R.id.tableContainer);
        }
    }
}