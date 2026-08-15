package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.example.hadi_bakalm.R;

import java.util.ArrayList;
import java.util.List;

public class bagis_sayfa extends AppCompatActivity implements PurchasesUpdatedListener {

    private Button btnSubmitDonate;
    private BillingClient billingClient;
    private String selectedProductId = "bagis_100"; // Varsayılan tutar: ₺100

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_bagis_sayfa);

        initViews();
        setupClickListeners();
        setupBillingClient();
    }

    private void initViews() {
        btnSubmitDonate = findViewById(R.id.btnSubmitDonate);
    }

    private void setupClickListeners() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        LinearLayout card10 = findViewById(R.id.card10);
        LinearLayout card20 = findViewById(R.id.card20);
        LinearLayout card50 = findViewById(R.id.card50);
        LinearLayout card100 = findViewById(R.id.card100);
        LinearLayout card200 = findViewById(R.id.card200);
        LinearLayout card500 = findViewById(R.id.card500);
        LinearLayout card1000 = findViewById(R.id.card1000);

        // Tutar Seçimleri ve İlgili Google Play Product ID Eşleştirmesi
        if (card10 != null) card10.setOnClickListener(v -> selectAmount("₺10", "bagis_10"));
        if (card20 != null) card20.setOnClickListener(v -> selectAmount("₺20", "bagis_20"));
        if (card50 != null) card50.setOnClickListener(v -> selectAmount("₺50", "bagis_50"));
        if (card100 != null) card100.setOnClickListener(v -> selectAmount("₺100", "bagis_100"));
        if (card200 != null) card200.setOnClickListener(v -> selectAmount("₺200", "bagis_200"));
        if (card500 != null) card500.setOnClickListener(v -> selectAmount("₺500", "bagis_500"));
        if (card1000 != null) card1000.setOnClickListener(v -> selectAmount("₺1000", "bagis_1000"));

        // Ana Bağış Butonu
        if (btnSubmitDonate != null) {
            btnSubmitDonate.setOnClickListener(v -> launchPurchaseFlow());
        }
    }

    @SuppressLint("SetTextI18n")
    private void selectAmount(String displayAmount, String productId) {
        selectedProductId = productId;
        if (btnSubmitDonate != null) {
            btnSubmitDonate.setText(displayAmount + " Bağış Yap");
        }
    }

    // 1. Google Play Billing İstemcisini Bağlama
    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                // Bağlantı hazır olduğunda gerekirse log basılabilir
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Bağlantı koparsa servis tekrar bağlanmaya hazır bekler
            }
        });
    }

    // 2. Satın Alma Akışını Başlatma
    private void launchPurchaseFlow() {
        if (billingClient == null || !billingClient.isReady()) {
            Toast.makeText(this, "Ödeme servisi hazırlanıyor, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
            setupBillingClient();
            return;
        }

        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(selectedProductId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> runOnUiThread(() -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !productDetailsList.isEmpty()) {
                ProductDetails productDetails = productDetailsList.get(0);

                List<BillingFlowParams.ProductDetailsParams> flowProductDetailsParamsList = new ArrayList<>();
                flowProductDetailsParamsList.add(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(flowProductDetailsParamsList)
                        .build();

                billingClient.launchBillingFlow(bagis_sayfa.this, billingFlowParams);
            } else {
                Toast.makeText(bagis_sayfa.this, "Google Play Ürün Bulamadı! Kod: " + billingResult.getResponseCode(), Toast.LENGTH_LONG).show();
            }
        }));
    }

    // 3. Ödeme Sonucu Dinleyicisi
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Bağış işlemi iptal edildi.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ödeme sırasında bir hata oluştu: " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 4. Satın Alınan Bağışı Tüketme (Consume) İşlemi
    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            ConsumeResponseListener listener = (billingResult, purchaseToken) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(bagis_sayfa.this, "Destek yapıldığın için çok teşekkürler!", Toast.LENGTH_LONG).show();
                }
            };

            billingClient.consumeAsync(consumeParams, listener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}