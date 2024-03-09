package org.mockInvestment.stock.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockInvestment.advice.exception.StockNotFoundException;
import org.mockInvestment.stock.domain.Stock;
import org.mockInvestment.stock.domain.StockPrice;
import org.mockInvestment.stock.domain.StockPriceHistory;
import org.mockInvestment.stock.dto.StockInfoDetailResponse;
import org.mockInvestment.stock.dto.StockPriceCandlesResponse;
import org.mockInvestment.stock.repository.StockPriceHistoryRepository;
import org.mockInvestment.stock.repository.StockPriceRedisRepository;
import org.mockInvestment.stock.repository.StockRepository;
import org.mockInvestment.stock.util.PeriodExtractor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPriceHistoryRepository stockPriceHistoryRepository;

    @Mock
    private PeriodExtractor periodExtractor;

    @Mock
    private StockPriceRedisRepository stockPriceRedisRepository;

    Map<String, String> stockInfoWithBase = new HashMap<>();

    Map<String, String> stockInfoWithoutBase = new HashMap<>();

    @BeforeEach
    void setUp() {
        stockInfoWithBase.put("name", "Mock Stock");
        stockInfoWithBase.put("symbol", "MOCK");
        stockInfoWithBase.put("curr", "1.0");
        stockInfoWithBase.put("base", "0.1");

        stockInfoWithoutBase.put("name", "Mock Stock");
        stockInfoWithoutBase.put("symbol", "MOCK");
        stockInfoWithoutBase.put("curr", "1.0");
    }

    @Test
    @DisplayName("유효한 코드로 현재 주가에 대한 정보를 불러온다. 레디스에 전날의 가격을 저장했다.")
    void findStockInfoDetail_containBase() {
        when(stockPriceRedisRepository.get(any(String.class)))
                .thenReturn(stockInfoWithBase);

        StockInfoDetailResponse response = stockService.findStockInfoDetail("Mock Stock");

        assertThat(response.base()).isEqualTo(0.1);
        assertThat(response.price()).isEqualTo(1.0);
        assertThat(response.name()).isEqualTo("Mock Stock");
    }

    @Test
    @DisplayName("유효한 코드로 현재 주가에 대한 정보를 불러온다. 레디스에 전날의 가격을 저장하지 않았다.")
    void findStockInfoDetail_notContainBase() {
        when(stockPriceRedisRepository.get(any(String.class)))
                .thenReturn(stockInfoWithoutBase);
        when(stockRepository.findByCode(any(String.class)))
                .thenReturn(Optional.of(new Stock()));
        StockPrice today = new StockPrice(1.0, 1.0, 1.0, 1.0, 1.0);
        StockPrice yesterday = new StockPrice(0.1, 0.1, 0.1, 0.1, 0.1);
        List<StockPriceHistory> histories = new ArrayList<>();
        histories.add(new StockPriceHistory(today, 2L));
        histories.add(new StockPriceHistory(yesterday, 1L));
        when(stockPriceHistoryRepository.findTop2ByStockOrderByDateDesc(any(Stock.class)))
                .thenReturn(histories);

        StockInfoDetailResponse response = stockService.findStockInfoDetail("Mock Stock");

        assertThat(response.base()).isEqualTo(0.1);
        assertThat(response.price()).isEqualTo(1.0);
        assertThat(response.name()).isEqualTo("Mock Stock");
    }


    @Test
    @DisplayName("유효한 코드로 현재 주가(들)에 대한 간략한 정보를 불러온다. 레디스에 전날의 가격을 저장하지 않았다.")
    void findStockInfoSummaries_notContainBase() {
        when(stockPriceRedisRepository.get(any(String.class)))
                .thenReturn(stockInfoWithoutBase);
        when(stockRepository.findByCode(any(String.class)))
                .thenReturn(Optional.of(new Stock()));
        StockPrice stockPrice = new StockPrice(1.0, 1.0, 1.0, 1.0, 1.0);
        List<StockPriceHistory> histories = new ArrayList<>();
        histories.add(new StockPriceHistory(stockPrice, 1L));
        histories.add(new StockPriceHistory(stockPrice, 1L));
        when(stockPriceHistoryRepository.findTop2ByStockOrderByDateDesc(any(Stock.class)))
                .thenReturn(histories);

        List<String> stockCodes = new ArrayList<>();
        stockCodes.add("US1");
        stockCodes.add("US2");
        assertThat(stockService.findStockInfoSummaries(stockCodes).stocks().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("유효하지 않은 코드로 현재 주가(들)에 대한 간략한 정보를 불려오려고 하면, StockNotFoundException을 발생시킨다.")
    void findStockCurrentPrice_exception_invalidCode() {
        when(stockRepository.findByCode(any(String.class)))
                .thenThrow(new StockNotFoundException());

        List<String> stockCodes = new ArrayList<>();
        stockCodes.add("XXX");
        assertThatThrownBy(() -> stockService.findStockInfoSummaries(stockCodes))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    @DisplayName("최근 3개월 동안의 주가 정보를 불러온다.")
    void findStockPriceHistoriesForThreeMonths() {
        when(stockRepository.findByCode(any(String.class)))
                .thenReturn(Optional.of(new Stock()));
        List<StockPriceHistory> histories = new ArrayList<>();
        StockPrice stockPrice = new StockPrice(1.0, 1.0, 1.0, 1.0, 1.0);
        int count = 10;
        for (int i = 0; i < count; i++) {
            histories.add(new StockPriceHistory(stockPrice, 1L));
        }
        when(stockPriceHistoryRepository.findAllByStockAndDateBetween(any(Stock.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(histories);
        when(periodExtractor.getStart())
                .thenReturn(LocalDate.now());
        when(periodExtractor.getEnd())
                .thenReturn(LocalDate.now());

        StockPriceCandlesResponse response = stockService.findStockPriceHistories("US1", periodExtractor);

        assertThat(response.candles().size()).isEqualTo(count);
    }

}