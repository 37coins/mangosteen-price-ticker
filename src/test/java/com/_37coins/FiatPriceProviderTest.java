package com._37coins;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class FiatPriceProviderTest {
    @Test
    public void testThatGetLocalCurCodeReturnsCorrectCurrency(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("USD", fiatPriceProvider.getLocalCurCode(new Locale("en","US")));
    }
    @Test
    public void testThatGetLocalCurCodeReturnsCorrectCurrencyWithoutCountry(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("USD", fiatPriceProvider.getLocalCurCode(new Locale("en")));
    }
    @Test
    public void testThatGetLocalCurCodeReturnsCorrectCurrencyForMultipleLanguage(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("INR", fiatPriceProvider.getLocalCurCode(new Locale("en","IN")));
    }
    @Test
    public void testThatGetLocalCurCodeReturnsCorrectCurrencyForMultipleLanguageDE(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("EUR", fiatPriceProvider.getLocalCurCode(new Locale("de")));
    }
    @Test
    public void testThatGetLocalCurCodeReturnsCorrectCurrencyForMultipleLanguageES(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("USD", fiatPriceProvider.getLocalCurCode(new Locale("es")));
    }
    @Test
    public void testThatGetLocalCurCodeReturnsUSDIfCanNotFindLanguage(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("USD", fiatPriceProvider.getLocalCurCode(new Locale("33")));
    }
    @Test
    public void testThatGetLocalCurCodeReturnsUSDIfLanguageIsEmpty(){
        FiatPriceProvider fiatPriceProvider = new FiatPriceProvider(BigDecimal.valueOf(1), null);
        assertEquals("USD", fiatPriceProvider.getLocalCurCode(new Locale("")));
    }

}
