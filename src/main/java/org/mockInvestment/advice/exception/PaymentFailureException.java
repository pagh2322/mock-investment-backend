package org.mockInvestment.advice.exception;

import org.mockInvestment.advice.exception.general.BadRequestException;

public class PaymentFailureException extends BadRequestException {

    private static final String MESSAGE = "결제를 진행할 수 없습니다.";

    public PaymentFailureException() {
        super(MESSAGE);
    }
}
