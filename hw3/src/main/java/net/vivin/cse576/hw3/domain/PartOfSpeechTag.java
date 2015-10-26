package net.vivin.cse576.hw3.domain;

import java.util.Arrays;
import java.util.List;

public enum PartOfSpeechTag implements Tag {
    C("C"), D("D"), E("E"), F("F"), I("I"), J("J"), L("L"), M("M"), N("N"), P("P"), R("R"), S("S"), T("T"), U("U"), V("V"), W("W"), Comma(","), Period("."), ColonSemicolonDash(":"), Parenthesis("-"), QuotationMark("'"), CurrencySymbol("$");

    private String symbol;

    PartOfSpeechTag(String symbol) {
        this.symbol = symbol;
    }

    public static PartOfSpeechTag fromString(String s) {
        if (s == null) {
            return null;
        }

        switch (s) {
            case ",":
                return Comma;

            case ".":
                return Period;

            case ":":
                return ColonSemicolonDash;

            case "-":
                return Parenthesis;

            case "'":
                return QuotationMark;

            case "`":
                return QuotationMark;

            case "$":
                return CurrencySymbol;

            case "#":
                return CurrencySymbol;

            default:
                return PartOfSpeechTag.valueOf(s);
        }
    }

    public static List<PartOfSpeechTag> tags() {
        return Arrays.asList(C, D, E, F, I, J, L, M, N, P, R, S, T, U, V, W, Comma, Period, ColonSemicolonDash, Parenthesis, QuotationMark, CurrencySymbol);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
