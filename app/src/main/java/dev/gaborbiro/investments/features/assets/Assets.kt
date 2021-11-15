package dev.gaborbiro.investments.features.assets

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val stockTickers = listOf(
    ft { // ASI Latin American Equity I Acc
        symbol = "GB00B4R0SD95:GBP"
        currencyOverride = "GBp"
        purchase {
            date = parseUTC("2021/06/28 11:15")
            amount = 445.48
            cost = 500.0 // GBP
        }
    },
    ft { // Alphabet Inc A
        symbol = "GOOGL:NSQ"
        purchase {
            date = parseUTC("2017/06/05 00:00")
            amount = 4.0
            cost = 4199.86 // USD
        }
        purchase {
            date = parseUTC("2019/06/03 14:32")
            amount = 3.0
            cost = 3200.79 // USD
        }
    },
    ft { // AXA Framlington Global Technology Z Acc
        symbol = "GB00B4W52V57:GBX"
        purchase {
            date = parseUTC("2020/01/07 11:15")
            amount = 344.59
            cost = 1500.0 // GBP
        }
        purchase {
            date = parseUTC("2021/03/01 11:15")
            amount = 270.795
            cost = 1745.0 // GBP
        }
    },
    ft { // Baillie Gifford American B Acc
        symbol = "GB0006061963:GBX"
        purchase {
            date = parseUTC("2021/03/01 09:15")
            amount = 37.825
            cost = 772.0 // GBP
        }
    },
    ft { // iShares North American Eq Idx (UK) D Acc
        symbol = "GB00B7QK1Y37:GBX"
        purchase {
            date = parseUTC("2017/06/05 00:00")
            amount = 760.698
            cost = 2410.0 // GBP
        }
        purchase {
            date = parseUTC("2018/12/03 11:15")
            amount = 240.77
            cost = 900.0 // GBP
        }
        purchase {
            date = parseUTC("2018/12/10 11:15")
            amount = 1975.169
            cost = 7000.0 // GBP
        }
        purchase {
            date = parseUTC("2019/05/07 11:15")
            amount = 523.286
            cost = 2000.0 // GBP
        }
        purchase {
            date = parseUTC("2019/11/28 11:15")
            amount = 543.992
            cost = 2300.0 // GBP
        }
    },
    ft { // iShares UK Equity Index (UK) D Acc
        symbol = "GB00B7C44X99:GBX"
        purchase {
            date = parseUTC("2017/06/05 00:00")
            amount = 1105.97
            cost = 2400.0 // GBP
        }
    },
    ft { // Capital One Financial Corp
        symbol = "COF:NYQ"
        purchase {
            date = parseUTC("2020/04/15 14:31")
            amount = 39.0
            cost = 2008.9 // GBP
        }
    },
    ft { // Facebook Inc A
        symbol = "FB:NSQ"
        purchase {
            date = parseUTC("2017/06/05 00:00")
            amount = 29.0
            cost = 4806.0 // USD
        }
    },
    ft { // Fidelity China Consumer W Acc
        symbol = "GB00B82ZSC67:GBX"
        purchase {
            date = parseUTC("2021/06/28 11:15")
            amount = 48.85
            cost = 200.0 // GBP
        }
    },
    ft { // First Solar Inc
        symbol = "FSLR:NSQ"
        purchase {
            date = parseUTC("2021/03/01 14:31")
            amount = 11.0
            cost = 907.28 // USD
        }
    },
    ft { // GlaxoSmithKline PLC
        symbol = "GSK:LSE"
        purchase {
            date = parseUTC("2020/07/20 08:01")
            amount = 30.0
            cost = 495.79 // GBP
        }
    },
    ft { // iShares Core MSCI World ETF USD Acc GBP
        symbol = "SWDA:LSE:GBX"
        purchase {
            date = parseUTC("2021/05/25 11:52")
            amount = 43.0
            cost = 2483.11 // GBP
        }
    },
    ft { // iShares Edge MSCI Wld Val Fctr ETF $Acc GBP
        symbol = "IWFV:LSE:GBX"
        purchase {
            date = parseUTC("2017/11/21 00:00")
            amount = 139.0
            cost = 3253.21 // GBP
        }
    },
    ft { // Learning Technologies Group PLC
        symbol = "LTG:LSE"
        purchase {
            date = parseUTC("2021/06/28 08:03")
            amount = 250.0
            cost = 468.75 // GBP
        }
    },
    ft { // Microsoft Corp
        symbol = "MSFT:NSQ"
        purchase {
            date = parseUTC("2021/06/28 14:30")
            amount = 2.0
            cost = 542.98 // USD
        }
    },
    ft { // National Grid PLC
        symbol = "NG.:LSE"
        purchase {
            date = parseUTC("2020/07/13 08:01")
            amount = 45.0
            cost = 395.86 // GBP
        }
    },
    ft { // PensionBee Group PLC When Issue
        symbol = "PBEE:LSE"
        purchase {
            date = parseUTC("2021/04/28 08:02")
            amount = 54.0
            cost = 99.36 // GBP
        }
    },
    ft { // Rathbone Greenbank Global Sstby I Acc
        symbol = "GB00BDZVKB97:GBP"
        currencyOverride = "GBp"
        purchase {
            date = parseUTC("2021/06/28 11:15")
            amount = 130.18
            cost = 200.0 // GBP
        }
    },
    ft { // Restore PLC
        symbol = "RST:LSE"
        purchase {
            date = parseUTC("2021/06/28 08:03")
            amount = 100.0
            cost = 394.94 // GBP
        }
    },
    ft { // Unilever PLC
        symbol = "ULVR:LSE"
        purchase {
            date = parseUTC("2020/07/13 08:01")
            amount = 9.0
            cost = 382.45 // GBP
        }
    },
    ft { // Vanguard FTSE All-World UCITS ETF GBP
        symbol = "VWRL:LSE:GBP"
        purchase {
            date = parseUTC("2019/07/29 08:11")
            amount = 14.0
            cost = 989.72 // GBP
        }
    },
    ft { // Vanguard FTSE 100 Idx Unit Tr £ Acc
        symbol = "GB00BD3RZ368:GBP"
        purchase {
            date = parseUTC("2019/05/03 11:15")
            amount = 16.6694
            cost = 2000.0 // GBP
        }
    },
    ft { // Vodafone Group PLC
        symbol = "VOD:LSE"
        purchase {
            date = parseUTC("2020/07/20 08:01")
            amount = 325.0
            cost = 422.02 // GBP
        }
    },
    ft { // Deliveroo PLC
        symbol = "ROO:LSE"
        purchase {
            date = parseUTC("2021/04/07 00:00")
            amount = 7840.0
            cost = 30576.0 // GBP
        }
    },
    ft { // First Energy Metals Ltd
        symbol = "FE:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:31")
            amount = 2.87604256
            cost = 100.0 // USD
        }
    },
    ft { // Royal Caribbean Group
        symbol = "RCL:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:33")
            amount = 1.1265067
            cost = 100.0 // USD
        }
    },
    ft { // Norwegian Cruise Line Holdings Ltd
        symbol = "NCLH:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:31")
            amount = 3.33222259
            cost = 100.0 // USD
        }
    },
    ft { // Carnival PLC
        symbol = "CCL:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:31")
            amount = 3.46020761
            cost = 100.0 // USD
        }
    },
    ft { // Boeing Co
        symbol = "BA:NYQ"
        purchase {
            date = parseUTC("2021/04/10 14:30")
            amount = 0.39846987
            cost = 100.0 // USD
        }
    },
    ft { // Delta Air Lines Inc
        symbol = "DAL:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:30")
            amount = 2.03832042
            cost = 100.0 // USD
        }
    },
    ft { // Tesla Inc
        symbol = "TSLA:NSQ"
        purchase {
            date = parseUTC("2021/04/12 14:31")
            amount = 0.14612192
            cost = 100.0 // USD
        }
    },
    ft { // Invesco Mortgage Capital Inc
        symbol = "IVR:NYQ"
        purchase {
            date = parseUTC("2021/04/10 14:31")
            amount = 25.51020408
            cost = 100.0 // USD
        }
    },
    ft { // DoorDash Inc Ordinary Shares - Class A
        symbol = "DASH:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:32")
            amount = 0.36603221
            cost = 50.0 // USD
        }
    },
    ft { // MFA Financial Inc
        symbol = "MFA:NYQ"
        purchase {
            date = parseUTC("2021/04/12 14:30")
            amount = 12.01923076
            cost = 50.0 // USD
        }
    },
    ft { // Seaboard Corp
        symbol = "SEB:ASQ"
        purchase {
            date = parseUTC("2021/04/12 14:31")
            amount = 0.01292009
            cost = 50.0 // USD
        }
    },
)
val cryptoTickers = listOf(
    crypto {
        symbol = "SOL"
        amount = 66.815247936
    },
)

private fun parseUTC(dateTime: String): ZonedDateTime {
    return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        .atZone(ZoneOffset.UTC)
}