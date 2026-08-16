package com.example.hadi_bakalm.data;

import com.example.hadi_bakalm.model.Concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ConceptRepository {

    private static final List<Concept> ALL_CONCEPTS;
    private static final Map<String, Concept> CONCEPT_MAP;

    static {
        List<Concept> list = new ArrayList<>();
        Map<String, Concept> map = new HashMap<>();

        // Nörobiyolojik ve Fizyolojik Kavramlar
        addConcept(list, map, new Concept(
                "Amigdala",
                "Amigdala, beyinde tehdit ve duygusal tepkileri yöneten badem şekilli yapıdır...",
                "Bana göre amigdala...",
                "Örnek: 'Aniden köpek gördüm ve kalbim hızlandı' — bu amigdalanın tepkisi",
                "Günlük hayatta ani tehlike anında hızlı tepki vermemizi sağlar",
                "Kortizol salgılanmasını tetikler, kalp atışını hızlandırır"
        ));
        addConcept(list, map, new Concept("Prefrontal Korteks / Mantık Korteksi (PFC)"));
        addConcept(list, map, new Concept("Anterior Singulat Korteks (ACC)"));
        addConcept(list, map, new Concept("Dopamin / Dopamin Bazal Seviyesi"));
        addConcept(list, map, new Concept("Kortizol"));
        addConcept(list, map, new Concept("Adenozin"));
        addConcept(list, map, new Concept("ATP ve Glikoz"));
        addConcept(list, map, new Concept("Ayna Nöronlar"));
        addConcept(list, map, new Concept("Oksitosin"));
        addConcept(list, map, new Concept("Serotonin"));
        addConcept(list, map, new Concept("Mikroglia Hücreleri"));
        addConcept(list, map, new Concept("Corpus Callosum"));
        addConcept(list, map, new Concept("Nöroplastisite"));
        addConcept(list, map, new Concept("Miyelinizasyon"));
        addConcept(list, map, new Concept("Sinaptik Budama (Synaptic Pruning)"));
        addConcept(list, map, new Concept("Dopaminerjik Down-Regülasyon (Aşağı Yönlü Düzenleme)"));
        addConcept(list, map, new Concept("Anhedoni"));
        addConcept(list, map, new Concept("Homeostazi"));
        addConcept(list, map, new Concept("Bilişsel Atrofi"));
        addConcept(list, map, new Concept("Reticular Activating System (RAS)"));
        addConcept(list, map, new Concept("Allostatik Adaptasyon"));
        addConcept(list, map, new Concept("Hormonal Yarı Ömür"));
        addConcept(list, map, new Concept("Efor Temelli Ödül Döngüsü (Effort-Driven Reward Circuit)"));
        addConcept(list, map, new Concept("Dopaminerjik Çıpa (Hedonic Anchor)"));

        // Bilişsel ve Psikolojik Kavramlar
        addConcept(list, map, new Concept("Bilişsel Cimrilik (Cognitive Miser)"));
        addConcept(list, map, new Concept("Bilişsel Çelişki (Cognitive Dissonance)"));
        addConcept(list, map, new Concept("Bilişsel Esneklik (Cognitive Flexibility)"));
        addConcept(list, map, new Concept("Bilişsel Aşırı Yükleme (Cognitive Overload)"));
        addConcept(list, map, new Concept("Bilişsel Ayrışma"));
        addConcept(list, map, new Concept("Bilişsel Geri Çekilme (Cognitive Disengagement)"));
        addConcept(list, map, new Concept("Bilişsel Kaçakçılık (Cognitive Smuggling)"));
        addConcept(list, map, new Concept("Yüksek Metabolik Maliyet (High Metabolic Cost)"));
        addConcept(list, map, new Concept("Önem Ağı (Salience Network)"));
        addConcept(list, map, new Concept("Örtük Bellek (Implicit Memory)"));
        addConcept(list, map, new Concept("Flaş Bellek (Flashbulb Memory)"));
        addConcept(list, map, new Concept("Tesadüfi Öğrenme (Incidental Learning)"));
        addConcept(list, map, new Concept("Habitüasyon"));
        addConcept(list, map, new Concept("Azalan Verimler Yasası (Diminishing Returns)"));
        addConcept(list, map, new Concept("Batık Maliyet Yanılgısı (Sunk Cost Fallacy)"));
        addConcept(list, map, new Concept("Olumsuzluk Yanlılığı (Negative Bias)"));
        addConcept(list, map, new Concept("Orantılı Önyargı (Proportionality Bias)"));
        addConcept(list, map, new Concept("Teksaslı Nişancı Safsatası"));
        addConcept(list, map, new Concept("Halo Etkisi (Halo Effect)"));
        addConcept(list, map, new Concept("Otorite Önyargısı"));
        addConcept(list, map, new Concept("Sürüye Uyum Sağlama (Conformity) / İtaat Doğrulaması (Conformity Ping)"));
        addConcept(list, map, new Concept("Yengeç Sepeti Sendromu"));
        addConcept(list, map, new Concept("Entelektüelleştirme"));
        addConcept(list, map, new Concept("Gri Kaya Savunması (Grey Rock)"));
        addConcept(list, map, new Concept("Gaslighting"));
        addConcept(list, map, new Concept("DARVO"));
        addConcept(list, map, new Concept("Üçgenleme (Triangulation)"));
        addConcept(list, map, new Concept("Kontrollü Maruziyet / Mikro-Dozlama (Micro-dosing)"));
        addConcept(list, map, new Concept("Mitridatizm"));
        addConcept(list, map, new Concept("Amigdala Gaspı (Amygdala Hijack)"));
        addConcept(list, map, new Concept("Stres Aşlaması (Stress Inoculation)"));
        addConcept(list, map, new Concept("Antropomorfizm"));

        ALL_CONCEPTS = Collections.unmodifiableList(list);
        CONCEPT_MAP = Collections.unmodifiableMap(map);
    }

    private static void addConcept(List<Concept> list, Map<String, Concept> map, Concept concept) {
        if (concept != null && concept.getName() != null) {
            list.add(concept);
            map.put(concept.getName().trim().toLowerCase(), concept);
        }
    }

    public static List<Concept> getAllConcepts() {
        return ALL_CONCEPTS;
    }

    public static Concept getConceptByName(String name) {
        if (name == null) return null;
        return CONCEPT_MAP.get(name.trim().toLowerCase());
    }
}