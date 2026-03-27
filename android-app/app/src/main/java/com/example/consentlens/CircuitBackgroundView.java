package com.example.consentlens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class CircuitBackgroundView extends View {

    private Paint bgPaint;
    private Paint glowPaint;
    private Paint tracePaint;
    private Paint nodePaint;
    private Paint nodeRingPaint;
    private Paint nodeRing2Paint;

    // Fixed circuit node positions as fractions of width/height
    private final float[][] nodes = {
            {0.15f, 0.18f},
            {0.40f, 0.12f},
            {0.68f, 0.22f},
            {0.85f, 0.10f},
            {0.25f, 0.40f},
            {0.55f, 0.35f},
            {0.78f, 0.45f},
            {0.10f, 0.60f},
            {0.38f, 0.62f},
            {0.62f, 0.58f},
            {0.90f, 0.65f},
            {0.20f, 0.80f},
            {0.50f, 0.78f},
            {0.75f, 0.85f},
            {0.92f, 0.88f},
    };

    // Trace connections: pairs of node indices
    private final int[][] traces = {
            {0, 1}, {1, 2}, {2, 3},
            {0, 4}, {1, 5}, {2, 6},
            {4, 5}, {5, 6},
            {4, 7}, {5, 8}, {6, 9}, {6, 10},
            {7, 8}, {8, 9}, {9, 10},
            {7, 11}, {8, 12}, {9, 13}, {10, 14},
            {11, 12}, {12, 13}, {13, 14},
            {3, 6}, {11, 14},
    };

    public CircuitBackgroundView(Context context) {
        super(context);
        init();
    }

    public CircuitBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircuitBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint();
        glowPaint.setStyle(Paint.Style.FILL);

        tracePaint = new Paint();
        tracePaint.setStyle(Paint.Style.STROKE);
        tracePaint.setStrokeWidth(1.5f);
        tracePaint.setColor(0x3300DC8C);
        tracePaint.setAntiAlias(true);
        tracePaint.setStrokeCap(Paint.Cap.ROUND);
        tracePaint.setStrokeJoin(Paint.Join.ROUND);

        nodePaint = new Paint();
        nodePaint.setStyle(Paint.Style.FILL);
        nodePaint.setColor(0xCC00DC8C);
        nodePaint.setAntiAlias(true);

        nodeRingPaint = new Paint();
        nodeRingPaint.setStyle(Paint.Style.STROKE);
        nodeRingPaint.setStrokeWidth(1f);
        nodeRingPaint.setColor(0x5500DC8C);
        nodeRingPaint.setAntiAlias(true);

        nodeRing2Paint = new Paint();
        nodeRing2Paint.setStyle(Paint.Style.STROKE);
        nodeRing2Paint.setStrokeWidth(0.8f);
        nodeRing2Paint.setColor(0x2200DC8C);
        nodeRing2Paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // ── Base background: very dark navy ──
        bgPaint.setColor(0xFF06101A);
        canvas.drawRect(0, 0, w, h, bgPaint);

        // ── Top radial glow (teal tint) ──
        RadialGradient topGlow = new RadialGradient(
                w * 0.5f, 0,
                h * 0.6f,
                new int[]{0x220D3040, 0x00060E18},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(topGlow);
        canvas.drawRect(0, 0, w, h, glowPaint);

        // ── Bottom-left purple glow ──
        RadialGradient purpleGlow = new RadialGradient(
                0, h,
                h * 0.55f,
                new int[]{0x183C0A60, 0x00020309},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(purpleGlow);
        canvas.drawRect(0, 0, w, h, glowPaint);

        // ── Right green accent glow ──
        RadialGradient greenGlow = new RadialGradient(
                w, h * 0.4f,
                h * 0.45f,
                new int[]{0x10004030, 0x00020309},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        glowPaint.setShader(greenGlow);
        canvas.drawRect(0, 0, w, h, glowPaint);

        glowPaint.setShader(null);

        // ── Circuit traces (L-shaped paths between nodes) ──
        Path path = new Path();
        for (int[] trace : traces) {
            float x1 = nodes[trace[0]][0] * w;
            float y1 = nodes[trace[0]][1] * h;
            float x2 = nodes[trace[1]][0] * w;
            float y2 = nodes[trace[1]][1] * h;

            path.reset();
            path.moveTo(x1, y1);

            // Decide L-bend direction based on which node is further
            if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
                // Horizontal-first L-bend
                float midX = x1 + (x2 - x1) * 0.6f;
                path.lineTo(midX, y1);
                path.lineTo(midX, y2);
                path.lineTo(x2, y2);
            } else {
                // Vertical-first L-bend
                float midY = y1 + (y2 - y1) * 0.6f;
                path.lineTo(x1, midY);
                path.lineTo(x2, midY);
                path.lineTo(x2, y2);
            }

            canvas.drawPath(path, tracePaint);
        }

        // ── Nodes: filled dot + two rings ──
        for (float[] node : nodes) {
            float cx = node[0] * w;
            float cy = node[1] * h;
            float dp = getResources().getDisplayMetrics().density;

            // Outer glow ring
            canvas.drawCircle(cx, cy, 10 * dp, nodeRing2Paint);
            // Inner ring
            canvas.drawCircle(cx, cy, 6 * dp, nodeRingPaint);
            // Solid dot
            nodePaint.setColor(0xBB00DC8C);
            canvas.drawCircle(cx, cy, 3.5f * dp, nodePaint);
            // Bright center
            nodePaint.setColor(0xFF80FFD0);
            canvas.drawCircle(cx, cy, 1.5f * dp, nodePaint);
        }

        // ── Subtle horizontal scan line overlay ──
        Paint scanPaint = new Paint();
        scanPaint.setStyle(Paint.Style.STROKE);
        scanPaint.setStrokeWidth(0.5f);
        scanPaint.setColor(0x06FFFFFF);
        int lineSpacing = (int)(20 * getResources().getDisplayMetrics().density);
        for (int y = 0; y < h; y += lineSpacing) {
            canvas.drawLine(0, y, w, y, scanPaint);
        }
    }
}