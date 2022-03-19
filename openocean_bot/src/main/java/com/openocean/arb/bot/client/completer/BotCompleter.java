package com.openocean.arb.bot.client.completer;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.List;

/**
 * 自定义补全
 *
 * @author lidong
 * @date 2022/3/17
 */
public class BotCompleter implements Completer {

    Completer completer;

    public BotCompleter() {
        this.completer = new StringsCompleter();
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        completer.complete(reader, line, candidates);
    }

    public void setCompleter(Completer completer) {
        this.completer = completer;
    }

}
