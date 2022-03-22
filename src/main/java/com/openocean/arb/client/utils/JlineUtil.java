package com.openocean.arb.client.utils;

import com.openocean.arb.client.completer.BotCompleter;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

/**
 * 命令行交互工具类
 *
 * @author lidong
 * @date 2022/3/19
 */
public class JlineUtil {
    private JlineUtil() {
    }

    private static BotCompleter completer = new BotCompleter();
    private static LineReader lineReader = LineReaderBuilder.builder().completer(completer).build();

    /**
     * 设置自定义补全
     */
    public static void setCompleter(Completer myCompleter) {
        completer.setCompleter(myCompleter);
    }

    /**
     * 获取LineReader实例
     */
    public static LineReader getInstance() {
        return lineReader;
    }
}
