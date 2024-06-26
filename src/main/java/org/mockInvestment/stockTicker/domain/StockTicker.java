package org.mockInvestment.stockTicker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name = "stock_tickers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;

    @Column(name = "stock_market")
    @Enumerated(EnumType.STRING)
    private StockMarket stockMarket;


    public StockTicker(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
