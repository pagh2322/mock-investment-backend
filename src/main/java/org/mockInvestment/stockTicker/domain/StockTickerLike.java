package org.mockInvestment.stockTicker.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mockInvestment.member.domain.Member;

@Entity
@Getter
@Table(name = "stock_ticker_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockTickerLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private StockTicker stockTicker;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;


    @Builder
    public StockTickerLike(StockTicker stockTicker, Member member) {
        this.stockTicker = stockTicker;
        this.member = member;
    }

    public void delete() {
        stockTicker = null;
        member = null;
    }

}
