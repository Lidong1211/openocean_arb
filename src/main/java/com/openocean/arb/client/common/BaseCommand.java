package com.openocean.arb.client.common;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.openocean.arb.client.holder.JlineHolder;
import com.openocean.arb.client.utils.JlineUtil;
import com.openocean.arb.util.BotUtil;
import lombok.Getter;
import org.assertj.core.util.Lists;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 命令行交互 基类
 *
 * @author lidong
 * @date 2022/3/14
 */
@Component
public abstract class BaseCommand {
    /**
     * 命令
     */
    @Getter
    protected String command;
    /**
     * 描述
     */
    @Getter
    protected String desc;
    /**
     * 是否开启该命令,默认不开启
     */
    @Getter
    protected boolean enable = false;
    /**
     * 是否在初始页面显示命令，默认不显示
     */
    @Getter
    protected boolean initShow = false;
    /**
     * 排序
     */
    @Getter
    protected Integer sort = Integer.MAX_VALUE;
    public static final String PROMPT = ">>> ";

    /**
     * 命令行交互
     *
     * @param message 提示信息
     */
    protected void readLine(String message) {
        StringsCompleter stringsCompleter = new StringsCompleter(JlineHolder.commandStrList);
        ArgumentCompleter completer = new ArgumentCompleter(Lists.newArrayList(stringsCompleter));
        readLine(message, StrUtil.EMPTY, completer);
    }

    /**
     * 命令行交互
     *
     * @param message   提示信息
     * @param prefix    >>> 前提示信息
     * @param completer 自动补全配置
     */
    protected void readLine(String message, String prefix, Completer completer) {
        readLine(message, prefix, completer, null);
    }

    /**
     * 命令行交互
     *
     * @param message   提示信息
     * @param prefix    >>> 前提示信息
     * @param completer 自动补全配置
     * @param mask      使用指定的字符掩码读取下一行。如果为空,则字符将被回显。如果为0，则不回显字符
     */
    protected void readLine(String message, String prefix, Completer completer, Character mask) {
        try {
            JlineUtil.setCompleter(completer);
            LineReader reader = JlineUtil.getInstance();
            do {
                reader.printAbove(message);
                if (StrUtil.isNotBlank(message)) {
                    // 如果有提示信息需隔行开启命令行
                    reader.printAbove(StrUtil.EMPTY);
                }
                String line = reader.readLine(StrUtil.concat(true, prefix, PROMPT), mask);
                if (line != null) {
                    message = deal(prefix, line.trim());
                }
            } while (message != null);
        } catch (EndOfFileException e) {
            BotUtil.exit();
        } catch (Exception e) {
            JlineHolder.initCommand.readLine(StrUtil.EMPTY);
        }
    }

    /**
     * 开启对应命令
     */
    public abstract void open();

    /**
     * 命令默认处理逻辑
     */
    public String deal(String prefix, String line) {
        // 从某个命令跳回初始命令
        if (line == null) {
            JlineHolder.initCommand.readLine(StrUtil.EMPTY);
            return null;
        }
        // 处理用户输入的命令
        line = line.toLowerCase().trim();
        BaseCommand command = JlineHolder.commandMap.get(line);
        if (command != null) {
            // 有对应命令则开启命令
            ThreadUtil.execute(() -> command.open());
            return null;
        } else if (StrUtil.isNotBlank(line)) {
            // 没有对应命令则提示
            return invalidMsg(line, JlineHolder.commandStrList);
        }
        // 无输入开启新行
        return StrUtil.EMPTY;
    }


    /**
     * 命令错误提示信息
     *
     * @param input 输入的命令
     * @param list  可用的命令
     */
    public String invalidMsg(String input, List<String> list) {
        return "    invalid choice: '" + input + "' (choose from '" + CollectionUtil.join(list, "', '") + "')";
    }


    /**
     * 帮助信息
     */
    public String helpInfo() {
        return "    - " + StrUtil.padAfter(getCommand(), 12, ' ') + getDesc();
    }
}
