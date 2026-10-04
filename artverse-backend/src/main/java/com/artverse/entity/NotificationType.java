package com.artverse.entity;

public enum NotificationType {

    FOLLOW,
    LIKE,
    COMMENT,
    ANNOUNCEMENT,
    PURCHASE,

    // Auction notifications
    NEW_BID,
    OUTBID,
    AUCTION_WON,
    AUCTION_ENDED,

    // Commission notifications
    COMMISSION
}