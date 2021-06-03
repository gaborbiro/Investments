package dev.gaborbiro.investments

import dev.gaborbiro.investments.model.Asset

const val FT_TIME_SERIES_URL =
    "https://markets.ft.com/research/webservices/securities/v1/time-series?symbols=%s&source=%s&dayCount=28&minuteInterval=10080"

const val BINANCE_PRICES_URL = "https://api.binance.com/api/v3/ticker/price"

const val USD2GBP_BASE_URL =
    "https://currencyapi.net/api/v1/rates?key=VXEdvf2A2jCcRcF3tWYds328xxEs17KAUjzP&base=USD"

const val DOC_BASE_URL = "https://markets.ft.com/research/webservices/securities/v1/docs"

val ftAssets = listOf(
    Asset.FT("GOOGL:NSQ", 7.00),
    Asset.FT("GB00B4W52V57:GBX", 615.385),
    Asset.FT("GB0006061963:GBX", 37.835),
    Asset.FT("GB00B7QK1Y37:GBX", 4043.915),
    Asset.FT("GB00B7C44X99:GBX", 1105.97),
    Asset.FT("COF:NYQ", 39.00),
    Asset.FT("FB:NSQ", 29.00),
    Asset.FT("FSLR:NSQ", 11.00),
    Asset.FT("GSK:LSE", 30.00),
    Asset.FT("SWDA:LSE:GBX", 43.00),
    Asset.FT("IWFV:LSE:GBX", 139.00),
    Asset.FT("NG.:LSE", 45.00),
    Asset.FT("PBEE:LSE", 54.00),
    Asset.FT("ULVR:LSE", 9.00),
    Asset.FT("VWRL:LSE:GBP", 14.00),
    Asset.FT("GB00BD3RZ368:GBP", 16.6694),
    Asset.FT("VOD:LSE", 325.00),
    Asset.FT("FE:NYQ", 2.87604256),
    Asset.FT("RCL:NYQ", 1.1265067),
    Asset.FT("CCL:NYQ", 3.46020761),
    Asset.FT("BA:NYQ", 0.39846987),
    Asset.FT("DAL:NYQ", 2.03832042),
    Asset.FT("TSLA:NSQ", 0.14612192),
    Asset.FT("IVR:NYQ", 25.51020408),
    Asset.FT("DASH:NYQ", 0.36603221),
    Asset.FT("MFA:NYQ", 12.01923076),
    Asset.FT("SEB:ASQ", 0.01292009),
)
val cryptoAssets = listOf(
    Asset.Crypto("BTC", 0.21775232),
    Asset.Crypto("ETH", 0.56514631),
    Asset.Crypto("ZRX", 30.67180979),
    Asset.Crypto("OGN", 25.59088487),
    Asset.Crypto("BAT", 25.2691627),
    Asset.Crypto("MATIC", 22.8),
    Asset.Crypto("BCH", 0.04585),
    Asset.Crypto("DOGE", 96.3),
    Asset.Crypto("TRX", 334.9),
    Asset.Crypto("YFI", 0.000425),
    Asset.Crypto("VET", 143.6),
    Asset.Crypto("MKR", 0.00493),
    Asset.Crypto("WRX", 8.5),
    Asset.Crypto("AAVE", 0.0414),
    Asset.Crypto("SNX", 1.163),
    Asset.Crypto("ZEC", 0.08823),
    Asset.Crypto("COMP", 0.0321),
    Asset.Crypto("ANKR", 126.3),
    Asset.Crypto("THETA", 1.877),
    Asset.Crypto("DOT", 0.571),
    Asset.Crypto("BTT", 3384.0),
    Asset.Crypto("ENJ", 7.72),
    Asset.Crypto("ARDR", 55.2),
    Asset.Crypto("XEM", 62.93),
    Asset.Crypto("ZIL", 105.1),
    Asset.Crypto("BNB", 0.0339),
    Asset.Crypto("ATOM", 0.805),
    Asset.Crypto("SXP", 5.228),
    Asset.Crypto("FIO", 60.82),
    Asset.Crypto("1INCH", 3.4),
    Asset.Crypto("MANA", 12.9),
    Asset.Crypto("GRT", 13.43),
    Asset.Crypto("CHZ", 37.48),
    Asset.Crypto("IOTA", 8.78),
    Asset.Crypto("FIL", 0.1312),
    Asset.Crypto("LUNA", 1.404),
    Asset.Crypto("CAKE", 0.485),
    Asset.Crypto("XRP", 0.268),
)