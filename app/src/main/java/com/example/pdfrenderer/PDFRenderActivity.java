package com.example.pdfrenderer;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PDFRenderActivity extends AppCompatActivity {
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ImageView imageView;
    private Button btnNextPage;
    private Button btnPrevPage;
    private ParcelFileDescriptor parcelFileDescriptor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfrender);
        init();
        initListeners();
    }

    private void initListeners() {
        View.OnClickListener clickListener = (v) -> {
            if (pdfRenderer != null && currentPage != null) {
                if (v == btnNextPage) {
                    renderPage(currentPage.getIndex() + 1);
                } else if (v == btnPrevPage) {
                    renderPage(currentPage.getIndex() - 1);
                }
            }
        };
        btnNextPage.setOnClickListener(clickListener);
        btnPrevPage.setOnClickListener(clickListener);
    }

    private void init() {
        imageView = findViewById(R.id.imgPdf);
        btnNextPage = findViewById(R.id.btnNextPage);
        btnPrevPage = findViewById(R.id.btnPrevPage);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initRenderer();
        renderPage(0);
    }

    private void initRenderer() {
        try {
            File temp = new File(getCacheDir(), "temp.pdf");
            FileOutputStream fos = new FileOutputStream(temp);
            InputStream is = getAssets().open("test.pdf");

            byte[] buffer = new byte[1024];
            int readBytes;
            while ((readBytes = is.read(buffer)) != -1)
                fos.write(buffer, 0, readBytes);
            fos.close();
            is.close();

            parcelFileDescriptor = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void renderPage(int pageIndex) {
        if (currentPage != null) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(pageIndex);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageView.setImageBitmap(bitmap);
        btnPrevPage.setEnabled(currentPage.getIndex() > 0);
        btnNextPage.setEnabled(currentPage.getIndex() + 1 < pdfRenderer.getPageCount());
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            if (currentPage != null)
                currentPage.close();
            try {
                parcelFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pdfRenderer.close();
        }
        super.onPause();
    }
}
