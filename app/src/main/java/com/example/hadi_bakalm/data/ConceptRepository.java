package com.example.hadi_bakalm.data;

import com.example.hadi_bakalm.model.Concept;

import java.util.ArrayList;
import java.util.List;

/**
 * Tüm kavramların tutulduğu tek merkezi kaynak.
 * MainActivity liste göstermek için, KavramDetayActivity ise
 * tıklanan kavramın detayını bulmak için buradan okur.
 * Veri sadece burada tanımlanır, başka hiçbir yerde tekrar yazılmaz.
 */
@SuppressWarnings("unused")
public class ConceptRepository {

    private static final List<Concept> ALL_CONCEPTS = buildConceptList();

    public static List<Concept> getAllConcepts() {
        return ALL_CONCEPTS;
    }

    /**
     * Kavram adına göre arama yapar. Bulamazsa null döner.
     */
    public static Concept getConceptByName(String name) {
        if (name == null) return null;
        for (Concept concept : ALL_CONCEPTS) {
            if (concept != null && name.equals(concept.getName())) {
                return concept;
            }
        }
        return null;
    }

    private static List<Concept> buildConceptList() {
        List<Concept> list = new ArrayList<>();

        // Nörobiyolojik ve Fizyolojik Kavramlar
        list.add(new Concept(
                "Amigdala",
                "Amigdala, beyinde tehdit ve duygusal tepkileri yöneten badem şekilli yapıdır...",
                "Bana göre amigdala...",
                "Örnek: 'Aniden köpek gördüm ve kalbim hızlandı' — bu amigdalanın tepkisi",
                "Günlük hayatta ani tehlike anında hızlı tepki vermemizi sağlar",
                "Kortizol salgılanmasını tetikler, kalp atışını hızlandırır"
        ));
        list.add(new Concept("Prefrontal Korteks / Mantık Korteksi (PFC)"));
        list.add(new Concept("Anterior Singulat Korteks (ACC)"));
        list.add(new Concept("Dopamin / Dopamin Bazal Seviyesi"));
        list.add(new Concept("Kortizol"));
        list.add(new Concept("Adenozin"));
        list.add(new Concept("ATP ve Glikoz"));
        list.add(new Concept("Ayna Nöronlar"));
        list.add(new Concept("Oksitosin"));
        list.add(new Concept("Serotonin"));
        list.add(new Concept("Mikroglia Hücreleri"));
        list.add(new Concept("Corpus Callosum"));
        list.add(new Concept("Nöroplastisite"));
        list.add(new Concept("Miyelinizasyon"));
        list.add(new Concept("Sinaptik Budama (Synaptic Pruning)"));
        list.add(new Concept("Dopaminerjik Down-Regülasyon (Aşağı Yönlü Düzenleme)"));
        list.add(new Concept("Anhedoni"));
        list.add(new Concept("Homeostazi"));
        list.add(new Concept("Bilişsel Atrofi"));
        list.add(new Concept("Reticular Activating System (RAS)"));
        list.add(new Concept("Allostatik Adaptasyon"));
        list.add(new Concept("Hormonal Yarı Ömür"));
        list.add(new Concept("Efor Temelli Ödül Döngüsü (Effort-Driven Reward Circuit)"));
        list.add(new Concept("Dopaminerjik Çıpa (Hedonic Anchor)"));

        // Bilişsel ve Psikolojik Kavramlar
        list.add(new Concept("Bilişsel Cimrilik (Cognitive Miser)"));
        list.add(new Concept("Bilişsel Çelişki (Cognitive Dissonance)"));
        list.add(new Concept("Bilişsel Esneklik (Cognitive Flexibility)"));
        list.add(new Concept("Bilişsel Aşırı Yükleme (Cognitive Overload)"));
        list.add(new Concept("Bilişsel Ayrışma"));
        list.add(new Concept("Bilişsel Geri Çekilme (Cognitive Disengagement)"));
        list.add(new Concept("Bilişsel Kaçakçılık (Cognitive Smuggling)"));
        list.add(new Concept("Yüksek Metabolik Maliyet (High Metabolic Cost)"));
        list.add(new Concept("Önem Ağı (Salience Network)"));
        list.add(new Concept("Örtük Bellek (Implicit Memory)"));
        list.add(new Concept("Flaş Bellek (Flashbulb Memory)"));
        list.add(new Concept("Tesadüfi Öğrenme (Incidental Learning)"));
        list.add(new Concept("Habitüasyon"));
        list.add(new Concept("Azalan Verimler Yasası (Diminishing Returns)"));
        list.add(new Concept("Batık Maliyet Yanılgısı (Sunk Cost Fallacy)"));
        list.add(new Concept("Olumsuzluk Yanlılığı (Negative Bias)"));
        list.add(new Concept("Orantılı Önyargı (Proportionality Bias)"));
        list.add(new Concept("Teksaslı Nişancı Safsatası"));
        list.add(new Concept("Halo Etkisi (Halo Effect)"));
        list.add(new Concept("Otorite Önyargısı"));
        list.add(new Concept("Sürüye Uyum Sağlama (Conformity) / İtaat Doğrulaması (Conformity Ping)"));
        list.add(new Concept("Yengeç Sepeti Sendromu"));
        list.add(new Concept("Entelektüelleştirme"));
        list.add(new Concept("Gri Kaya Savunması (Grey Rock)"));
        list.add(new Concept("Gaslighting"));
        list.add(new Concept("DARVO"));
        list.add(new Concept("Üçgenleme (Triangulation)"));
        list.add(new Concept("Kontrollü Maruziyet / Mikro-Dozlama (Micro-dosing)"));
        list.add(new Concept("Mitridatizm"));
        list.add(new Concept("Amigdala Gaspı (Amygdala Hijack)"));
        list.add(new Concept("Stres Aşlaması (Stress Inoculation)"));
        list.add(new Concept("Antropomorfizm"));

        return list;
    }
}