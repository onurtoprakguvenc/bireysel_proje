package com.example.hadi_bakalm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

    // Aktif çizim alanını takip etmek için referans
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
        }
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    // Activity tarafından çağrılan yardımcı metotlar
    public void setToolModeToActiveCanvas(DrawingView.ToolMode mode) {
        if (activeDrawingCanvas != null) {
            activeDrawingCanvas.setToolMode(mode);
        }
    }

    public void setColorToActiveCanvas(int color) {
        if (activeDrawingCanvas != null) {
            activeDrawingCanvas.setColor(color);
        }
    }

    public void clearActiveCanvas() {
        if (activeDrawingCanvas != null) {
            activeDrawingCanvas.clearCanvas();
        }
    }

    // ViewHolder Sınıfları
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
}