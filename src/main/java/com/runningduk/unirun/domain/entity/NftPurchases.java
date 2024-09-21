package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name="nft_purchases")
public class NftPurchases {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="nft_purchases_id")
    private int nftPurchasesId;

    @Column(name="user_id")
    private String purchaserId;

    @Column(name="token_id")
    private int tokenId;

    @Column(name="transaction_hash")
    private String transactionHash;

    @Column(name="purchased_at")
    private String purchasedAt;
}
