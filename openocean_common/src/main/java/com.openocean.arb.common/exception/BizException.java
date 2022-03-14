package com.openocean.arb.common.exception;

import com.openocean.arb.common.constants.BizCodeEnum;
import lombok.Data;

/**
 * 业务异常
 *
 * @author lidong
 */
@Data
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 8770620175959486163L;

    /**
     * 错误代码
     */
    private Integer code;

    public BizException() {
        super();
    }

    public BizException(String message) {
        super(message);
        this.code = BizCodeEnum.BIZ_ERROR.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(BizCodeEnum bizCode) {
        super(bizCode.getMessage());
        this.code = bizCode.getCode();
    }

    public BizException(BizCodeEnum bizCode, String message) {
        super(String.format(bizCode.getMessage(), message));
        this.code = bizCode.getCode();
    }

    public static BizException inst(BizCodeEnum bizCode) {
        return new BizException(bizCode);
    }

    public static BizException inst(BizCodeEnum bizCode, String message) {
        return new BizException(bizCode, message);
    }

}
