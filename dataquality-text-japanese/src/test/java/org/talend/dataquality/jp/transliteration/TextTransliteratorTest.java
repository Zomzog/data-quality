// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.jp.transliteration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class TextTransliteratorTest {

    private static final String delimiter = " ";

    private static final TextTransliterator transliterator = TextTransliterator.getInstance();

    private static final List<String> testTextList = new ArrayList<>();
    static {
        testTextList.add("親譲りの無鉄砲で小供の時から損ばかりしている");
        testTextList.add("東京は夜の七時"); // chōonpu:東京; Multi-pronunciation Kana: は
        testTextList.add("くノ一 female ninja"); // mixed hiragana, katakana, kanji, english
        testTextList.add("日本型の顔文字👨‍🎨『笑い』(≧▽≦)富士山／^o^＼"); // emoticon
        testTextList.add("縮む"); // to shrink
        testTextList.add("ﾂｲｯﾀｰ");
        testTextList.add("がぎぐげご");
        testTextList.add("ぱぴぷぺぽ");
        testTextList.add("きゃきゅきょ");
        testTextList.add("ぎゃぎゅぎょ");
        testTextList.add("ぴゃぴゅぴょ");
        testTextList.add("シュワルツェネッガー");
        testTextList.add("フィジカル");

    }

    @Test
    public void testTransliterateKatakanaReading() {
        List<String> expactedTextList = new ArrayList<>();
        expactedTextList.add("オヤユズリ ノ ムテッポウ デ ショウ キョウ ノ トキ カラ ソン バカリ シ テ イル");
        expactedTextList.add("トウキョウ ハ ヨル ノ ナナ ジ");
        expactedTextList.add("ク ノ イチ female ninja");
        expactedTextList.add("ニッポン ガタ ノ カオ モジ 👨 ‍ 🎨 『 ワライ 』(≧▽≦) フジサン ／^ o ^＼");
        expactedTextList.add("チヂム");
        expactedTextList.add("ﾂｲｯﾀｰ");
        expactedTextList.add("ガ ギグゲゴ");
        expactedTextList.add("パピプペポ");
        expactedTextList.add("キ ャキュキョ");
        expactedTextList.add("ギャギュギョ");
        expactedTextList.add("ピャピュピョ");
        expactedTextList.add("シュワルツェネッガー");
        expactedTextList.add("フィジカル");

        for (int i = 0; i < testTextList.size(); i++) {
            final String katakanaReading = transliterator.transliterate(testTextList.get(i), TransliterateType.KATAKANA_READING,
                    delimiter);
            assertEquals(expactedTextList.get(i), katakanaReading);
        }
    }

    @Test
    public void testTransliterateKatakanaPronunciation() {
        List<String> expactedTextList = new ArrayList<>();
        expactedTextList.add("オヤユズリ ノ ムテッポー デ ショー キョー ノ トキ カラ ソン バカリ シ テ イル");
        expactedTextList.add("トーキョー ワ ヨル ノ ナナ ジ");
        expactedTextList.add("ク ノ イチ female ninja");
        expactedTextList.add("ニッポン ガタ ノ カオ モジ 👨 ‍ 🎨 『 ワライ 』(≧▽≦) フジサン ／^ o ^＼");
        expactedTextList.add("チジム");
        expactedTextList.add("ﾂｲｯﾀｰ");
        expactedTextList.add("ガ ギグゲゴ");
        expactedTextList.add("パピプペポ");
        expactedTextList.add("キ ャキュキョ");
        expactedTextList.add("ギャギュギョ");
        expactedTextList.add("ピャピュピョ");
        expactedTextList.add("シュワルツェネッガー");
        expactedTextList.add("フィジカル");

        for (int i = 0; i < testTextList.size(); i++) {
            final String katakanaPronunciation = transliterator.transliterate(testTextList.get(i),
                    TransliterateType.KATAKANA_PRONUNCIATION);
            assertEquals(expactedTextList.get(i), katakanaPronunciation);
        }
    }

    @Test
    public void testTransliterateHiragana() {
        List<String> expactedTextList = new ArrayList<>();
        expactedTextList.add("おやゆずり の むてっぽう で しょう きょう の とき から そん ばかり し て いる");
        expactedTextList.add("とうきょう は よる の なな じ");
        expactedTextList.add("く の いち female ninja");
        expactedTextList.add("にっぽん がた の かお もじ 👨 ‍ 🎨 『 わらい 』(≧▽≦) ふじさん ／^ o ^＼");
        expactedTextList.add("ちぢむ");
        expactedTextList.add("ついったあ");
        expactedTextList.add("が ぎぐげご");
        expactedTextList.add("ぱぴぷぺぽ");
        expactedTextList.add("き ゃきゅきょ");
        expactedTextList.add("ぎゃぎゅぎょ");
        expactedTextList.add("ぴゃぴゅぴょ");
        expactedTextList.add("しゅわるつぇねっがあ");
        expactedTextList.add("ふぃじかる");

        for (int i = 0; i < testTextList.size(); i++) {
            final String hiragana = transliterator.transliterate(testTextList.get(i), TransliterateType.HIRAGANA);
            assertEquals(expactedTextList.get(i), hiragana);
        }
    }

    @Test
    public void testTransliterateHepburn() {
        List<String> expactedTextList = new ArrayList<>();
        expactedTextList.add("oyayuzuri no muteppō de shō kyō no toki kara son bakari shi te iru");
        expactedTextList.add("tōkyō wa yoru no nana ji");
        expactedTextList.add("ku no ichi female ninja");
        expactedTextList.add("nippon gata no kao moji 👨 ‍ 🎨 『 warai 』(≧▽≦) fujisan ／^ o ^＼");
        expactedTextList.add("chijimu");
        expactedTextList.add("tsuittā");
        expactedTextList.add("ga gigugego");
        expactedTextList.add("papipupepo");
        expactedTextList.add("ki yakyukyo");
        expactedTextList.add("gyagyugyo");
        expactedTextList.add("pyapyupyo");
        expactedTextList.add("shuwarutsueneggā");
        expactedTextList.add("fuijikaru");

        for (int i = 0; i < testTextList.size(); i++) {
            final String hepburn = transliterator.transliterate(testTextList.get(i), TransliterateType.HEPBURN);
            assertEquals(expactedTextList.get(i), hepburn);
        }

    }

    @Test
    public void testTransliterateKunreiShiki() {
        List<String> expactedTextList = new ArrayList<>();
        expactedTextList.add("oyayuzuri no muteppō de syō kyō no toki kara son bakari si te iru");
        expactedTextList.add("tōkyō wa yoru no nana zi");
        expactedTextList.add("ku no iti female ninja");
        expactedTextList.add("nippon gata no kao mozi 👨 ‍ 🎨 『 warai 』(≧▽≦) huzisan ／^ o ^＼");
        expactedTextList.add("tizimu");
        expactedTextList.add("tuittā");
        expactedTextList.add("ga gigugego");
        expactedTextList.add("papipupepo");
        expactedTextList.add("ki yakyukyo");
        expactedTextList.add("gyagyugyo");
        expactedTextList.add("pyapyupyo");
        expactedTextList.add("syuwarutsueneggā");
        expactedTextList.add("fuizikaru");

        for (int i = 0; i < testTextList.size(); i++) {
            final String kunrei_shiki = transliterator.transliterate(testTextList.get(i), TransliterateType.KUNREI_SHIKI);
            assertEquals(expactedTextList.get(i), kunrei_shiki);
        }
    }

    @Test
    public void testTransliterateNihonShiki() {
        List<String> expactedTextList = new ArrayList<>();
        expactedTextList.add("oyayuzuri no muteppō de syō kyō no toki kara son bakari si te iru");
        expactedTextList.add("tōkyō wa yoru no nana zi");
        expactedTextList.add("ku no iti female ninja");
        expactedTextList.add("nippon gata no kao mozi 👨 ‍ 🎨 『 warai 』(≧▽≦) huzisan ／^ o ^＼");
        expactedTextList.add("tizimu");
        expactedTextList.add("tuittā");
        expactedTextList.add("ga gigugego");
        expactedTextList.add("papipupepo");
        expactedTextList.add("ki yakyukyo");
        expactedTextList.add("gyagyugyo");
        expactedTextList.add("pyapyupyo");
        expactedTextList.add("syuwarutsueneggā");
        expactedTextList.add("fuizikaru");

        for (int i = 0; i < testTextList.size(); i++) {
            final String nihon_shiki = transliterator.transliterate(testTextList.get(i), TransliterateType.NIHON_SHIKI);
            assertEquals(expactedTextList.get(i), nihon_shiki);
        }
    }

    @Test
    public void testChoonpuHiragana() {
        Map<String, String> tests = new HashMap<>();
        tests.put("ローマ字", "ろおまじ"); // Rōmaji (Roman letters)
        tests.put("エレベーター", "えれべえたあ"); // Erebētā (Elevator)
        tests.put("モーターカー", "もおたあかあ"); // Mōtākā (Motor car)
        tests.put("スポーツカーシリーズ", "すぽおつかあ しりいず"); // Supōtsukā shirīzu (Sports car series)
        tests.put("クーデター", "くうでたあ"); // Kūdetā (Coup d'etat)
        tests.put("ラーメン", "らあめん"); // Rāmen
        tests.put("らーめん", "ら ー めん"); // Rāmen (kuromoji return tokens: ら| ー| めん)
        tests.put("モールス信号 ・・ ・ー ーー ・・・ ーーー ・ー・ ー・ー", // Mōrusu shingō ... (Morse code ...)
                "もおるす しんごう ・ ・ ・ー ーー ・ ・ ・ ーーー ・ー・ ー・ー");

        for (Map.Entry<String, String> t : tests.entrySet()) {
            assertEquals(t.getValue(), transliterator.transliterate(t.getKey(), TransliterateType.HIRAGANA));
        }

    }
}