package org.mockInvestment.util;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockInvestment.balance.api.BalanceApi;
import org.mockInvestment.balance.application.BalanceFindService;
import org.mockInvestment.comment.api.CommentApi;
import org.mockInvestment.comment.application.*;
import org.mockInvestment.member.api.MemberApi;
import org.mockInvestment.member.application.MemberNicknameUpdateService;
import org.mockInvestment.memberOwnStock.api.MemberOwnStockApi;
import org.mockInvestment.memberOwnStock.application.MemberOwnStockFindService;
import org.mockInvestment.stockOrder.api.StockOrderApi;
import org.mockInvestment.stockOrder.application.StockOrderCreateService;
import org.mockInvestment.stockOrder.application.StockOrderDeleteService;
import org.mockInvestment.stockPrice.api.StockPriceApi;
import org.mockInvestment.stockPrice.application.StockPriceCandleFindService;
import org.mockInvestment.global.auth.AuthFilter;
import org.mockInvestment.global.auth.AuthenticationPrincipalArgumentResolver;
import org.mockInvestment.global.auth.token.JwtTokenProvider;
import org.mockInvestment.stockOrder.application.StockOrderFindService;
import org.mockInvestment.stockPrice.application.StockPriceFindService;
import org.mockInvestment.stockPrice.util.PeriodExtractor;
import org.mockInvestment.stockTicker.api.StockTickerApi;
import org.mockInvestment.stockTicker.application.StockTickerFindService;
import org.mockInvestment.stockTicker.application.StockTickerLikeToggleService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest({
        BalanceApi.class,
        MemberApi.class,
        StockOrderApi.class,
        StockPriceApi.class,
        StockTickerApi.class,
        MemberOwnStockApi.class,
        CommentApi.class,
})
@WithMockUser
@ExtendWith(RestDocumentationExtension.class)
public class ApiTest {

    protected MockMvcRequestSpecification restDocs;

    @MockBean
    protected BalanceFindService balanceFindService;

    @MockBean
    protected MemberNicknameUpdateService memberNicknameUpdateService;

    @MockBean
    protected PeriodExtractor periodExtractor;

    @MockBean
    protected StockPriceCandleFindService stockPriceCandleFindService;

    @MockBean
    protected StockPriceFindService stockPriceFindService;

    @MockBean
    protected StockOrderFindService stockOrderFindService;

    @MockBean
    protected StockOrderCreateService stockOrderCreateService;

    @MockBean
    protected StockOrderDeleteService stockOrderDeleteService;

    @MockBean
    protected StockTickerFindService stockTickerFindService;

    @MockBean
    protected StockTickerLikeToggleService stockTickerLikeToggleService;

    @MockBean
    protected MemberOwnStockFindService memberOwnStockFindService;

    @MockBean
    protected CommentFindService commentFindService;

    @MockBean
    protected CommentCreateService commentCreateService;

    @MockBean
    protected CommentReportCreateService commentReportCreateService;

    @MockBean
    protected CommentUpdateService commentUpdateService;

    @MockBean
    protected CommentDeleteService commentDeleteService;

    @MockBean
    protected CommentLikeToggleService commentLikeToggleService;

    @MockBean
    protected AuthenticationPrincipalArgumentResolver authenticationPrincipalArgumentResolver;

    @MockBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected AuthFilter authFilter;

    protected Map<String, String> cookies = new HashMap<>();


    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        restDocs = RestAssuredMockMvc.given()
                .mockMvc(MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .defaultRequest(post("/**").with(csrf().asHeader()))
                        .defaultRequest(delete("/**").with(csrf().asHeader()))
                        .defaultRequest(put("/**").with(csrf().asHeader()))
                        .apply(documentationConfiguration(restDocumentation)
                                .operationPreprocessors()
                                .withRequestDefaults(prettyPrint(), modifyHeaders().remove("X-CSRF-TOKEN"))
                                .withResponseDefaults(prettyPrint(), modifyHeaders()
                                        .remove("X-Content-Type-Options")
                                        .remove("X-XSS-Protection")
                                        .remove("Pragma")
                                        .remove("X-Frame-Options")))
                        .build())
                .log().all();

        cookies.put("Authorization", "Access Token");
    }

}
