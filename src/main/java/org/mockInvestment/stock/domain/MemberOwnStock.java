package org.mockInvestment.stock.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mockInvestment.advice.exception.InvalidStockOrderException;
import org.mockInvestment.member.domain.Member;
import org.mockInvestment.stockOrder.domain.StockOrder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class MemberOwnStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Stock stock;

    @OneToMany(mappedBy = "memberOwnStock")
    private List<StockOrder> stockOrders;

    private Long volume;

    private double averageCost;

    @Builder
    public MemberOwnStock(Long id, Member member, Stock stock, StockOrder stockOrder) {
        this.id = id;
        this.member = member;
        this.stock = stock;
        this.stockOrders = new ArrayList<>();
        stockOrders.add(stockOrder);
        volume = stockOrder.getVolume();
        averageCost = stockOrder.getBidPrice();
    }

    public void apply(double price, long volume, boolean isBuy) {
        if (isBuy)
            buy(price, volume);
        else
            sell(price, volume);
    }

    private void buy(double price, long volume) {
        double total = (averageCost * this.volume) + (price * volume);
        this.volume += volume;
        averageCost = total / (double) volume;
    }

    private void sell(double price, long volume) {
        double total = (averageCost * this.volume) - (price * volume);
        if (this.volume - volume < 0)
            throw new InvalidStockOrderException();
        this.volume -= volume;
        averageCost = total / (double) volume;
    }

}
