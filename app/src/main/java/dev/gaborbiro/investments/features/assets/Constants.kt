package dev.gaborbiro.investments.features.assets

const val FT_TIME_SERIES_URL =
    "https://markets.ft.com/research/webservices/securities/v1/time-series?symbols=%s&source=%s&dayCount=1&minuteInterval=1440"

const val BINANCE_PRICES_URL = "https://api.binance.com/api/v3/ticker/price"

const val USD2GBP_BASE_URL =
    "https://currencyapi.net/api/v1/rates?key=VXEdvf2A2jCcRcF3tWYds328xxEs17KAUjzP&base=USD"

const val DOC_BASE_URL = "https://markets.ft.com/research/webservices/securities/v1/docs"

val stockTickers = listOf(
    Ticker.FT("GOOGL:NSQ", 7.00, 7400.65),
    Ticker.FT("GB00B4W52V57:GBX", 615.385, 3245.0),
    Ticker.FT("GB0006061963:GBX", 37.835, 772.0),
    Ticker.FT("GB00B7QK1Y37:GBX", 4043.915, 14610.0),
    Ticker.FT("GB00B7C44X99:GBX", 1105.97, 2400.0),
    Ticker.FT("COF:NYQ", 39.0, 2008.9),
    Ticker.FT("FB:NSQ", 29.0, 4806.0),
    Ticker.FT("FSLR:NSQ", 11.0, 907.28),
    Ticker.FT("GSK:LSE", 30.0, 498.27),
    Ticker.FT("SWDA:LSE:GBX", 43.0, 2843.11),
    Ticker.FT("IWFV:LSE:GBX", 139.0, 3253.21),
    Ticker.FT("NG.:LSE", 45.0, 397.84),
    Ticker.FT("PBEE:LSE", 54.0, 99.36),
    Ticker.FT("ULVR:LSE", 9.0, 384.36),
    Ticker.FT("VWRL:LSE:GBP", 14.0, 989.72),
    Ticker.FT("GB00BD3RZ368:GBP", 16.6694, 2000.0),
    Ticker.FT("VOD:LSE", 325.0, 424.13),
    Ticker.FT("FE:NYQ", 2.87604256, 100.0),
    Ticker.FT("RCL:NYQ", 1.1265067, 100.0),
    Ticker.FT("NCLH:NYQ", 3.33222259, 100.0),
    Ticker.FT("CCL:NYQ", 3.46020761, 100.0),
    Ticker.FT("BA:NYQ", 0.39846987, 100.0),
    Ticker.FT("DAL:NYQ", 2.03832042, 100.0),
    Ticker.FT("TSLA:NSQ", 0.14612192, 100.0),
    Ticker.FT("IVR:NYQ", 25.51020408, 100.0),
    Ticker.FT("DASH:NYQ", 0.36603221, 50.0),
    Ticker.FT("MFA:NYQ", 12.01923076, 50.0),
    Ticker.FT("SEB:ASQ", 0.01292009, 50.0),
)
val cryptoTickers = listOf(
    Ticker.Crypto("BTC", 0.21775232),
    Ticker.Crypto("ETH", 0.56514631),
    Ticker.Crypto("ZRX", 30.67180979),
    Ticker.Crypto("OGN", 25.59088487),
    Ticker.Crypto("BAT", 25.2691627),
    Ticker.Crypto("MATIC", 22.8),
    Ticker.Crypto("BCH", 0.04585),
    Ticker.Crypto("DOGE", 96.3),
    Ticker.Crypto("TRX", 334.9),
    Ticker.Crypto("YFI", 0.000425),
    Ticker.Crypto("VET", 143.6),
    Ticker.Crypto("MKR", 0.00493),
    Ticker.Crypto("WRX", 8.5),
    Ticker.Crypto("AAVE", 0.0414),
    Ticker.Crypto("SNX", 1.163),
    Ticker.Crypto("ZEC", 0.08823),
    Ticker.Crypto("COMP", 0.0321),
    Ticker.Crypto("ANKR", 126.3),
    Ticker.Crypto("THETA", 1.877),
    Ticker.Crypto("DOT", 0.571),
    Ticker.Crypto("BTT", 3384.0),
    Ticker.Crypto("ENJ", 7.72),
    Ticker.Crypto("ARDR", 55.2),
    Ticker.Crypto("XEM", 62.93),
    Ticker.Crypto("ZIL", 105.1),
    Ticker.Crypto("BNB", 0.0339),
    Ticker.Crypto("ATOM", 0.805),
    Ticker.Crypto("SXP", 5.228),
    Ticker.Crypto("FIO", 60.82),
    Ticker.Crypto("1INCH", 3.4),
    Ticker.Crypto("MANA", 12.9),
    Ticker.Crypto("GRT", 13.43),
    Ticker.Crypto("CHZ", 37.48),
    Ticker.Crypto("IOTA", 8.78),
    Ticker.Crypto("FIL", 0.1312),
    Ticker.Crypto("LUNA", 1.404),
    Ticker.Crypto("CAKE", 0.485),
    Ticker.Crypto("XRP", 0.268),
)