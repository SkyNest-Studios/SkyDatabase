package dev.skynest.xyz.interfaces;

import dev.skynest.xyz.container.tmp.models.cvs.TMPCVSAppend;
import dev.skynest.xyz.container.tmp.models.cvs.TMPCVSReplace;
import dev.skynest.xyz.container.tmp.models.txt.TMPTXTAppend;
import dev.skynest.xyz.container.tmp.models.txt.TMPTXTReplace;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Type {

    TXT_REPLACE(new TMPTXTReplace<>()),
    TXT_APPEND(new TMPTXTAppend<>()),
    CVS_REPLACE(new TMPCVSReplace<>()),
    CVS_APPEND(new TMPCVSAppend<>()),
    ;

    private final TMP<? extends IData> temporarySystem;

}
