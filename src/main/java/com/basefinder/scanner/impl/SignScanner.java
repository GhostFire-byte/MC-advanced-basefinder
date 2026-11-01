package com.basefinder.scanner.impl;

import com.basefinder.model.Finding;
import com.basefinder.model.FindingCategory;
import com.basefinder.model.Severity;
import com.basefinder.model.VerificationStatus;
import com.basefinder.scanner.AbstractScanner;
import com.basefinder.scanner.FindingSink;
import com.basefinder.scanner.ScanContext;
import com.basefinder.world.BlockRef;
import com.basefinder.world.Blocks;

/** Finds signs with readable text (named stashes/areas are strong markers). */
public final class SignScanner extends AbstractScanner {

    private static final double WEIGHT = 0.7;

    public SignScanner() {
        super("signs", "Sign / Text Scanner",
                "Detects signs that carry readable text, which often label stashes or areas.",
                FindingCategory.SIGN);
    }

    @Override
    public void scan(ScanContext ctx, FindingSink sink) {
        for (BlockRef be : ctx.world().blockEntities()) {
            if (!ctx.scope().containsBlockEntity(be) || !Blocks.isSign(be.id())) {
                continue;
            }
            if (!be.hasText()) {
                continue;
            }
            String text = be.text().strip();
            sink.emit(Finding.builder(id(), FindingCategory.SIGN, be.pos())
                    .confidence(100)
                    .signalWeight(WEIGHT)
                    .verification(VerificationStatus.VERIFIED)
                    .severity(Severity.MEDIUM)
                    .label("Sign: \"" + trim(text) + "\"")
                    .source("Sign text")
                    .detail(text)
                    .observedAtTick(ctx.tick())
                    .meta("text", text)
                    .build());
        }
    }

    private static String trim(String s) {
        String flat = s.replace('\n', ' ');
        return flat.length() <= 24 ? flat : flat.substring(0, 24) + "...";
    }
}
