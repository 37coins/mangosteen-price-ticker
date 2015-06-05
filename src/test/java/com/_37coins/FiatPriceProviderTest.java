package com._37coins;

import org.joda.money.CurrencyUnit;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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

    @Test
    public void testThatGetLocalCurValueReturnsCorrectValue() throws IOException, URISyntaxException {
        FiatPriceProvider fiatPriceProvider = spy(new FiatPriceProvider(null, ""));
        HashMap<String, PriceTick> stringPriceTickHashMap = new HashMap<>();
        PriceTick usd = new PriceTick().setLast(new BigDecimal(230)).setCurCode("USD");
        stringPriceTickHashMap.put("USD", usd);
        doReturn(stringPriceTickHashMap).when(fiatPriceProvider).receivePriceTickMap();
        assertEquals(usd, fiatPriceProvider.getLocalCurValue(BigDecimal.valueOf(1.0), CurrencyUnit.USD));
    }

}
