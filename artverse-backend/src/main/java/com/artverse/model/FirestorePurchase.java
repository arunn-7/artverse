package com.artverse.model;

public class FirestorePurchase {

    private String id;
    private String buyerUid;
    private String buyerEmail;
    private String buyerName;
    private String sellerUid;
    private String sellerEmail;
    private String sellerName;
    private String artworkId;
    private String artworkTitle;
    private String artworkImageUrl;
    private String auctionId;
    private String price;
    private String currency;
    private String purchaseDate;
    private String paymentId;
    private String orderId;
    private String purchaseType;

    public FirestorePurchase() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBuyerUid() { return buyerUid; }
    public void setBuyerUid(String buyerUid) { this.buyerUid = buyerUid; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getSellerUid() { return sellerUid; }
    public void setSellerUid(String sellerUid) { this.sellerUid = sellerUid; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getArtworkId() { return artworkId; }
    public void setArtworkId(String artworkId) { this.artworkId = artworkId; }

    public String getArtworkTitle() { return artworkTitle; }
    public void setArtworkTitle(String artworkTitle) { this.artworkTitle = artworkTitle; }

    public String getArtworkImageUrl() { return artworkImageUrl; }
    public void setArtworkImageUrl(String artworkImageUrl) {
        this.artworkImageUrl = artworkImageUrl;
    }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getPurchaseType() { return purchaseType; }
    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }
}