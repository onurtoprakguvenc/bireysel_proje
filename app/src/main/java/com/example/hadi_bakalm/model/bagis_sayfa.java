package com.example.hadi_bakalm.model;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.example.hadi_bakalm.R;

import java.util.Collections;
import java.util.List;

public class bagis_sayfa extends AppCompatActivity implements PurchasesUpdatedListener {

    private Button btnSubmitDonate;
    private BillingClient billingClient;
    private String selectedProductId = "bagis_100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_bagis_sayfa);

        initViews();
        setupCardClicks();
        setupBillingClient();
    }

    private void initViews() {
        btnSubmitDonate = findViewById(R.id.btnSubmitDonation);
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSubmitDonate != null) {
            btnSubmitDonate.setOnClickListener(v -> launchPurchaseFlow());
        }
    }

    private void setupCardClicks() {
        bindDonationCard(R.id.tier50, "₺50", "bagis_50");
        bindDonationCard(R.id.tier100, "₺100", "bagis_100");
        bindDonationCard(R.id.tier250, "₺250", "bagis_250");
        bindDonationCard(R.id.tier500, "₺500", "bagis_500");
        bindDonationCard(R.id.tier1000, "₺1000", "bagis_1000");
        bindDonationCard(R.id.tier2500, "₺2500", "bagis_2500");
    }

    private void bindDonationCard(int viewId, String displayAmount, String productId) {
        findViewById(viewId).setOnClickListener(v -> selectAmount(displayAmount, productId));
    }

    @SuppressLint("SetTextI18n")
    private void selectAmount(String displayAmount, String productId) {
        selectedProductId = productId;
        if (btnSubmitDonate != null) {
            btnSubmitDonate.setText(displayAmount + " Bağış Yap");
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
                // Bağlantı hazır
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Servis bağlantısı koptuğunda
            }
        });
    }

    private void launchPurchaseFlow() {
        if (billingClient == null || !billingClient.isReady()) {
            Toast.makeText(this, "Ödeme servisi hazırlanıyor, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
            setupBillingClient();
            return;
        }

        List<QueryProductDetailsParams.Product> productList = Collections.singletonList(
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

                List<BillingFlowParams.ProductDetailsParams> flowParamsList = Collections.singletonList(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                );

                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(flowParamsList)
                        .build();

                billingClient.launchBillingFlow(bagis_sayfa.this, billingFlowParams);
            } else {
                Toast.makeText(bagis_sayfa.this, "Google Play Ürün Bulamadı! Kod: " + billingResult.getResponseCode(), Toast.LENGTH_LONG).show();
            }
        }));
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "Bağış işlemi iptal edildi.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ödeme hatası: " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> runOnUiThread(() -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(bagis_sayfa.this, "Desteğiniz için çok teşekkürler!", Toast.LENGTH_LONG).show();
                }
            }));
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