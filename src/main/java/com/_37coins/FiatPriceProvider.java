package com._37coins;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FiatPriceProvider {
    //collected from api: https://api.bitcoinaverage.com/ticker/global/
    //on jan 25
    public static final List<String> CURRENCIES =
            Arrays.asList("AED", "AFN", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN",
                    "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTC", "BTN", "BWP", "BYR", "BZD", "CAD", "CDF",
                    "CHF", "CLF", "CLP", "CNY", "COP", "CRC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EEK",
                    "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD",
                    "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD",
                    "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD",
                    "LSL", "LTL", "LVL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MTL", "MUR",
                    "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN",
                    "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG",
                    "SEK", "SGD", "SHP", "SLL", "SOS", "SRD", "STD", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND",
                    "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VEF", "VND", "VUV", "WST",
                    "XAF", "XAG", "XAU", "XCD", "XDR", "XOF", "XPF", "YER", "ZAR", "ZMK", "ZMW", "ZWL");

    public static Logger log = LoggerFactory.getLogger(FiatPriceProvider.class);

    private String url;
    private BigDecimal rate;
    private CurrencyUnit cu;

    private final Cache cache;

    public FiatPriceProvider(Cache cache, String url) {
        this.cache = cache;
        this.url = url;
    }

    public FiatPriceProvider(BigDecimal rate, CurrencyUnit cu) {
        this.rate = rate;
        this.cu = cu;
        this.cache = null;
    }

    public PriceTick getLocalCurValue(Locale locale) {
        return getLocalCurValue(null, locale);
    }

    public PriceTick getLocalCurValue(BigDecimal btcValue, CurrencyUnit cu) {
        if (!CURRENCIES.contains(cu.getCode())) {
            cu = CurrencyUnit.of("USD");
        }
        PriceTick pt = extractPriceTick(cu);
        if (pt != null){
            if (btcValue != null) {
                BigDecimal price = pt.getLast().setScale(cu.getDecimalPlaces(), RoundingMode.HALF_DOWN);
                pt.setLastFactored(btcValue.setScale(8, RoundingMode.HALF_DOWN).multiply(price));
            }
            pt.setCurCode(cu.getCode());
        }
        return pt;
    }

    private PriceTick extractPriceTick(CurrencyUnit cu){
        Element cachedPriceTickElement = null;
        if (cache != null) {
            cachedPriceTickElement = cache.get("price" + cu.getCode());
        }
        if (cachedPriceTickElement != null){
            return (PriceTick) cachedPriceTickElement.getObjectValue();
        }
        try {
            Map<String, PriceTick> priceTickMap = receivePriceTickMap();
            updateCache(priceTickMap);
            for (Entry<String, PriceTick> pt : priceTickMap.entrySet()) {
                if (pt.getKey().equalsIgnoreCase(cu.getCode())) {
                    return pt.getValue();
                }
            }
        } catch (Exception ex) {
            log.error("fiat price exception", ex);
            ex.printStackTrace();
        }
        return null;
    }

    private void updateCache(Map<String, PriceTick> priceTickMap) {
        if (cache == null){
            log.info("Cache is null. Cannot update cache");
            return;
        }
        for (Entry<String, PriceTick> pt : priceTickMap.entrySet()) {
            if (pt.getValue() != null) {
                cache.put(new Element("price" + pt.getKey(), pt.getValue()));
            }
        }
    }

    Map<String, PriceTick> receivePriceTickMap() throws URISyntaxException, IOException {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(500, TimeUnit.MILLISECONDS);
        client.setReadTimeout(500, TimeUnit.MILLISECONDS);

        Request request = new Request.Builder()
                .url(url + "/all")
                .build();

        Response response = client.newCall(request).execute();

        return new ObjectMapper().readValue(
                response.body().string(),
                new TypeReference<Map<String, PriceTick>>(){});
    }

    public PriceTick getLocalCurValue(BigDecimal btcValue, Locale locale) {
        if (btcValue != null) {
            if (rate != null && cu != null) {
                return new PriceTick().setLastFactored(btcValue.multiply(rate)).setCurCode(cu.getCode()).setLast(rate);
            }
            CurrencyUnit cu = findCurrency(locale);
            return getLocalCurValue(btcValue, cu);
        }
        return null;
    }

    private CurrencyUnit findCurrency(Locale locale) {
        String country = locale.getCountry();
        final List<CurrencyUnit> popularCurrency = Arrays.asList(
                CurrencyUnit.USD, CurrencyUnit.EUR, CurrencyUnit.JPY,
                CurrencyUnit.CHF, CurrencyUnit.AUD, CurrencyUnit.CAD
        );
        if (StringUtils.isEmpty(country) && StringUtils.isNotEmpty(locale.getLanguage())) {
            List<Locale> locales = LocaleUtils.countriesByLanguage(locale.getLanguage());
            List<CurrencyUnit> currencyUnits = new ArrayList<>();
            for (Locale l : locales) {
                CurrencyUnit currencyByCountry = findCurrencyByCountry(l.getCountry());
                if (currencyByCountry != null) {
                    currencyUnits.add(currencyByCountry);
                }
            }
            for (CurrencyUnit currencyUnit : popularCurrency) {
                if (currencyUnits.contains(currencyUnit)) {
                    return currencyUnit;
                }
            }
            if (!currencyUnits.isEmpty()) {
                return currencyUnits.get(0);
            }
        }
        return findCurrencyByCountry(country, CurrencyUnit.USD);
    }

    private CurrencyUnit findCurrencyByCountry(String country) {
        return findCurrencyByCountry(country, null);
    }

    private CurrencyUnit findCurrencyByCountry(String country, CurrencyUnit defaultCurrencyUnit) {
        try {
            return CurrencyUnit.ofCountry(country);
        } catch (IllegalCurrencyException e) {
            log.warn("Use default currency: " + defaultCurrencyUnit + " for country: " + country);
            return defaultCurrencyUnit;
        }
    }

    public String getLocalCurCode(Locale locale) {
        return findCurrency(locale).getCode();
    }

    public String getLocalCurCode() {
        if (cu != null) {
            return cu.getCode();
        } else {
            return null;
        }
    }

}
