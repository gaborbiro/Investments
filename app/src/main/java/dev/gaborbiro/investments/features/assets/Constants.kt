package dev.gaborbiro.investments.features.assets

const val FT_TIME_SERIES_URL =
    "https://markets.ft.com/research/webservices/securities/v1/time-series?symbols=%s&source=%s&dayCount=1&minuteInterval=1440"

const val BINANCE_PRICES_URL = "https://api.binance.com/api/v3/ticker/price"

const val USD2GBP_BASE_URL =
    "https://currencyapi.net/api/v1/rates?key=VXEdvf2A2jCcRcF3tWYds328xxEs17KAUjzP&base=USD"

const val DOC_BASE_URL = "https://markets.ft.com/research/webservices/securities/v1/docs"

const val HISTORY_LENGTH_MONTHS = 2L