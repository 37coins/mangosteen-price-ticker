package com._37coins;

import org.joda.money.CurrencyUnit;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;

public class FiatPriceProviderAvailabilityTest {

    @Test
    public void test(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(null, "https://api.bitcoinaverage.com/ticker/global/");
        assertNotNull(fiatPriceProvider.getLocalCurValue(BigDecimal.valueOf(1.0), CurrencyUnit.USD).getLast());
    }
}
