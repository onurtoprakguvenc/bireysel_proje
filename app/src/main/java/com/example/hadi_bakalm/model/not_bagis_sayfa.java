package com.example.hadi_bakalm.model;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class not_bagis_sayfa extends AppCompatActivity implements PurchasesUpdatedListener {

    private Button btnSubmitDonate;
    private BillingClient billingClient;
    private String selectedProductId = "bagis_100";

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

        bindCardClick(R.id.card10, "₺10", "bagis_10");
        bindCardClick(R.id.card20, "₺20", "bagis_20");
        bindCardClick(R.id.card50, "₺50", "bagis_50");
        bindCardClick(R.id.card100, "₺100", "bagis_100");
        bindCardClick(R.id.card200, "₺200", "bagis_200");
        bindCardClick(R.id.card500, "₺500", "bagis_500");
        bindCardClick(R.id.card1000, "₺1000", "bagis_1000");

        if (btnSubmitDonate != null) {
            btnSubmitDonate.setOnClickListener(v -> launchPurchaseFlow());
        }
    }

    private void bindCardClick(int viewId, String displayAmount, String productId) {
        LinearLayout card = findViewById(viewId);
        if (card != null) {
            card.setOnClickListener(v -> selectAmount(displayAmount, productId));
        }
    }

    private void selectAmount(String displayAmount, String productId) {
        selectedProductId = productId;
        if (btnSubmitDonate != null) {
            btnSubmitDonate.setText(String.format("%s Bağış Yap", displayAmount));
        }
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                // Bağlantı kuruldu
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Bağlantı koptuğunda yeniden deneme akışı
            }
        });
    }

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
            if (isFinishing() || isDestroyed()) return;

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

                billingClient.launchBillingFlow(not_bagis_sayfa.this, billingFlowParams);
            } else {
                Toast.makeText(not_bagis_sayfa.this, "Google Play Ürün Bulamadı! Kod: " + billingResult.getResponseCode(), Toast.LENGTH_LONG).show();
            }
        }));
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (isFinishing() || isDestroyed()) return;

        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchase != null) {
                    handlePurchase(purchase);
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Bağış işlemi iptal edildi.", Toast.LENGTH_SHORT).show();
        } else {
            String debugMessage = billingResult.getDebugMessage();
            Toast.makeText(this, "Ödeme sırasında bir hata oluştu: " + (debugMessage.isEmpty() ? "Bilinmeyen Hata" : debugMessage), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            ConsumeResponseListener listener = (billingResult, purchaseToken) -> runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(not_bagis_sayfa.this, "Desteğiniz için çok teşekkürler!", Toast.LENGTH_LONG).show();
                }
            });

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