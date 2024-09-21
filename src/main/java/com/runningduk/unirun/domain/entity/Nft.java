package com.runningduk.unirun.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nft")
public class Nft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="nft_id")
    private int nftId;

    @Column(name="token_id")
    private String tokenId;

    @Column(name="metadata_uri")
    private String metadataUri;

    @Column(name="owner_address")
    private String ownerAddress;

    @Column(name="university_id")
    private String universityId;
}
