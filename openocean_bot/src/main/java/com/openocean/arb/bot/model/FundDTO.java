package com.openocean.arb.bot.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资金
 *
 * @author lidong
 */
@Data
public class FundDTO {
    // DEX报价币种数量
    private BigDecimal dexQuoteQty;
    // DEX基础币种数量
    private BigDecimal dexBaseQty;
    // DEX地址
    private String dexAddress;
    // DEX链秘钥
    private String privateKey;
    // CEX报价币种数量
    private BigDecimal cexQuoteQty;
    // CEX基础币种数量
    private BigDecimal cexBaseQty;

    public static FundDTO createEmpty() {
        FundDTO fundDTO = new FundDTO();
        fundDTO.setDexQuoteQty(BigDecimal.ZERO);
        fundDTO.setDexBaseQty(BigDecimal.ZERO);
        fundDTO.setCexQuoteQty(BigDecimal.ZERO);
        fundDTO.setCexBaseQty(BigDecimal.ZERO);
        return fundDTO;
    }
}
